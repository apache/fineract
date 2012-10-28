package org.mifosng.platform.charge.service;

import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeAppliesTo;
import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeCalculationType;
import static org.mifosng.platform.charge.service.ChargeEnumerations.chargeTimeType;

import java.util.Arrays;
import java.util.List;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.charge.domain.ChargeAppliesTo;
import org.mifosng.platform.charge.domain.ChargeCalculationType;
import org.mifosng.platform.charge.domain.ChargeTimeType;
import org.springframework.stereotype.Service;

@Service
public class ChargeDropdownReadPlatformServiceImpl implements ChargeDropdownReadPlatformService {

	@Override
	public List<EnumOptionData> retrieveCalculationTypes() {

		return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
				chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT)
		// chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
		// chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST)
				);
	}

	@Override
	public List<EnumOptionData> retrieveApplicableToTypes() {
		return Arrays.asList(chargeAppliesTo(ChargeAppliesTo.LOAN));
	}

	@Override
	public List<EnumOptionData> retrieveCollectionTimeTypes() {
		return Arrays.asList(chargeTimeType(ChargeTimeType.DISBURSEMENT), chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE));
	}
}