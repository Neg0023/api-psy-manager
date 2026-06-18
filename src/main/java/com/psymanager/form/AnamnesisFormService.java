package com.psymanager.form;

import com.google.api.services.forms.v1.Forms;
import com.google.api.services.forms.v1.model.BatchUpdateFormRequest;
import com.google.api.services.forms.v1.model.ChoiceQuestion;
import com.google.api.services.forms.v1.model.CreateItemRequest;
import com.google.api.services.forms.v1.model.Form;
import com.google.api.services.forms.v1.model.Info;
import com.google.api.services.forms.v1.model.Item;
import com.google.api.services.forms.v1.model.Location;
import com.google.api.services.forms.v1.model.Option;
import com.google.api.services.forms.v1.model.Question;
import com.google.api.services.forms.v1.model.QuestionItem;
import com.google.api.services.forms.v1.model.Request;
import com.google.api.services.forms.v1.model.TextQuestion;
import com.psymanager.common.exception.BadRequestException;
import com.psymanager.common.exception.IntegrationException;
import com.psymanager.common.exception.ResourceNotFoundException;
import com.psymanager.form.dto.AnamnesisFormResponse;
import com.psymanager.form.dto.CreateFormRequest;
import com.psymanager.form.dto.FormQuestionDto;
import com.psymanager.form.dto.FormQuestionType;
import com.psymanager.patient.Patient;
import com.psymanager.patient.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnamnesisFormService {

    private final GoogleOAuthService googleOAuthService;
    private final AnamnesisFormRepository formRepository;
    private final PatientRepository patientRepository;

    public AnamnesisFormService(GoogleOAuthService googleOAuthService,
                                AnamnesisFormRepository formRepository,
                                PatientRepository patientRepository) {
        this.googleOAuthService = googleOAuthService;
        this.formRepository = formRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public AnamnesisFormResponse createForm(CreateFormRequest request) {
        Patient patient = resolvePatient(request.patientId());
        validateQuestions(request.questions());

        Forms forms = googleOAuthService.formsClient();
        try {
            Form form = new Form().setInfo(new Info()
                    .setTitle(request.title())
                    .setDocumentTitle(request.title()));
            Form created = forms.forms().create(form).execute();
            String formId = created.getFormId();

            List<Request> requests = buildItemRequests(request.questions());
            if (!requests.isEmpty()) {
                forms.forms()
                        .batchUpdate(formId, new BatchUpdateFormRequest().setRequests(requests))
                        .execute();
            }

            String shareUrl = created.getResponderUri();
            if (shareUrl == null || shareUrl.isBlank()) {
                shareUrl = "https://docs.google.com/forms/d/" + formId + "/viewform";
            }

            AnamnesisForm entity = new AnamnesisForm();
            entity.setPatient(patient);
            entity.setGoogleFormId(formId);
            entity.setShareUrl(shareUrl);
            entity.setTitle(request.title());
            return toResponse(formRepository.save(entity));
        } catch (IOException e) {
            throw new IntegrationException("Falha ao criar o formulário no Google Forms", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AnamnesisFormResponse> list(Long patientId) {
        List<AnamnesisForm> forms = (patientId != null)
                ? formRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                : formRepository.findAllByOrderByCreatedAtDesc();
        return forms.stream().map(this::toResponse).toList();
    }

    private Patient resolvePatient(Long patientId) {
        if (patientId == null) {
            return null;
        }
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + patientId));
    }

    private void validateQuestions(List<FormQuestionDto> questions) {
        for (FormQuestionDto question : questions) {
            if (question.type() == FormQuestionType.MULTIPLE_CHOICE) {
                long validOptions = question.options() == null ? 0
                        : question.options().stream().filter(o -> o != null && !o.isBlank()).count();
                if (validOptions < 2) {
                    throw new BadRequestException(
                            "Perguntas de múltipla escolha precisam de ao menos 2 opções: " + question.text());
                }
            }
        }
    }

    private List<Request> buildItemRequests(List<FormQuestionDto> questions) {
        List<Request> requests = new ArrayList<>();
        int index = 0;
        for (FormQuestionDto dto : questions) {
            Question question = new Question();
            switch (dto.type()) {
                case SHORT_TEXT -> question.setTextQuestion(new TextQuestion().setParagraph(false));
                case LONG_TEXT -> question.setTextQuestion(new TextQuestion().setParagraph(true));
                case MULTIPLE_CHOICE -> {
                    List<Option> options = dto.options().stream()
                            .filter(o -> o != null && !o.isBlank())
                            .map(o -> new Option().setValue(o))
                            .toList();
                    question.setChoiceQuestion(new ChoiceQuestion().setType("RADIO").setOptions(options));
                }
            }
            Item item = new Item()
                    .setTitle(dto.text())
                    .setQuestionItem(new QuestionItem().setQuestion(question));
            requests.add(new Request().setCreateItem(
                    new CreateItemRequest().setItem(item).setLocation(new Location().setIndex(index++))));
        }
        return requests;
    }

    private AnamnesisFormResponse toResponse(AnamnesisForm form) {
        Patient patient = form.getPatient();
        return new AnamnesisFormResponse(
                form.getId(),
                patient != null ? patient.getId() : null,
                patient != null ? patient.getFullName() : null,
                form.getGoogleFormId(),
                form.getShareUrl(),
                form.getTitle(),
                form.getCreatedAt());
    }
}
