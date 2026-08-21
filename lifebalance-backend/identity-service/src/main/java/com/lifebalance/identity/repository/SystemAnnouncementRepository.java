package com.lifebalance.identity.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.identity.model.SystemAnnouncement;
import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;

public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, UUID> {

    @Query(value = """
            SELECT announcement
            FROM SystemAnnouncement announcement
            LEFT JOIN FETCH announcement.publishedBy
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND (:startsFrom IS NULL OR announcement.startsAt >= :startsFrom)
              AND (:startsTo IS NULL OR announcement.startsAt <= :startsTo)
              AND (:keyword IS NULL
                   OR lower(announcement.title) LIKE :keyword
                   OR lower(announcement.message) LIKE :keyword)
            """,
            countQuery = """
            SELECT count(announcement)
            FROM SystemAnnouncement announcement
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND (:startsFrom IS NULL OR announcement.startsAt >= :startsFrom)
              AND (:startsTo IS NULL OR announcement.startsAt <= :startsTo)
              AND (:keyword IS NULL
                   OR lower(announcement.title) LIKE :keyword
                   OR lower(announcement.message) LIKE :keyword)
            """)
    Page<SystemAnnouncement> search(
            @Param("status") AnnouncementStatus status,
            @Param("audience") AnnouncementAudience audience,
            @Param("startsFrom") OffsetDateTime startsFrom,
            @Param("startsTo") OffsetDateTime startsTo,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT announcement
            FROM SystemAnnouncement announcement
            WHERE announcement.status IN :statuses
              AND announcement.startsAt <= :now
              AND (announcement.endsAt IS NULL OR announcement.endsAt >= :now)
            ORDER BY announcement.startsAt DESC
            """)
    List<SystemAnnouncement> findActiveAt(
            @Param("statuses") List<AnnouncementStatus> statuses,
            @Param("now") OffsetDateTime now
    );
}
