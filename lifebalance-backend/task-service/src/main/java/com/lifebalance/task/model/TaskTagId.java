package com.lifebalance.task.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TaskTagId implements Serializable {
   
    private UUID taskId;

    private UUID tagId;

    @Override
    public boolean equals(Object o){
         if (this == o) return true;
        if (!(o instanceof TaskTagId that)) return false;

        return Objects.equals(taskId, that.taskId)
                && Objects.equals(tagId, that.tagId);
    }
}