package org.mifosplatform.portfolio.charge.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public interface ChargeDropdownReadPlatformService {

    List<EnumOptionData> retrieveCalculationTypes();

    List<EnumOptionData> retrieveApplicableToTypes();

    List<EnumOptionData> retrieveCollectionTimeTypes();
}
