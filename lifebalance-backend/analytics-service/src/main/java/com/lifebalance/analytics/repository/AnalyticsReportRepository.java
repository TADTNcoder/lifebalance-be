package com.lifebalance.analytics.repository;

import com.lifebalance.analytics.domain.AnalyticsReport;
import com.lifebalance.analytics.domain.ReportDimension;
import com.lifebalance.analytics.domain.ReportStatus;
import com.lifebalance.analytics.domain.ReportType;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, UUID> {

    Optional<AnalyticsReport> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT report
            FROM AnalyticsReport report
            WHERE report.id = :reportId
              AND report.ownerId = :ownerId
            """)
    Optional<AnalyticsReport> findByIdAndOwnerIdForUpdate(
            @Param("reportId") UUID reportId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT report
            FROM AnalyticsReport report
            WHERE report.ownerId = :ownerId
              AND (:reportType IS NULL OR report.reportType = :reportType)
              AND (:dimension IS NULL OR report.dimension = :dimension)
              AND (:status IS NULL OR report.status = :status)
              AND (:periodStart IS NULL OR report.periodEnd >= :periodStart)
              AND (:periodEnd IS NULL OR report.periodStart <= :periodEnd)
            ORDER BY report.generatedAt DESC, report.id DESC
            """)
    Page<AnalyticsReport> search(
            @Param("ownerId") UUID ownerId,
            @Param("reportType") ReportType reportType,
            @Param("dimension") ReportDimension dimension,
            @Param("status") ReportStatus status,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            Pageable pageable
    );
}
