package com.psymanager.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    /**
     * Lista pacientes com filtros opcionais por status e por texto (nome ou CPF).
     * O status nulo é ignorado. A busca textual ({@code q}) deve chegar como string
     * vazia quando não houver filtro — assim {@code like '%%'} casa com todos os
     * registros, evitando passar {@code null} (que o PostgreSQL não consegue tipar
     * dentro de {@code lower(concat(...))}).
     */
    @Query("""
            select p from Patient p
            where (:status is null or p.status = :status)
              and (lower(p.fullName) like lower(concat('%', :q, '%'))
                   or p.cpf like concat('%', :q, '%'))
            """)
    Page<Patient> search(@Param("status") PatientStatus status,
                         @Param("q") String q,
                         Pageable pageable);
}
