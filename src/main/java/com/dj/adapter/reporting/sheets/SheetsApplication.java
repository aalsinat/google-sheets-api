package com.dj.adapter.reporting.sheets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SheetsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SheetsApplication.class, args);
	}

	//TODO: Add logging entries to application
	//TODO: Add offset features to sheet headers
	//TODO: Add dynamic restrictions for updating cell rows. Use Predicates as a parameter on save operation.
	//TODO: Add utilities for A1Notation
}
