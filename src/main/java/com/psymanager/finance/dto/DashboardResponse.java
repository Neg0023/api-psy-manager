package com.psymanager.finance.dto;

import java.math.BigDecimal;

/** Consolidação financeira do mês: faturamento, despesas e saldo líquido. */
public record DashboardResponse(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoLiquido) {
}
