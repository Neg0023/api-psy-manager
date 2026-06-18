package com.psymanager.finance;

import com.psymanager.finance.dto.DashboardResponse;
import com.psymanager.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private FinancialEntryRepository repository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private FinanceService service;

    @Test
    void dashboardComputesSaldoLiquidoForTheMonth() {
        YearMonth month = YearMonth.of(2026, 6);
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(repository.sumByTypeInRange(EntryType.RECEITA, from, to)).thenReturn(new BigDecimal("5000.00"));
        when(repository.sumByTypeInRange(EntryType.DESPESA, from, to)).thenReturn(new BigDecimal("1800.50"));

        DashboardResponse dashboard = service.dashboard(month);

        assertThat(dashboard.totalReceitas()).isEqualByComparingTo("5000.00");
        assertThat(dashboard.totalDespesas()).isEqualByComparingTo("1800.50");
        assertThat(dashboard.saldoLiquido()).isEqualByComparingTo("3199.50");
    }
}
