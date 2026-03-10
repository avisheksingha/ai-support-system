package com.aisupport.common.config;

import java.util.TimeZone;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * GlobalTimezoneConfig is a Spring component that sets the default timezone for the application to UTC.
 * This ensures that all date and time operations across the application are consistent and based on UTC.
 */
@Component
public class GlobalTimezoneConfig {
	
	@PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
