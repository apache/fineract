/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.serialization;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.accounting.rule.command.AccountingRuleCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class AccountingRuleCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<AccountingRuleCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountingRuleCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonfromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
    }

    @Override
    public AccountingRuleCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = AccountingRuleJsonInputParams.getAllValues();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long id = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ID.getValue(), element);
        final Long officeId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.OFFICE_ID.getValue(), element);
        final Long accountToDebitId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(),
                element);
        final Long accountToCreditId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                element);
        final String name = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.NAME.getValue(), element);
        final String description = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.DESCRIPTION.getValue(), element);

        // final String[] creditTags =
        this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue(), element);
        // final String[] debitTags =
        this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue(), element);

        return new AccountingRuleCommand(id, officeId, accountToDebitId, accountToCreditId, name, description);
    }
}