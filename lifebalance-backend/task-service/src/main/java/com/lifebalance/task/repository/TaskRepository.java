package com.lifebalance.task.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND lower(trim(task.name)) = lower(trim(:name))
                        """)
        Optional<Task> findByNameAndOwnerId(
                        @Param("name") String name,
                        @Param("ownerId") UUID ownerId);

        /**
         * Returns every task with the given name for an owner. Task names are
         * unique only within the same time slot, so callers must inspect the
         * planning/deadline fields before rejecting a duplicate.
         */
        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND lower(trim(task.name)) = lower(trim(:name))
                        """)
        List<Task> findAllByNameAndOwnerId(
                        @Param("name") String name,
                        @Param("ownerId") UUID ownerId);

        Optional<Task> findByIdAndOwnerId(
                        UUID id,
                        UUID ownerId);

        @Query("""
                        SELECT CASE WHEN COUNT(task) > 0 THEN true ELSE false END
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND lower(trim(task.name)) = lower(trim(:name))
                        """)
        boolean existsByNameAndOwnerId(
                        @Param("name") String name,
                        @Param("ownerId") UUID ownerId);

        Page<Task> findByOwnerIdAndNameContainingIgnoreCase(
                        UUID ownerId,
                        String keyword,
                        Pageable pageable);

        Page<Task> findByOwnerIdAndStatus(
                        UUID ownerId,
                        TaskStatus status,
                        Pageable pageable);

        Page<Task> findByOwnerIdAndPriority(
                UUID ownerId,
                PriorityLevel priority,
                Pageable pageable);

        /**
         * Serializes metadata updates for every occurrence in the same monthly
         * income group so concurrent edits cannot split a salary chain.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND task.monthlyIncomeGroupId = (
                                SELECT target.monthlyIncomeGroupId
                                FROM Task target
                                WHERE target.id = :taskId
                                  AND target.ownerId = :ownerId
                          )
                        ORDER BY task.id
                        """)
        List<Task> lockMonthlyIncomeGroupForTask(
                @Param("taskId") UUID taskId,
                @Param("ownerId") UUID ownerId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND task.monthlyIncomeGroupId = :monthlyIncomeGroupId
                        ORDER BY task.id
                        """)
        List<Task> lockMonthlyIncomeGroup(
                @Param("ownerId") UUID ownerId,
                @Param("monthlyIncomeGroupId") UUID monthlyIncomeGroupId);

        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE task.ownerId = :ownerId
                          AND (
                                :keyword IS NULL
                                OR :keyword = ''
                                OR lower(task.name) LIKE lower(concat('%', :keyword, '%'))
                                OR lower(coalesce(task.description, '')) LIKE lower(concat('%', :keyword, '%'))
                          )
                          AND (:status IS NULL OR task.status = :status)
                          AND (:priority IS NULL OR task.priority = :priority)
                          AND (:categoryId IS NULL OR task.category.id = :categoryId)
                          AND (
                                (task.deadline IS NULL AND COALESCE(:deadlineFrom, task.deadline) IS NULL)
                                OR task.deadline >= COALESCE(:deadlineFrom, task.deadline)
                          )
                          AND (
                                (task.deadline IS NULL AND COALESCE(:deadlineTo, task.deadline) IS NULL)
                                OR task.deadline <= COALESCE(:deadlineTo, task.deadline)
                          )
                        """)
        Page<Task> searchByOwnerAndFilters(
                        @Param("ownerId") UUID ownerId,
                        @Param("keyword") String keyword,
                        @Param("status") TaskStatus status,
                        @Param("priority") PriorityLevel priority,
                        @Param("categoryId") UUID categoryId,
                        @Param("deadlineFrom") LocalDate deadlineFrom,
                        @Param("deadlineTo") LocalDate deadlineTo,
                        Pageable pageable);

        @Query("""
                        SELECT task
                        FROM Task task
                        WHERE (
                                :keyword IS NULL
                                OR :keyword = ''
                                OR lower(task.name) LIKE lower(concat('%', :keyword, '%'))
                                OR lower(coalesce(task.description, '')) LIKE lower(concat('%', :keyword, '%'))
                          )
                          AND (:status IS NULL OR task.status = :status)
                          AND (:priority IS NULL OR task.priority = :priority)
                          AND (:categoryId IS NULL OR task.category.id = :categoryId)
                          AND (
                                (task.deadline IS NULL AND COALESCE(:deadlineFrom, task.deadline) IS NULL)
                                OR task.deadline >= COALESCE(:deadlineFrom, task.deadline)
                          )
                          AND (
                                (task.deadline IS NULL AND COALESCE(:deadlineTo, task.deadline) IS NULL)
                                OR task.deadline <= COALESCE(:deadlineTo, task.deadline)
                          )
                        """)
        Page<Task> searchAllByFilters(
                        @Param("keyword") String keyword,
                        @Param("status") TaskStatus status,
                        @Param("priority") PriorityLevel priority,
                        @Param("categoryId") UUID categoryId,
                        @Param("deadlineFrom") LocalDate deadlineFrom,
                        @Param("deadlineTo") LocalDate deadlineTo,
                        Pageable pageable);
}
