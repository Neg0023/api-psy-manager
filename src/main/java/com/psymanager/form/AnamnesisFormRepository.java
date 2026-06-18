package com.psymanager.form;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnamnesisFormRepository extends JpaRepository<AnamnesisForm, Long> {

    List<AnamnesisForm> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<AnamnesisForm> findAllByOrderByCreatedAtDesc();
}
