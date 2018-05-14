package com.dj.adapter.reporting.sheets.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "process.ame-signal.reporting")
public class AmeSignalConfiguration {
    Map<String, String> columns = new HashMap<>();

    public Map<String, String> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }
}
