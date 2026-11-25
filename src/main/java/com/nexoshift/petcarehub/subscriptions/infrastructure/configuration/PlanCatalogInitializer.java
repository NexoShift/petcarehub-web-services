package com.nexoshift.petcarehub.subscriptions.infrastructure.configuration;

import com.nexoshift.petcarehub.subscriptions.domain.model.aggregates.Plan;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanCode;
import com.nexoshift.petcarehub.subscriptions.domain.model.valueobjects.PlanLimits;
import com.nexoshift.petcarehub.subscriptions.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Initializes the default subscription plans.
 */
@Component
public class PlanCatalogInitializer implements ApplicationRunner {

    private final PlanRepository planRepository;

    public PlanCatalogInitializer(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Registers the default plans if they do not exist.
     *
     * @param args the application arguments
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        saveIfMissing(new Plan(
                PlanCode.PET_OWNER_BASIC,
                "Pet Owner Basic",
                new BigDecimal("19.90"),
                new PlanLimits(2, null, 15, 50)
        ));

        saveIfMissing(new Plan(
                PlanCode.PET_OWNER_PREMIUM,
                "Pet Owner Premium",
                new BigDecimal("29.90"),
                new PlanLimits(6, null, 40, 150)
        ));

        saveIfMissing(new Plan(
                PlanCode.CLINIC_BASIC,
                "Clinic Basic",
                new BigDecimal("400.00"),
                new PlanLimits(null, 4, 100, 300)
        ));

        saveIfMissing(new Plan(
                PlanCode.CLINIC_PREMIUM,
                "Clinic Premium",
                new BigDecimal("900.00"),
                new PlanLimits(null, 10, 300, 1000)
        ));
    }

    /**
     * Persists a plan only when its code does not exist.
     *
     * @param plan the subscription plan
     */
    private void saveIfMissing(Plan plan) {

        if (planRepository.existsByCode(plan.getCode())) {
            return;
        }

        planRepository.save(plan);
    }
}