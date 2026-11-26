package com.nexoshift.petcarehub.aiassistance.application.internal.queryservices;

import com.nexoshift.petcarehub.aiassistance.domain.model.aggregates.OwnerExplanation;
import com.nexoshift.petcarehub.aiassistance.domain.model.queries.GetOwnerExplanationsByPetIdQuery;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.PetId;
import com.nexoshift.petcarehub.aiassistance.domain.model.valueobjects.UserId;
import com.nexoshift.petcarehub.aiassistance.domain.repositories.OwnerExplanationRepository;
import com.nexoshift.petcarehub.aiassistance.domain.services.OwnerExplanationQueryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OwnerExplanationQueryServiceImpl implements OwnerExplanationQueryService {

  private final OwnerExplanationRepository repository;

  public OwnerExplanationQueryServiceImpl(OwnerExplanationRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<OwnerExplanation> handle(GetOwnerExplanationsByPetIdQuery query) {
    return repository.findByPetIdAndOwnerIdOrderByIdAsc(
        new PetId(query.petId()), new UserId(query.ownerUserId()));
  }
}
