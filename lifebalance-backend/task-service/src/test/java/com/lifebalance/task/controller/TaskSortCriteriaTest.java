package com.lifebalance.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.error.TaskErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class TaskSortCriteriaTest {

    @Test
    void mapsApprovedSortAliasesToEntityProperties() {
        Sort sort = TaskSortCriteria.toSort("scheduledTime", "asc");

        Sort.Order order = sort.getOrderFor("scheduledStartAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void defaultsBlankSortToCreatedAtDescending() {
        Sort sort = TaskSortCriteria.toSort(" ", null);

        Sort.Order order = sort.getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void rejectsUnknownSortField() {
        assertThatThrownBy(() -> TaskSortCriteria.toSort("deletedAt", "DESC"))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_INVALID_SORT_CRITERIA);
    }

    @Test
    void rejectsUnknownSortDirection() {
        assertThatThrownBy(() -> TaskSortCriteria.toSort("deadline", "sideways"))
                .isInstanceOf(AppException.class)
                .extracting("code")
                .isEqualTo(TaskErrorCode.TASK_INVALID_SORT_CRITERIA);
    }
}
