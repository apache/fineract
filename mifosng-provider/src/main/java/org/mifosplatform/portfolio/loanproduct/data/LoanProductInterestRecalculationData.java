package org.mifosplatform.portfolio.loanproduct.data;

import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.interestRecalculationCompoundingType;
import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.rescheduleStrategyType;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;

public class LoanProductInterestRecalculationData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long productId;
    private final EnumOptionData interestRecalculationCompoundingType;
    private final EnumOptionData rescheduleStrategyType;

    public LoanProductInterestRecalculationData(final Long id, final Long productId,
            final EnumOptionData interestRecalculationCompoundingType, final EnumOptionData rescheduleStrategyType) {
        this.id = id;
        this.productId = productId;
        this.interestRecalculationCompoundingType = interestRecalculationCompoundingType;
        this.rescheduleStrategyType = rescheduleStrategyType;
    }

    public static LoanProductInterestRecalculationData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final Long productId = null;
        final EnumOptionData interestRecalculationCompoundingType = interestRecalculationCompoundingType(InterestRecalculationCompoundingMethod.NONE);
        final EnumOptionData rescheduleStrategyType = rescheduleStrategyType(LoanRescheduleStrategyMethod.REDUCE_EMI_AMOUNT);
        return new LoanProductInterestRecalculationData(id, productId, interestRecalculationCompoundingType, rescheduleStrategyType);
    }

    public EnumOptionData getInterestRecalculationCompoundingType() {
        return this.interestRecalculationCompoundingType;
    }

    public EnumOptionData getRescheduleStrategyType() {
        return this.rescheduleStrategyType;
    }

}
