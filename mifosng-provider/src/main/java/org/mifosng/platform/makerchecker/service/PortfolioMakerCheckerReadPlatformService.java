package org.mifosng.platform.makerchecker.service;

import java.util.Collection;

import org.mifosng.platform.api.data.MakerCheckerData;

public interface PortfolioMakerCheckerReadPlatformService {

	Collection<MakerCheckerData> retrieveAllEntriesToBeChecked();

	MakerCheckerData retrieveById(Long id);

}
