/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for adding an accounting closure
 */
public class AccountingRuleCommand {

    @SuppressWarnings("unused")
    private final Long id;
    private final Long officeId;
    private final Long accountToDebitId;
    private final Long accountToCreditId;
    private final String name;
    private final String description;

    public AccountingRuleCommand(Long id, Long officeId, Long accountToDebitId, Long accountToCreditId, String name, String description) {
        this.id = id;
        this.officeId = officeId;
        this.accountToDebitId = accountToDebitId;
        this.accountToCreditId = accountToCreditId;
        this.name = name;
        this.description = description;
    }

    public void validateForCreate() {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("AccountingRule");

        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue()).value(this.accountToDebitId)
                .notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue()).value(this.accountToCreditId)
                .notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.OFFICE_ID.getValue()).value(this.officeId).ignoreIfNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.NAME.getValue()).value(this.name).notBlank()
                .notExceedingLengthOf(100);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("AccountingRule");

        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue()).value(this.accountToDebitId)
                .ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue()).value(this.accountToCreditId)
                .ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.OFFICE_ID.getValue()).value(this.officeId).ignoreIfNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.NAME.getValue()).value(this.name).ignoreIfNull().notBlank()
                .notExceedingLengthOf(100);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DESCRIPTION.getValue()).value(this.description).ignoreIfNull()
                .notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}