package com.dj.adapter.reporting.sheets.configuration;

import com.dj.adapter.reporting.sheets.service.ServiceFactory;
import com.dj.adapter.reporting.sheets.service.GoogleSheetsService;
import com.dj.adapter.reporting.sheets.service.ServiceManagerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class ReportingConfiguration {
    @Bean
    ServiceManagerFactory serviceManagerFactory() {
        return new ServiceManagerFactory();
    }

    @Bean
    ServiceFactory serviceFactory(ServiceManagerFactory serviceManagerFactory) {
        return serviceManagerFactory.getFactory();
    }

    @Bean
    GoogleSheetsService service(ServiceFactory serviceFactory) {
        return serviceFactory.getService("client_secret.json");
    }
}
