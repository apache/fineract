/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import static org.mifosplatform.portfolio.charge.service.ChargeEnumerations.chargeCalculationType;
import static org.mifosplatform.portfolio.charge.service.ChargeEnumerations.chargePaymentMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargePaymentMode;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
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
        List<EnumOptionData> chargeAppliesToTypes = new ArrayList<EnumOptionData>();
        for (ChargeAppliesTo chargeAppliesTo : ChargeAppliesTo.values()) {
            if(ChargeAppliesTo.INVALID.equals(chargeAppliesTo)) continue;
            chargeAppliesToTypes.add(ChargeEnumerations.chargeAppliesTo(chargeAppliesTo));
        }
        return chargeAppliesToTypes;
    }

    @Override
    public List<EnumOptionData> retrieveCollectionTimeTypes() {
        List<EnumOptionData> chargeTimeTypes = new ArrayList<EnumOptionData>();
        for (ChargeTimeType chargeTimeType : ChargeTimeType.values()) {
            if (ChargeTimeType.INVALID.equals(chargeTimeType) 
                    || ChargeTimeType.MONTHLY.equals(chargeTimeType) // To be implemented for Savings
                    || ChargeTimeType.YEARLY.equals(chargeTimeType)) // To be implemented for Savings 
                continue;
            chargeTimeTypes.add(ChargeEnumerations.chargeTimeType(chargeTimeType));
        }
        return chargeTimeTypes;
    }

    @Override
    public List<EnumOptionData> retrivePaymentModes() {
        return Arrays.asList(chargePaymentMode(ChargePaymentMode.REGULAR), chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER));
    }
}