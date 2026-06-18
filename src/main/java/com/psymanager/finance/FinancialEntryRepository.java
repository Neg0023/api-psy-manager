package com.psymanager.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FinancialEntryRepository
        extends JpaRepository<FinancialEntry, Long>,
                JpaSpecificationExecutor<FinancialEntry> {

    /** Soma dos valores de um tipo dentro do período (0 se vazio). */
    @Query("""
            select coalesce(sum(e.amount), 0) from FinancialEntry e
            where e.type = :type
              and e.competenceDate between :from and :to
            """)
    BigDecimal sumByTypeInRange(@Param("type") EntryType type,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);
}
