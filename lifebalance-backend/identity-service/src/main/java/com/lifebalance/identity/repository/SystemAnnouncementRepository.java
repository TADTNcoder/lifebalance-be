package com.lifebalance.identity.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Query("""
            SELECT announcement
            FROM SystemAnnouncement announcement
            LEFT JOIN FETCH announcement.publishedBy
            WHERE announcement.id = :id
            """)
    Optional<SystemAnnouncement> findDetailById(@Param("id") UUID id);

    @Query(value = """
            SELECT announcement
            FROM SystemAnnouncement announcement
            LEFT JOIN FETCH announcement.publishedBy
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND announcement.startsAt >= COALESCE(:startsFrom, announcement.startsAt)
              AND announcement.startsAt <= COALESCE(:startsTo, announcement.startsAt)
              AND (:keyword IS NULL
                   OR lower(announcement.title) LIKE :keyword
                   OR lower(announcement.message) LIKE :keyword)
            """,
            countQuery = """
            SELECT count(announcement)
            FROM SystemAnnouncement announcement
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND announcement.startsAt >= COALESCE(:startsFrom, announcement.startsAt)
              AND announcement.startsAt <= COALESCE(:startsTo, announcement.startsAt)
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

    @Query(value = """
            SELECT announcement
            FROM SystemAnnouncement announcement
            LEFT JOIN FETCH announcement.publishedBy
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND announcement.audience IN :allowedAudiences
              AND announcement.startsAt >= COALESCE(:startsFrom, announcement.startsAt)
              AND announcement.startsAt <= COALESCE(:startsTo, announcement.startsAt)
              AND (:keyword IS NULL
                   OR lower(announcement.title) LIKE :keyword
                   OR lower(announcement.message) LIKE :keyword)
            """,
            countQuery = """
            SELECT count(announcement)
            FROM SystemAnnouncement announcement
            WHERE (:status IS NULL OR announcement.status = :status)
              AND (:audience IS NULL OR announcement.audience = :audience)
              AND announcement.audience IN :allowedAudiences
              AND announcement.startsAt >= COALESCE(:startsFrom, announcement.startsAt)
              AND announcement.startsAt <= COALESCE(:startsTo, announcement.startsAt)
              AND (:keyword IS NULL
                   OR lower(announcement.title) LIKE :keyword
                   OR lower(announcement.message) LIKE :keyword)
            """)
    Page<SystemAnnouncement> searchVisible(
            @Param("status") AnnouncementStatus status,
            @Param("audience") AnnouncementAudience audience,
            @Param("allowedAudiences") Collection<AnnouncementAudience> allowedAudiences,
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

    @Query("""
            SELECT announcement.status, count(announcement)
            FROM SystemAnnouncement announcement
            WHERE announcement.createdAt >= COALESCE(:createdFrom, announcement.createdAt)
              AND announcement.createdAt <= COALESCE(:createdTo, announcement.createdAt)
            GROUP BY announcement.status
            """)
    List<Object[]> countByStatus(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );

    @Query("""
            SELECT announcement.audience, count(announcement)
            FROM SystemAnnouncement announcement
            WHERE announcement.createdAt >= COALESCE(:createdFrom, announcement.createdAt)
              AND announcement.createdAt <= COALESCE(:createdTo, announcement.createdAt)
            GROUP BY announcement.audience
            """)
    List<Object[]> countByAudience(
            @Param("createdFrom") OffsetDateTime createdFrom,
            @Param("createdTo") OffsetDateTime createdTo
    );
}
