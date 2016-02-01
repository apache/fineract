/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.data;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
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