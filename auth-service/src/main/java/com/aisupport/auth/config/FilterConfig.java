package com.aisupport.auth.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aisupport.auth.filter.CorrelationIdFilter;
import com.aisupport.common.constants.Correlation;

@Configuration
public class FilterConfig {

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/*");  // apply to all endpoints
        registration.setOrder(1);           // runs first before other filters
        registration.setName(Correlation.FILTER_NAME);
        return registration;
    }
}
