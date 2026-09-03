package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.ActualRecordStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ActualRecordAggregateRepositoryImpl implements ActualRecordAggregateRepository {

    private final EntityManager entityManager;

    public ActualRecordAggregateRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Long sumActualMinutes(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            LocalDate from,
            LocalDate to
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<ActualRecord> record = query.from(ActualRecord.class);
        query.select(criteriaBuilder.sumAsLong(record.get("actualMinutes")));
        query.where(predicates(
                criteriaBuilder,
                record,
                ownerId,
                taskId,
                capitalCycleId,
                null,
                from,
                to
        ));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public BigDecimal sumActualCost(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            String currencyCode,
            LocalDate from,
            LocalDate to
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = criteriaBuilder.createQuery(BigDecimal.class);
        Root<ActualRecord> record = query.from(ActualRecord.class);
        query.select(criteriaBuilder.sum(record.get("actualCost")));
        query.where(predicates(
                criteriaBuilder,
                record,
                ownerId,
                taskId,
                capitalCycleId,
                currencyCode,
                from,
                to
        ));
        return entityManager.createQuery(query).getSingleResult();
    }

    private static Predicate[] predicates(
            CriteriaBuilder criteriaBuilder,
            Root<ActualRecord> record,
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            String currencyCode,
            LocalDate from,
            LocalDate to
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(record.get("ownerId"), ownerId));
        predicates.add(criteriaBuilder.equal(record.get("status"), ActualRecordStatus.ACTIVE));

        if (taskId != null) {
            predicates.add(criteriaBuilder.equal(record.get("taskId"), taskId));
        }
        if (capitalCycleId != null) {
            predicates.add(criteriaBuilder.equal(record.get("capitalCycleId"), capitalCycleId));
        }
        if (currencyCode != null) {
            predicates.add(criteriaBuilder.equal(record.get("currencyCode"), currencyCode));
        }
        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(record.get("actualDate"), from));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(record.get("actualDate"), to));
        }

        return predicates.toArray(Predicate[]::new);
    }
}
