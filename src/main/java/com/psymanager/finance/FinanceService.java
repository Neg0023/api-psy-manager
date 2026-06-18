package com.psymanager.finance;

import com.psymanager.common.exception.ResourceNotFoundException;
import com.psymanager.finance.dto.DashboardResponse;
import com.psymanager.finance.dto.FinancialEntryRequest;
import com.psymanager.finance.dto.FinancialEntryResponse;
import com.psymanager.patient.PatientRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class FinanceService {

    private final FinancialEntryRepository repository;
    private final PatientRepository patientRepository;

    public FinanceService(FinancialEntryRepository repository, PatientRepository patientRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialEntryResponse> list(LocalDate from, LocalDate to, EntryType type, EntryStatus status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "competenceDate").and(Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(FinancialEntrySpec.filter(from, to, type, status), sort).stream()
                .map(FinancialEntryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        BigDecimal receitas = repository.sumByTypeInRange(EntryType.RECEITA, from, to);
        BigDecimal despesas = repository.sumByTypeInRange(EntryType.DESPESA, from, to);
        return new DashboardResponse(receitas, despesas, receitas.subtract(despesas));
    }

    @Transactional
    public FinancialEntryResponse create(FinancialEntryRequest request) {
        FinancialEntry entry = new FinancialEntry();
        applyRequest(entry, request);
        return FinancialEntryMapper.toResponse(repository.save(entry));
    }

    @Transactional
    public FinancialEntryResponse update(Long id, FinancialEntryRequest request) {
        FinancialEntry entry = findOrThrow(id);
        applyRequest(entry, request);
        return FinancialEntryMapper.toResponse(repository.save(entry));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    private void applyRequest(FinancialEntry entry, FinancialEntryRequest request) {
        entry.setDescription(request.description());
        entry.setAmount(request.amount());
        entry.setCompetenceDate(request.competenceDate());
        entry.setType(request.type());
        entry.setStatus(request.status());
        if (request.patientId() != null) {
            entry.setPatient(patientRepository.findById(request.patientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Paciente não encontrado: " + request.patientId())));
        } else {
            entry.setPatient(null);
        }
    }

    private FinancialEntry findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado: " + id));
    }
}
