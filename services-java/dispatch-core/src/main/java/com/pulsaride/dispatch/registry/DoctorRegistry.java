package com.pulsaride.dispatch.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsaride.dispatch.domain.Doctor;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Steps 4 and 8 of the matching algorithm doc.
 *
 * Redis layout:
 * doctors:available -> Set<String> of doctor ids currently free
 * doctor:{id} -> Hash: specialty, description, load,
 * embedding (JSON array)
 *
 * Reservation uses WATCH/MULTI/EXEC (the doc's "Redis Atomic Reservation"):
 * if another request reserves the same doctor between us reading
 * "available" and committing, the transaction aborts and we return
 * false so the caller moves to the next candidate - this is exactly
 * the doc's Step 8 race-condition fix.
 */
@Component
public class DoctorRegistry {

    private static final String AVAILABLE_SET_KEY = "doctors:available";
    private static final String DOCTOR_KEY_PREFIX = "doctor:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public DoctorRegistry(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /**
     * Step 4: "Retrieve available doctors" + "we immediately remove unavailable
     * doctors".
     */
    public List<Doctor> getAvailableDoctors() {
        Set<String> ids = redis.opsForSet().members(AVAILABLE_SET_KEY);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Doctor> doctors = new ArrayList<>();
        for (String id : ids) {
            loadDoctor(id).ifPresent(doctors::add);
        }
        return doctors;
    }

    private Optional<Doctor> loadDoctor(String id) {
        Map<Object, Object> fields = redis.opsForHash().entries(DOCTOR_KEY_PREFIX + id);
        if (fields.isEmpty()) {
            return Optional.empty();
        }
        try {
            double[] embedding = objectMapper.readValue(
                    String.valueOf(fields.getOrDefault("embedding", "[]")), double[].class);

            return Optional.of(new Doctor(
                    id,
                    String.valueOf(fields.getOrDefault("specialty", "")),
                    String.valueOf(fields.getOrDefault("description", "")),
                    "1".equals(String.valueOf(fields.getOrDefault("available", "0"))),
                    Double.parseDouble(String.valueOf(fields.getOrDefault("load", "0"))),
                    embedding));
        } catch (Exception e) {
            // A malformed doctor record shouldn't crash matching for everyone else.
            return Optional.empty();
        }
    }

    /**
     * Step 8: atomic reservation. Returns true if this call won the race
     * and the doctor is now reserved for requestId; false if someone
     * else got there first (or the doctor was no longer available).
     */
    public boolean tryReserve(String doctorId, String requestId) {
        String doctorKey = DOCTOR_KEY_PREFIX + doctorId;

        List<Object> execResult = redis.execute(new SessionCallback<>() {
            @Override
            public List<Object> execute(org.springframework.data.redis.core.RedisOperations operations) {
                operations.watch(doctorKey);

                Object availableRaw = operations.opsForHash().get(doctorKey, "available");
                boolean stillAvailable = "1".equals(String.valueOf(availableRaw));

                if (!stillAvailable) {
                    operations.unwatch();
                    return null; // signal "not reserved" without opening a transaction
                }

                operations.multi();
                operations.opsForHash().put(doctorKey, "available", "0");
                operations.opsForHash().put(doctorKey, "reservedFor", requestId);
                operations.opsForSet().remove(AVAILABLE_SET_KEY, doctorId);
                return operations.exec(); // null if WATCH detected a concurrent change
            }
        });

        return execResult != null && !execResult.isEmpty();
    }

    /**
     * Frees a doctor back up - used on refusal, timeout, or completed consultation.
     */
    public void release(String doctorId) {
        String doctorKey = DOCTOR_KEY_PREFIX + doctorId;
        redis.opsForHash().put(doctorKey, "available", "1");
        redis.opsForHash().delete(doctorKey, "reservedFor");
        redis.opsForSet().add(AVAILABLE_SET_KEY, doctorId);
    }

    /**
     * Called from ProProfileUpdatedListener. Updates specialty + the
     * embedding computed from the profile description. Does NOT touch
     * available/load or set membership -
     * those come from a separate event/seeding path.
     */
    public void updateProfile(String doctorId, String specialty, String description, double[] embedding) {
        String doctorKey = DOCTOR_KEY_PREFIX + doctorId;
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("specialty", specialty);
            fields.put("description", description);
            fields.put("embedding", objectMapper.writeValueAsString(embedding));
            redis.opsForHash().putAll(doctorKey, fields);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update embedding for doctor " + doctorId, e);
        }
    }

    /**
     * Called from ProAvailabilityChangedListener. Updates available +
     * load, and keeps the doctors:available set in sync - the same set
     * getAvailableDoctors() reads from. currentLoad may be null (the
     * contract allows it); when null we leave the "load" field
     * untouched rather than overwriting it with a bogus 0.
     */
    public void updateAvailability(String doctorId, boolean available, Integer currentLoad) {
        String doctorKey = DOCTOR_KEY_PREFIX + doctorId;

        Map<String, String> fields = new HashMap<>();
        fields.put("available", available ? "1" : "0");
        if (currentLoad != null) {
            fields.put("load", String.valueOf(currentLoad));
        }
        redis.opsForHash().putAll(doctorKey, fields);

        if (available) {
            redis.opsForSet().add(AVAILABLE_SET_KEY, doctorId);
        } else {
            redis.opsForSet().remove(AVAILABLE_SET_KEY, doctorId);
        }
    }
}
