package com.lifebalance.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lifebalance.task.model.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
            SELECT category
            FROM Category category
            WHERE category.ownerId = :ownerId
               OR category.ownerId IS NULL
            ORDER BY category.isSystem DESC, category.name ASC
            """)
    List<Category> findVisibleByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("""
            SELECT category
            FROM Category category
            WHERE category.id = :id
              AND (category.ownerId = :ownerId OR category.ownerId IS NULL)
            """)
    Optional<Category> findVisibleByIdAndOwnerId(
            @Param("id") UUID id,
            @Param("ownerId") UUID ownerId);

    @Query("""
            SELECT CASE WHEN COUNT(category) > 0 THEN true ELSE false END
            FROM Category category
            WHERE (category.ownerId = :ownerId OR category.ownerId IS NULL)
              AND lower(trim(category.name)) = lower(trim(:name))
            """)
    boolean existsVisibleName(
            @Param("ownerId") UUID ownerId,
            @Param("name") String name);

    @Query("""
            SELECT CASE WHEN COUNT(category) > 0 THEN true ELSE false END
            FROM Category category
            WHERE (category.ownerId = :ownerId OR category.ownerId IS NULL)
              AND lower(trim(category.name)) = lower(trim(:name))
              AND category.id <> :excludedId
            """)
    boolean existsVisibleNameExcludingId(
            @Param("ownerId") UUID ownerId,
            @Param("name") String name,
            @Param("excludedId") UUID excludedId);

    @Query("""
            SELECT CASE WHEN COUNT(category) > 0 THEN true ELSE false END
            FROM Category category
            WHERE (category.ownerId = :ownerId OR category.ownerId IS NULL)
              AND lower(category.slug) = lower(:slug)
            """)
    boolean existsVisibleSlug(
            @Param("ownerId") UUID ownerId,
            @Param("slug") String slug);

    @Query("""
            SELECT CASE WHEN COUNT(category) > 0 THEN true ELSE false END
            FROM Category category
            WHERE (category.ownerId = :ownerId OR category.ownerId IS NULL)
              AND lower(category.slug) = lower(:slug)
              AND category.id <> :excludedId
            """)
    boolean existsVisibleSlugExcludingId(
            @Param("ownerId") UUID ownerId,
            @Param("slug") String slug,
            @Param("excludedId") UUID excludedId);
}
