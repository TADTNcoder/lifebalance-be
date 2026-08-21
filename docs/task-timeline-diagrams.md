# Task & Timeline Diagrams

These diagrams document the Task & Timeline Management behavior around
scheduling, rescheduling, drag-drop movement, and lifecycle transitions. They
are intentionally policy-aware: validation and conflict failure paths return an
error response without changing Task state, Timeline placement, planning
association, or history.

## Schedule Task Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Client
    participant TaskAPI as Task API
    participant TaskService as Task Service
    participant Planning as Planning/Capital Validator
    participant Timeline as Timeline Service
    participant Conflict as Conflict Validator
    participant History as Task History
    participant Store as Database

    User->>Client: Choose planned task and target time
    Client->>TaskAPI: POST /tasks/{taskId}/schedule
    TaskAPI->>TaskService: schedule(ownerId, taskId, window, timezone)
    TaskService->>Store: Load Task by id + owner
    Store-->>TaskService: Task snapshot

    alt Task missing or not owned
        TaskService-->>TaskAPI: NotFound/Forbidden
        TaskAPI-->>Client: Failure, no state changed
    else Task found
        TaskService->>TaskService: Validate status allows scheduling
        TaskService->>TaskService: Validate deadline and estimatedMinutes > 0
        TaskService->>Planning: Validate Time Capital or approved allowance

        alt Planning validation fails
            Planning-->>TaskService: Validation error
            TaskService-->>TaskAPI: Failure, no state changed
            TaskAPI-->>Client: Validation message
        else Planning valid
            TaskService->>Conflict: Check owner timeline overlap and policy

            alt Conflict detected and policy rejects
                Conflict-->>TaskService: Conflict rejection
                TaskService-->>TaskAPI: Failure, no state changed
                TaskAPI-->>Client: Conflict message
            else No blocking conflict
                TaskService->>Timeline: Create active placement
                Timeline->>Store: Insert timeline placement
                Timeline-->>TaskService: Placement created
                TaskService->>Store: Update Task status to SCHEDULED
                TaskService->>History: Append TASK_SCHEDULED event
                History->>Store: Insert history event
                TaskService-->>TaskAPI: Scheduled Task + placement
                TaskAPI-->>Client: Success
            end
        end
    end
```

## Reschedule Task Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Client
    participant TaskAPI as Task API
    participant TaskService as Task Service
    participant Timeline as Timeline Service
    participant Conflict as Conflict Validator
    participant History as Task History
    participant Store as Database

    User->>Client: Edit scheduled time
    Client->>TaskAPI: PATCH /tasks/{taskId}/timeline-placement
    TaskAPI->>TaskService: reschedule(ownerId, taskId, newWindow, reason)
    TaskService->>Store: Load Task and active placement by owner
    Store-->>TaskService: Task + current placement snapshot

    alt Task or active placement missing
        TaskService-->>TaskAPI: NotFound/InvalidState
        TaskAPI-->>Client: Failure, no state changed
    else Current schedule exists
        TaskService->>TaskService: Validate status allows reschedule
        TaskService->>TaskService: Validate new window and deadline
        TaskService->>Conflict: Exclude current placement and check new window

        alt Conflict detected and policy rejects
            Conflict-->>TaskService: Conflict rejection
            TaskService-->>TaskAPI: Failure, no state changed
            TaskAPI-->>Client: Conflict message with current schedule
        else New window valid
            TaskService->>Timeline: Update active placement window
            Timeline->>Store: Persist placement before/after window
            Timeline-->>TaskService: Placement updated
            TaskService->>History: Append TASK_RESCHEDULED event with before/after
            History->>Store: Insert history event
            TaskService-->>TaskAPI: Updated placement
            TaskAPI-->>Client: Success
        end
    end
```

## Drag-Drop Timeline Sequence

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Client
    participant TimelineAPI as Timeline API
    participant TimelineService as Timeline Service
    participant TaskService as Task Service
    participant Conflict as Conflict Validator
    participant History as Task History
    participant Store as Database

    User->>Client: Drag placement to new time slot
    Client->>TimelineAPI: PATCH /timeline/placements/{placementId}/move
    TimelineAPI->>TimelineService: move(ownerId, placementId, targetWindow)
    TimelineService->>Store: Load placement + Task by owner
    Store-->>TimelineService: Placement and Task snapshot

    alt Placement missing, not owned, or inactive
        TimelineService-->>TimelineAPI: NotFound/InvalidState
        TimelineAPI-->>Client: Failure, snap item back
    else Placement movable
        TimelineService->>TaskService: Validate Task state for timeline move
        TaskService-->>TimelineService: State validation result

        alt Task state rejects movement
            TimelineService-->>TimelineAPI: Failure, no state changed
            TimelineAPI-->>Client: Restore previous position
        else Task state allows movement
            TimelineService->>Conflict: Validate target slot, deadline, overlap

            alt Conflict detected and policy rejects
                Conflict-->>TimelineService: Conflict rejection
                TimelineService-->>TimelineAPI: Failure, no state changed
                TimelineAPI-->>Client: Restore previous position + conflict detail
            else Move accepted
                TimelineService->>Store: Update placement window
                TimelineService->>History: Append TIMELINE_PLACEMENT_MOVED event
                History->>Store: Insert history event
                TimelineService-->>TimelineAPI: Updated placement
                TimelineAPI-->>Client: Keep item in new position
            end
        end
    end
```

## Task Lifecycle Activity

```mermaid
flowchart TD
    Start([Action requested])
    Load[Load Task by taskId and ownerId]
    Exists{Task exists and owner matches?}
    Action{Requested action}
    ValidateState{Current state allows action?}
    ValidatePlanUpdate{Planning update valid?}
    ValidateScheduleReady{Planning ready for schedule?}
    ValidateSchedule{Schedule window valid?}
    ValidateConflict{Timeline conflict allowed?}
    Persist[Persist Task/Timeline change]
    History[Append history event]
    Success([Return success])
    Failure([Return failure and keep previous state])

    Draft[Draft]
    Planned[Planned]
    Scheduled[Scheduled]
    InProgress[In Progress]
    OnHold[On Hold]
    Completed[Completed]
    Cancelled[Cancelled]
    Archived[Archived]

    Start --> Load --> Exists
    Exists -- No --> Failure
    Exists -- Yes --> Action
    Action --> ValidateState
    ValidateState -- No --> Failure

    ValidateState -- Plan / Update planning --> ValidatePlanUpdate
    ValidatePlanUpdate -- Invalid --> Failure
    ValidatePlanUpdate -- Valid --> Planned

    ValidateState -- Schedule / Reschedule / Drag-Drop --> ValidateScheduleReady
    ValidateScheduleReady -- Missing required planning --> Failure
    ValidateScheduleReady -- Ready --> ValidateSchedule
    ValidateSchedule -- Invalid window or deadline --> Failure
    ValidateSchedule -- Valid window --> ValidateConflict
    ValidateConflict -- Blocking conflict --> Failure
    ValidateConflict -- No blocking conflict --> Scheduled

    ValidateState -- Start --> InProgress
    ValidateState -- Pause --> OnHold
    ValidateState -- Resume --> InProgress
    ValidateState -- Complete --> Completed
    ValidateState -- Cancel --> Cancelled
    ValidateState -- Archive --> Archived
    ValidateState -- Restore --> Planned
    ValidateState -- Reopen --> Planned

    Draft --> Persist
    Planned --> Persist
    Scheduled --> Persist
    InProgress --> Persist
    OnHold --> Persist
    Completed --> Persist
    Cancelled --> Persist
    Archived --> Persist

    Persist --> History --> Success
```

## Lifecycle State Map

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Create Task
    DRAFT --> PLANNED: Plan valid
    DRAFT --> CANCELLED: Cancel allowed
    DRAFT --> ARCHIVED: Archive allowed

    PLANNED --> SCHEDULED: Schedule valid + no blocking conflict
    PLANNED --> IN_PROGRESS: Start allowed
    PLANNED --> CANCELLED: Cancel allowed
    PLANNED --> ARCHIVED: Archive allowed
    PLANNED --> DRAFT: Move back if policy allows

    SCHEDULED --> SCHEDULED: Reschedule/Drag-drop valid
    SCHEDULED --> IN_PROGRESS: Start
    SCHEDULED --> PLANNED: Remove placement or unschedule
    SCHEDULED --> CANCELLED: Cancel allowed

    IN_PROGRESS --> ON_HOLD: Pause
    IN_PROGRESS --> COMPLETED: Complete valid
    IN_PROGRESS --> CANCELLED: Cancel allowed

    ON_HOLD --> IN_PROGRESS: Resume
    ON_HOLD --> PLANNED: Move back to planning
    ON_HOLD --> COMPLETED: Complete valid
    ON_HOLD --> CANCELLED: Cancel allowed

    COMPLETED --> PLANNED: Reopen if policy allows
    COMPLETED --> ARCHIVED: Archive

    CANCELLED --> PLANNED: Reopen if policy allows
    CANCELLED --> ARCHIVED: Archive

    ARCHIVED --> PLANNED: Restore

    note right of SCHEDULED
        Schedule, reschedule, and drag-drop require:
        owner validation, allowed Task state,
        valid window/deadline, planning readiness,
        Time Capital or approved policy allowance,
        and no blocking conflict.
    end note

    note left of DRAFT
        Any validation, ownership, policy, or conflict
        failure returns an error and keeps the previous
        Task and Timeline state unchanged.
    end note
```
