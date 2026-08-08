package com.lifebalance.task.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.task.model.Tag;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByUserIdOrderByNameAsc(UUID userId);

    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT tag
            FROM Tag tag
            WHERE tag.userId = :userId
              AND lower(trim(tag.name)) = lower(trim(:name))
            """)
    Optional<Tag> findByUserIdAndName(
            @Param("userId") UUID userId,
            @Param("name") String name
    );

    @Query("""
            SELECT CASE WHEN COUNT(tag) > 0 THEN true ELSE false END
            FROM Tag tag
            WHERE tag.userId = :userId
              AND lower(trim(tag.name)) = lower(trim(:name))
            """)
    boolean existsByUserIdAndName(
            @Param("userId") UUID userId,
            @Param("name") String name
    );

    List<Tag> findAllByIdInAndUserId(Collection<UUID> ids, UUID userId);

    @Query("""
            SELECT DISTINCT taskTag.tag
            FROM TaskTag taskTag
            WHERE taskTag.task.id = :taskId
              AND taskTag.task.userId = :userId
              AND taskTag.tag.userId = :userId
            ORDER BY taskTag.tag.name
            """)
    List<Tag> findByTaskIdAndUserId(
            @Param("taskId") UUID taskId,
            @Param("userId") UUID userId
    );
}
