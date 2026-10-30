package com.nexoshift.petcarehub.clinicalexams.infrastructure.persistence.jpa.repositories;

import com.nexoshift.petcarehub.clinicalexams.domain.model.aggregates.ClinicalExam;
import com.nexoshift.petcarehub.clinicalexams.domain.repositories.ClinicalExamRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalExamJpaRepository
        extends JpaRepository<ClinicalExam, Long>, ClinicalExamRepository {
}