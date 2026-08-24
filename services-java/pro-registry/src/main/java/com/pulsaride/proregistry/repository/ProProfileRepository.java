package com.pulsaride.proregistry.repository;

import com.pulsaride.proregistry.entity.ProProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProProfileRepository extends JpaRepository<ProProfile, UUID> {
}
