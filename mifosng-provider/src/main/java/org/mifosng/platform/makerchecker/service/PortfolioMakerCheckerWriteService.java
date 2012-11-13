package org.mifosng.platform.makerchecker.service;

public interface PortfolioMakerCheckerWriteService {

	Long logNewEntry(String taskName, String taskJson);

	Long approveEntry(Long id);
}