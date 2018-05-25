package com.dj.adapter.reporting.sheets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SheetsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SheetsApplication.class, args);
	}
	//TODO: Add offset management features to sheet headers
	//TODO: Add dynamic restrictions for updating cell rows. Use Predicates as a parameter on save operation.
	//TODO: Add utilities for A1Notation
	//TODO: Manage particular formats for every cell content (Date, Formula, ...)
	//TODO: Afegir retry en les crides de la API
	//TODO: Fer que les crides siguin asincrones
	//TODO: Add logging entries to application
}
