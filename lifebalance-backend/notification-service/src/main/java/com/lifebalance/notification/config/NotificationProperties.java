package com.lifebalance.notification.config;

import com.lifebalance.notification.domain.NotificationChannel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lifebalance.notification")
public class NotificationProperties {

    private NotificationChannel defaultChannel = NotificationChannel.IN_APP;

    public NotificationChannel getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(NotificationChannel defaultChannel) {
        this.defaultChannel = defaultChannel == null ? NotificationChannel.IN_APP : defaultChannel;
    }
}
