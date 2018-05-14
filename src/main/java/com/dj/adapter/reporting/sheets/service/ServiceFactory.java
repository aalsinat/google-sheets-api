package com.dj.adapter.reporting.sheets.service;

public interface ServiceFactory {
    GoogleSheetsService getService(String credentials);
}
