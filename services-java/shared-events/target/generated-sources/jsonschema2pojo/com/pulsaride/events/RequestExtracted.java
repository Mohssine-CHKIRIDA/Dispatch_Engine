
package com.pulsaride.events;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * RequestExtracted
 * <p>
 * Published by IA1 (NLP Triage) after turning the patient's free text into structured data. Consumed by both IA2 (urgency scoring) and IA3 (matching) — so this shape is the shared 'understanding' of the request that the rest of the pipeline builds on.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "event_id",
    "request_id",
    "event_type",
    "version",
    "produced_at",
    "producer",
    "payload"
})
@Generated("jsonschema2pojo")
public class RequestExtracted {

    /**
     * Unique ID for this specific event instance (not the request).
     * (Required)
     * 
     */
    @JsonProperty("event_id")
    @JsonPropertyDescription("Unique ID for this specific event instance (not the request).")
    @NotNull
    private UUID eventId;
    /**
     * Correlation ID shared by every event belonging to the same patient request, from submission to closure. This is how Evaluation reconstructs a full request lifecycle across services.
     * (Required)
     * 
     */
    @JsonProperty("request_id")
    @JsonPropertyDescription("Correlation ID shared by every event belonging to the same patient request, from submission to closure. This is how Evaluation reconstructs a full request lifecycle across services.")
    @NotNull
    private UUID requestId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("event_type")
    @NotNull
    private Object eventType;
    /**
     * Schema version for this event type, e.g. '1.0'. Bump on breaking changes so consumers can branch on it during migration.
     * (Required)
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("Schema version for this event type, e.g. '1.0'. Bump on breaking changes so consumers can branch on it during migration.")
    @NotNull
    private String version = "1.0";
    /**
     * ISO 8601 UTC timestamp of when the producing service emitted this event.
     * (Required)
     * 
     */
    @JsonProperty("produced_at")
    @JsonPropertyDescription("ISO 8601 UTC timestamp of when the producing service emitted this event.")
    @NotNull
    private OffsetDateTime producedAt;
    /**
     * Name of the service that published this event, e.g. 'ingestion', 'nlp-triage'. Useful for debugging and for Evaluation's traceability.
     * (Required)
     * 
     */
    @JsonProperty("producer")
    @JsonPropertyDescription("Name of the service that published this event, e.g. 'ingestion', 'nlp-triage'. Useful for debugging and for Evaluation's traceability.")
    @NotNull
    private String producer;
    /**
     * ExtractedPayload
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("payload")
    @Valid
    @NotNull
    private ExtractedPayload payload;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RequestExtracted() {
    }

    /**
     * 
     * @param eventId
     *     Unique ID for this specific event instance (not the request).
     * @param producedAt
     *     ISO 8601 UTC timestamp of when the producing service emitted this event.
     * @param payload
     *     ExtractedPayload.
     * @param requestId
     *     Correlation ID shared by every event belonging to the same patient request, from submission to closure. This is how Evaluation reconstructs a full request lifecycle across services.
     * @param producer
     *     Name of the service that published this event, e.g. 'ingestion', 'nlp-triage'. Useful for debugging and for Evaluation's traceability.
     * @param version
     *     Schema version for this event type, e.g. '1.0'. Bump on breaking changes so consumers can branch on it during migration.
     */
    public RequestExtracted(UUID eventId, UUID requestId, Object eventType, String version, OffsetDateTime producedAt, String producer, ExtractedPayload payload) {
        super();
        this.eventId = eventId;
        this.requestId = requestId;
        this.eventType = eventType;
        this.version = version;
        this.producedAt = producedAt;
        this.producer = producer;
        this.payload = payload;
    }

    /**
     * Unique ID for this specific event instance (not the request).
     * (Required)
     * 
     */
    @JsonProperty("event_id")
    public UUID getEventId() {
        return eventId;
    }

    /**
     * Unique ID for this specific event instance (not the request).
     * (Required)
     * 
     */
    @JsonProperty("event_id")
    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public RequestExtracted withEventId(UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    /**
     * Correlation ID shared by every event belonging to the same patient request, from submission to closure. This is how Evaluation reconstructs a full request lifecycle across services.
     * (Required)
     * 
     */
    @JsonProperty("request_id")
    public UUID getRequestId() {
        return requestId;
    }

    /**
     * Correlation ID shared by every event belonging to the same patient request, from submission to closure. This is how Evaluation reconstructs a full request lifecycle across services.
     * (Required)
     * 
     */
    @JsonProperty("request_id")
    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public RequestExtracted withRequestId(UUID requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("event_type")
    public Object getEventType() {
        return eventType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("event_type")
    public void setEventType(Object eventType) {
        this.eventType = eventType;
    }

    public RequestExtracted withEventType(Object eventType) {
        this.eventType = eventType;
        return this;
    }

    /**
     * Schema version for this event type, e.g. '1.0'. Bump on breaking changes so consumers can branch on it during migration.
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * Schema version for this event type, e.g. '1.0'. Bump on breaking changes so consumers can branch on it during migration.
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    public RequestExtracted withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * ISO 8601 UTC timestamp of when the producing service emitted this event.
     * (Required)
     * 
     */
    @JsonProperty("produced_at")
    public OffsetDateTime getProducedAt() {
        return producedAt;
    }

    /**
     * ISO 8601 UTC timestamp of when the producing service emitted this event.
     * (Required)
     * 
     */
    @JsonProperty("produced_at")
    public void setProducedAt(OffsetDateTime producedAt) {
        this.producedAt = producedAt;
    }

    public RequestExtracted withProducedAt(OffsetDateTime producedAt) {
        this.producedAt = producedAt;
        return this;
    }

    /**
     * Name of the service that published this event, e.g. 'ingestion', 'nlp-triage'. Useful for debugging and for Evaluation's traceability.
     * (Required)
     * 
     */
    @JsonProperty("producer")
    public String getProducer() {
        return producer;
    }

    /**
     * Name of the service that published this event, e.g. 'ingestion', 'nlp-triage'. Useful for debugging and for Evaluation's traceability.
     * (Required)
     * 
     */
    @JsonProperty("producer")
    public void setProducer(String producer) {
        this.producer = producer;
    }

    public RequestExtracted withProducer(String producer) {
        this.producer = producer;
        return this;
    }

    /**
     * ExtractedPayload
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("payload")
    public ExtractedPayload getPayload() {
        return payload;
    }

    /**
     * ExtractedPayload
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("payload")
    public void setPayload(ExtractedPayload payload) {
        this.payload = payload;
    }

    public RequestExtracted withPayload(ExtractedPayload payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RequestExtracted.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("eventId");
        sb.append('=');
        sb.append(((this.eventId == null)?"<null>":this.eventId));
        sb.append(',');
        sb.append("requestId");
        sb.append('=');
        sb.append(((this.requestId == null)?"<null>":this.requestId));
        sb.append(',');
        sb.append("eventType");
        sb.append('=');
        sb.append(((this.eventType == null)?"<null>":this.eventType));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("producedAt");
        sb.append('=');
        sb.append(((this.producedAt == null)?"<null>":this.producedAt));
        sb.append(',');
        sb.append("producer");
        sb.append('=');
        sb.append(((this.producer == null)?"<null>":this.producer));
        sb.append(',');
        sb.append("payload");
        sb.append('=');
        sb.append(((this.payload == null)?"<null>":this.payload));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.eventId == null)? 0 :this.eventId.hashCode()));
        result = ((result* 31)+((this.producedAt == null)? 0 :this.producedAt.hashCode()));
        result = ((result* 31)+((this.payload == null)? 0 :this.payload.hashCode()));
        result = ((result* 31)+((this.requestId == null)? 0 :this.requestId.hashCode()));
        result = ((result* 31)+((this.producer == null)? 0 :this.producer.hashCode()));
        result = ((result* 31)+((this.eventType == null)? 0 :this.eventType.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestExtracted) == false) {
            return false;
        }
        RequestExtracted rhs = ((RequestExtracted) other);
        return ((((((((this.eventId == rhs.eventId)||((this.eventId!= null)&&this.eventId.equals(rhs.eventId)))&&((this.producedAt == rhs.producedAt)||((this.producedAt!= null)&&this.producedAt.equals(rhs.producedAt))))&&((this.payload == rhs.payload)||((this.payload!= null)&&this.payload.equals(rhs.payload))))&&((this.requestId == rhs.requestId)||((this.requestId!= null)&&this.requestId.equals(rhs.requestId))))&&((this.producer == rhs.producer)||((this.producer!= null)&&this.producer.equals(rhs.producer))))&&((this.eventType == rhs.eventType)||((this.eventType!= null)&&this.eventType.equals(rhs.eventType))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))));
    }

}
