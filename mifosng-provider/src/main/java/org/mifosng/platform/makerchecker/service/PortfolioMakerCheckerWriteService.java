package org.mifosng.platform.makerchecker.service;


public interface PortfolioMakerCheckerWriteService {
//TODO - permissions on this to be done after development completes
	
	Long logNewEntry(String taskName, String taskJson);
	
	Long approveEntry(Long id);
}