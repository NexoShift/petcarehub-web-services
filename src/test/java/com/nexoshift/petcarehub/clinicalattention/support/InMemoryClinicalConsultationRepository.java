package com.nexoshift.petcarehub.clinicalattention.support;

import com.nexoshift.petcarehub.clinicalattention.domain.model.aggregates.ClinicalConsultation;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.clinicalattention.domain.model.valueobjects.VeterinarianId;
import com.nexoshift.petcarehub.clinicalattention.domain.repositories.ClinicalConsultationRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for unit tests. Like Spring Data it gives ids to the aggregate and to the records
 * inside it, and it "publishes" the domain events registered in the aggregate on every save.
 */
public class InMemoryClinicalConsultationRepository implements ClinicalConsultationRepository {

  private final Map<Long, ClinicalConsultation> store = new LinkedHashMap<>();
  private final List<Object> publishedEvents = new ArrayList<>();
  private long consultationSequence = 0;
  private long recordSequence = 0;
  private int saveCount = 0;

  @Override
  public ClinicalConsultation save(ClinicalConsultation consultation) {
    saveCount++;
    if (consultation.getId() == null) {
      ClinicalTestSupport.withId(consultation, ++consultationSequence);
    }
    assignRecordIds(consultation);
    publishedEvents.addAll(ClinicalTestSupport.domainEvents(consultation));
    ClinicalTestSupport.clearDomainEvents(consultation);
    store.put(consultation.getId(), consultation);
    return consultation;
  }

  @Override
  public Optional<ClinicalConsultation> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<ClinicalConsultation> findAllByPetIdOrderByCreatedAtDesc(PetId petId) {
    return store.values().stream()
        .filter(consultation -> consultation.getPetId().equals(petId))
        .sorted(Comparator.comparing(ClinicalConsultation::getId).reversed())
        .toList();
  }

  @Override
  public List<ClinicalConsultation> findAllByVeterinarianIdOrderByCreatedAtDesc(
      VeterinarianId veterinarianId) {
    return store.values().stream()
        .filter(consultation -> consultation.getVeterinarianId().equals(veterinarianId))
        .sorted(Comparator.comparing(ClinicalConsultation::getId).reversed())
        .toList();
  }

  public List<Object> publishedEvents() {
    return List.copyOf(publishedEvents);
  }

  public <E> List<E> publishedEvents(Class<E> type) {
    return publishedEvents.stream().filter(type::isInstance).map(type::cast).toList();
  }

  public int saveCount() {
    return saveCount;
  }

  public int size() {
    return store.size();
  }

  private void assignRecordIds(ClinicalConsultation consultation) {
    if (consultation.getAnamnesis() != null && consultation.getAnamnesis().getId() == null) {
      ClinicalTestSupport.withId(consultation.getAnamnesis(), ++recordSequence);
    }
    consultation.getPhysiologicalAssessments().stream()
        .filter(record -> record.getId() == null)
        .forEach(record -> ClinicalTestSupport.withId(record, ++recordSequence));
    consultation.getClinicalFindings().stream()
        .filter(record -> record.getId() == null)
        .forEach(record -> ClinicalTestSupport.withId(record, ++recordSequence));
    consultation.getDiagnoses().stream()
        .filter(record -> record.getId() == null)
        .forEach(record -> ClinicalTestSupport.withId(record, ++recordSequence));
  }
}
