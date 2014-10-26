/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.serialization;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.accountingRuleParamName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.savings.DepositAccountType;
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
 * Hence, method {@link #validateForLoanProductCreate(String)} from this class
 * is called separately for validation only if an accounting rule change is
 * detected by {@link ProductToGLAccountMappingWritePlatformService}
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

    public void validateForLoanProductCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanproduct");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper.extractIntegerNamed("accountingRule", element, Locale.getDefault());
        baseDataValidator.reset().parameter("accountingRule").value(accountingRuleType).notNull().inMinMaxRange(1, 4);

        if (isCashBasedAccounting(accountingRuleType) || isAccrualBasedAccounting(accountingRuleType)) {

            final Long fundAccountId = this.fromApiJsonHelper.extractLongNamed(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                    element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).value(fundAccountId).notNull()
                    .integerGreaterThanZero();

            final Long loanPortfolioAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOAN_PORTFOLIO.getValue()).value(loanPortfolioAccountId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromInterestId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_LOANS.getValue()).value(incomeFromInterestId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue()).value(incomeFromPenaltyId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromRecoveryAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_RECOVERY.getValue()).value(incomeFromRecoveryAccountId)
                    .notNull().integerGreaterThanZero();

            final Long writeOffAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writeOffAccountId)
                    .notNull().integerGreaterThanZero();

            final Long overpaymentAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.OVERPAYMENT.getValue()).value(overpaymentAccountId)
                    .notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

        }

        if (isAccrualBasedAccounting(accountingRuleType)) {

            final Long receivableInterestAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.INTEREST_RECEIVABLE.getValue())
                    .value(receivableInterestAccountId).notNull().integerGreaterThanZero();

            final Long receivableFeeAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.FEES_RECEIVABLE.getValue()).value(receivableFeeAccountId)
                    .notNull().integerGreaterThanZero();

            final Long receivablePenaltyAccountId = this.fromApiJsonHelper.extractLongNamed(
                    LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue(), element);
            baseDataValidator.reset().parameter(LOAN_PRODUCT_ACCOUNTING_PARAMS.PENALTIES_RECEIVABLE.getValue())
                    .value(receivablePenaltyAccountId).notNull().integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForSavingsProductCreate(final String json, DepositAccountType accountType) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SAVINGS_PRODUCT_RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // accounting related data validation
        final Integer accountingRuleType = this.fromApiJsonHelper
                .extractIntegerNamed(accountingRuleParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(accountingRuleParamName).value(accountingRuleType).notNull().inMinMaxRange(1, 3);

        if (isCashBasedAccounting(accountingRuleType)) {

            final Long savingsControlAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue())
                    .value(savingsControlAccountId).notNull().integerGreaterThanZero();

            final Long savingsReferenceAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue())
                    .value(savingsReferenceAccountId).notNull().integerGreaterThanZero();

            final Long transfersInSuspenseAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue())
                    .value(transfersInSuspenseAccountId).notNull().integerGreaterThanZero();

            final Long interestOnSavingsAccountId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue())
                    .value(interestOnSavingsAccountId).notNull().integerGreaterThanZero();

            final Long incomeFromFeeId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue()).value(incomeFromFeeId)
                    .notNull().integerGreaterThanZero();

            final Long incomeFromPenaltyId = this.fromApiJsonHelper.extractLongNamed(
                    SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(), element);
            baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue())
                    .value(incomeFromPenaltyId).notNull().integerGreaterThanZero();

            if (!accountType.equals(DepositAccountType.RECURRING_DEPOSIT) && !accountType.equals(DepositAccountType.FIXED_DEPOSIT)) {
                final Long overdraftAccount = this.fromApiJsonHelper.extractLongNamed(
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(), element);
                baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue())
                        .value(overdraftAccount).notNull().integerGreaterThanZero();

                final Long incomeFromInterest = this.fromApiJsonHelper.extractLongNamed(
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), element);
                baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue())
                        .value(incomeFromInterest).notNull().integerGreaterThanZero();

                final Long writtenOff = this.fromApiJsonHelper.extractLongNamed(
                        SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(), element);
                baseDataValidator.reset().parameter(SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue()).value(writtenOff)
                        .notNull().integerGreaterThanZero();
            }

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private boolean isCashBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.CASH_BASED.getValue().equals(accountingRuleType);
    }

    private boolean isAccrualBasedAccounting(final Integer accountingRuleType) {
        return AccountingRuleType.ACCRUAL_PERIODIC.getValue().equals(accountingRuleType)
                || AccountingRuleType.ACCRUAL_UPFRONT.getValue().equals(accountingRuleType);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}