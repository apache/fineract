/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.charge.domain.ChargeAppliesTo;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargePaymentMode;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mifosplatform.portfolio.charge.service.ChargeEnumerations.*;

@Service
public class ChargeDropdownReadPlatformServiceImpl implements ChargeDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveCalculationTypes() {

        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST));
    }

    @Override
    public List<EnumOptionData> retrieveApplicableToTypes() {
        final List<EnumOptionData> chargeAppliesToTypes = new ArrayList<>();
        for (final ChargeAppliesTo chargeAppliesTo : ChargeAppliesTo.values()) {
            if (ChargeAppliesTo.INVALID.equals(chargeAppliesTo)) {
                continue;
            }
            chargeAppliesToTypes.add(ChargeEnumerations.chargeAppliesTo(chargeAppliesTo));
        }
        return chargeAppliesToTypes;
    }

    @Override
    public List<EnumOptionData> retrieveCollectionTimeTypes() {
        final List<EnumOptionData> chargeTimeTypes = new ArrayList<>();
        for (final ChargeTimeType chargeTimeType : ChargeTimeType.values()) {
            if (ChargeTimeType.INVALID.equals(chargeTimeType) || ChargeTimeType.SAVINGS_CLOSURE.equals(chargeTimeType)) {
                continue;
            }
            chargeTimeTypes.add(ChargeEnumerations.chargeTimeType(chargeTimeType));
        }
        return chargeTimeTypes;
    }

    @Override
    public List<EnumOptionData> retrivePaymentModes() {
        return Arrays.asList(chargePaymentMode(ChargePaymentMode.REGULAR), chargePaymentMode(ChargePaymentMode.ACCOUNT_TRANSFER));
    }

    @Override
    public List<EnumOptionData> retrieveLoanCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_INTEREST));
    }

    @Override
    public List<EnumOptionData> retrieveLoanCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.DISBURSEMENT), chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE),
                chargeTimeType(ChargeTimeType.INSTALMENT_FEE), chargeTimeType(ChargeTimeType.OVERDUE_INSTALLMENT));
    }

    @Override
    public List<EnumOptionData> retrieveSavingsCalculationTypes() {
        return Arrays.asList(chargeCalculationType(ChargeCalculationType.FLAT),
                chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT));
    }

    @Override
    public List<EnumOptionData> retrieveSavingsCollectionTimeTypes() {
        return Arrays.asList(chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE), chargeTimeType(ChargeTimeType.SAVINGS_ACTIVATION),
                // chargeTimeType(ChargeTimeType.SAVINGS_CLOSURE),
                chargeTimeType(ChargeTimeType.WITHDRAWAL_FEE), chargeTimeType(ChargeTimeType.ANNUAL_FEE),
                chargeTimeType(ChargeTimeType.MONTHLY_FEE), chargeTimeType(ChargeTimeType.WEEKLY_FEE),
                chargeTimeType(ChargeTimeType.OVERDRAFT_FEE));
    }
}