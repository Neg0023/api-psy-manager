package com.psymanager.patient;

import com.psymanager.common.exception.ConflictException;
import com.psymanager.common.exception.ResourceNotFoundException;
import com.psymanager.patient.dto.PatientRequest;
import com.psymanager.patient.dto.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> list(PatientStatus status, String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? "" : q.trim();
        return repository.search(status, query, pageable).map(PatientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return PatientMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        String cpf = PatientMapper.normalizeCpf(request.cpf());
        if (cpf != null && repository.existsByCpf(cpf)) {
            throw new ConflictException("Já existe um paciente com este CPF");
        }
        Patient patient = new Patient();
        PatientMapper.applyRequest(patient, request, cpf);
        return PatientMapper.toResponse(repository.save(patient));
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findOrThrow(id);
        String cpf = PatientMapper.normalizeCpf(request.cpf());
        if (cpf != null && repository.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflictException("Já existe outro paciente com este CPF");
        }
        PatientMapper.applyRequest(patient, request, cpf);
        return PatientMapper.toResponse(repository.save(patient));
    }

    /** Soft-delete: marca o paciente como INACTIVE, preservando integridade referencial. */
    @Transactional
    public void deactivate(Long id) {
        Patient patient = findOrThrow(id);
        patient.setStatus(PatientStatus.INACTIVE);
        repository.save(patient);
    }

    private Patient findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
    }
}
