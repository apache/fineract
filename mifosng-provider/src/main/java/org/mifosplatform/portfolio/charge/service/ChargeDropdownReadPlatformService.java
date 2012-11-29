package org.mifosplatform.portfolio.charge.service;

import java.util.List;

import org.mifosng.platform.api.data.EnumOptionData;

public interface ChargeDropdownReadPlatformService {

	List<EnumOptionData> retrieveCalculationTypes();

	List<EnumOptionData> retrieveApplicableToTypes();

	List<EnumOptionData> retrieveCollectionTimeTypes();

}
