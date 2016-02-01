/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toOfficeIdParamName;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class AccountTransfersDetailDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountTransfersDetailDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {

        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        baseDataValidator.reset().parameter(fromOfficeIdParamName).value(fromOfficeId).notNull().integerGreaterThanZero();

        final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        baseDataValidator.reset().parameter(fromClientIdParamName).value(fromClientId).notNull().integerGreaterThanZero();

        final Long fromAccountId = this.fromApiJsonHelper.extractLongNamed(fromAccountIdParamName, element);
        baseDataValidator.reset().parameter(fromAccountIdParamName).value(fromAccountId).notNull().integerGreaterThanZero();

        final Integer fromAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(fromAccountTypeParamName, element);
        baseDataValidator.reset().parameter(fromAccountTypeParamName).value(fromAccountType).notNull()
                .isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        baseDataValidator.reset().parameter(toOfficeIdParamName).value(toOfficeId).notNull().integerGreaterThanZero();

        final Long toClientId = this.fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        baseDataValidator.reset().parameter(toClientIdParamName).value(toClientId).notNull().integerGreaterThanZero();

        final Long toAccountId = this.fromApiJsonHelper.extractLongNamed(toAccountIdParamName, element);
        baseDataValidator.reset().parameter(toAccountIdParamName).value(toAccountId).notNull().integerGreaterThanZero();

        final Integer toAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(toAccountTypeParamName, element);
        baseDataValidator.reset().parameter(toAccountTypeParamName).value(toAccountType).notNull()
                .isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

    }

}