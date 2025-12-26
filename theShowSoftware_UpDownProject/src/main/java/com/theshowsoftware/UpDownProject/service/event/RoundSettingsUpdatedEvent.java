package com.theshowsoftware.UpDownProject.service.event;

import org.springframework.context.ApplicationEvent;

public class RoundSettingsUpdatedEvent extends ApplicationEvent {
    public RoundSettingsUpdatedEvent(Object source) {
        super(source);
    }
}