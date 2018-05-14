package com.dj.adapter.reporting.sheets.service;

public class ServiceManagerFactory {

    public ServiceFactory getFactory() {
        return new GoogleSheetsServiceFactory();
    }
}
