package com.dj.adapter.reporting.sheets.service;

import com.dj.adapter.reporting.sheets.model.GoogleSpreadsheet;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;


public class GoogleSheetsService {
    /**
     * Common instance for {@code empty()}.
     */
    private static final GoogleSheetsService EMPTY = new GoogleSheetsService();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final Sheets value;

    /**
     * Constructs an empty instance.
     *
     * @implNote Generally only one empty instance, {@link GoogleSheetsService#EMPTY},
     * should exist per VM.
     */
    private GoogleSheetsService() {
        this.value = null;
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     * @throws NullPointerException if value is null
     */
    private GoogleSheetsService(Sheets value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Returns an empty {@code GoogleSheetsService} instance.  No value is present for this
     * GoogleSheetsService.
     *
     * @return an empty {@code GoogleSheetsService}
     * @apiNote Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Option.empty()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     */
    public static GoogleSheetsService empty() {
        GoogleSheetsService t = EMPTY;
        return t;
    }

    /**
     * Returns an {@code GoogleSheetsService} with the specified present non-null value.
     *
     * @param value the value to be present, which must be non-null
     * @return an {@code GoogleSheetsService} with the value present
     * @throws NullPointerException if value is null
     */
    public static GoogleSheetsService of(Sheets value) {
        return new GoogleSheetsService(value);
    }

    /**
     * Returns an {@code GoogleSheetsService} describing the specified value, if non-null,
     * otherwise returns an empty {@code GoogleSheetsService}.
     *
     * @param value the possibly-null value to describe
     * @return an {@code GoogleSheetsService} with a present value if the specified value
     * is non-null, otherwise an empty {@code GoogleSheetsService}
     */
    public static GoogleSheetsService ofNullable(Sheets value) {
        return value == null ? empty() : of(value);
    }

    /**
     * If a value is present in this {@code GoogleSheetsService}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code GoogleSheetsService}
     * @throws NoSuchElementException if there is no value present
     * @see GoogleSheetsService#isPresent()
     */
    public Sheets get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a valid spreadsheet with a given identifier exists, return the spreadsheet,
     * otherwise, return an empty {@code SpreadSheet}
     *
     * @param spreadSheetId
     * @return
     * @throws IOException
     */
    public GoogleSpreadsheet getSpreadSheetById(String spreadSheetId) throws IOException {
        return GoogleSpreadsheet
                .ofNullable(value.spreadsheets().get(spreadSheetId).execute()).setService(this);
    }
}
