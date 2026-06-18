package com.psymanager.finance;

import com.psymanager.finance.dto.DashboardResponse;
import com.psymanager.finance.dto.FinancialEntryRequest;
import com.psymanager.finance.dto.FinancialEntryResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @GetMapping("/entries")
    public List<FinancialEntryResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EntryType type,
            @RequestParam(required = false) EntryStatus status) {
        return service.list(from, to, type, status);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestParam(required = false) String month) {
        YearMonth yearMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
        return service.dashboard(yearMonth);
    }

    @PostMapping("/entries")
    public ResponseEntity<FinancialEntryResponse> create(@Valid @RequestBody FinancialEntryRequest request) {
        FinancialEntryResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/finance/entries/" + created.id())).body(created);
    }

    @PutMapping("/entries/{id}")
    public FinancialEntryResponse update(@PathVariable Long id, @Valid @RequestBody FinancialEntryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
