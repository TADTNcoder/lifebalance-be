package com.lifebalance.timeline.error;

public final class TimelineErrorCode {

    public static final String TIMELINE_TASK_NOT_FOUND = "TIMELINE_TASK_NOT_FOUND";
    public static final String TIMELINE_TASK_SNAPSHOT_INVALID = "TIMELINE_TASK_SNAPSHOT_INVALID";
    public static final String TIMELINE_TASK_NOT_ELIGIBLE = "TIMELINE_TASK_NOT_ELIGIBLE";
    public static final String TIMELINE_PLACEMENT_NOT_FOUND = "TIMELINE_PLACEMENT_NOT_FOUND";
    public static final String TIMELINE_PLACEMENT_NOT_ACTIVE = "TIMELINE_PLACEMENT_NOT_ACTIVE";
    public static final String TIMELINE_INVALID_WINDOW = "TIMELINE_INVALID_WINDOW";
    public static final String TIMELINE_INVALID_CYCLE = "TIMELINE_INVALID_CYCLE";
    public static final String TIMELINE_DEADLINE_VIOLATION = "TIMELINE_DEADLINE_VIOLATION";
    public static final String TIMELINE_CONFLICT = "TIMELINE_CONFLICT";
    public static final String TIMELINE_CONFLICT_CONFIRMATION_REQUIRED = "TIMELINE_CONFLICT_CONFIRMATION_REQUIRED";
    public static final String TIMELINE_INVALID_TIMEZONE = "TIMELINE_INVALID_TIMEZONE";
    public static final String TIMELINE_TEXT_TOO_LONG = "TIMELINE_TEXT_TOO_LONG";

    private TimelineErrorCode() {
    }
}
