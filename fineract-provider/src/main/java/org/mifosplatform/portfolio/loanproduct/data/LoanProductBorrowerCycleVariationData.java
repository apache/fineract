/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductParamType;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductValueConditionType;

public class LoanProductBorrowerCycleVariationData {

    @SuppressWarnings("unused")
    private final Long id;
    private final Integer borrowerCycleNumber;
    private final EnumOptionData paramType;
    private final EnumOptionData valueConditionType;
    @SuppressWarnings("unused")
    private final BigDecimal minValue;
    @SuppressWarnings("unused")
    private final BigDecimal maxValue;
    private final BigDecimal defaultValue;

    public LoanProductBorrowerCycleVariationData(final Long id, final Integer borrowerCycleNumber, final EnumOptionData paramType,
            final EnumOptionData valueConditionType, final BigDecimal defaultValue, final BigDecimal minValue, final BigDecimal maxValue) {
        this.id = id;
        this.borrowerCycleNumber = borrowerCycleNumber;
        this.paramType = paramType;
        this.valueConditionType = valueConditionType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
    }

    public LoanProductParamType getParamType() {
        return LoanProductParamType.fromInt(this.paramType.getId().intValue());
    }

    public Integer getBorrowerCycleNumber() {
        return this.borrowerCycleNumber;
    }

    public LoanProductValueConditionType getValueConditionType() {
        return LoanProductValueConditionType.fromInt(this.valueConditionType.getId().intValue());
    }

    public BigDecimal getDefaultValue() {
        return this.defaultValue;
    }

}
