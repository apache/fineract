package org.mifosplatform.accounting.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.domain.AccountingRuleType;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

/**
 * TODO Vishwas find a better approach for validation
 * 
 * Currently, validation of the passed in JSON is done before calling save or
 * update method on the target resource (in our case the loan Product)
 * 
 * However, in the case of a loan product it would be difficult to validate the
 * passed in JSON for valid {@link LoanProduct} to {@link GLAccount} mappings
 * during update because of the following scenario
 * 
 * The accounting rule type may be changed in the update command, so we would
 * have to validate if all required account heads for a particular account type
 * have been passed in (would be different for CASH and Accrual based). However,
 * till we have access to the domain object it would not be possible to detect
 * if an accounting rule has actually been changed
 * 
 * Hence, method {@link #validateForCreate(String)} from this class is called
 * separately for validation only if an accounting rule change is detected by
 * {@link ProductToGLAccountMappingWritePlatformService}
 * 
 * Also, the class is probably named wrong (*FromApiJsonDeserializer) should
 * probably be named as (*Validator) instead
 * 
 */
@Component
public final class ProductToGLAccountMappingFromApiJsonDeserializer {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ProductToGLAccountMappingFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = fromApiJsonHelper.parse(json);

        // accounting related data validation
        final Integer accountingRuleType = fromApiJsonHelper.extractIntegerNamed("accountingType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingType").value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        if (isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long writeOffAccountId = fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

        }

        if (isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    private boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_BASED.getValue().equals(accountingRuleType);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}