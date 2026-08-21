package com.lifebalance.timeline.validation;

import com.lifebalance.timeline.domain.TimelineConflictPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lifebalance.timeline.policy")
public class TimelinePolicyProperties {

    private TimelineConflictPolicy conflictPolicy = TimelineConflictPolicy.REJECT;

    public TimelineConflictPolicy getConflictPolicy() {
        return conflictPolicy;
    }

    public void setConflictPolicy(TimelineConflictPolicy conflictPolicy) {
        this.conflictPolicy = conflictPolicy == null ? TimelineConflictPolicy.REJECT : conflictPolicy;
    }
}
