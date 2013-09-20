/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.serialization;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.glaccount.api.GLAccountJsonInputParams;
import org.mifosplatform.accounting.glaccount.command.GLAccountCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link GuarantorCommand}'s.
 */
@Component
public final class GLAccountCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<GLAccountCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GLAccountCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonfromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
    }

    @Override
    public GLAccountCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = GLAccountJsonInputParams.getAllValues();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long id = this.fromApiJsonHelper.extractLongNamed(GLAccountJsonInputParams.ID.getValue(), element);
        final String name = this.fromApiJsonHelper.extractStringNamed(GLAccountJsonInputParams.NAME.getValue(), element);
        final Long parentId = this.fromApiJsonHelper.extractLongNamed(GLAccountJsonInputParams.PARENT_ID.getValue(), element);
        final String glCode = this.fromApiJsonHelper.extractStringNamed(GLAccountJsonInputParams.GL_CODE.getValue(), element);
        final Boolean disabled = this.fromApiJsonHelper.extractBooleanNamed(GLAccountJsonInputParams.DISABLED.getValue(), element);
        final Boolean manualEntriesAllowed = this.fromApiJsonHelper.extractBooleanNamed(
                GLAccountJsonInputParams.MANUAL_ENTRIES_ALLOWED.getValue(), element);
        final Integer type = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(GLAccountJsonInputParams.TYPE.getValue(), element);
        final Integer usage = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(GLAccountJsonInputParams.USAGE.getValue(), element);
        final String description = this.fromApiJsonHelper.extractStringNamed(GLAccountJsonInputParams.DESCRIPTION.getValue(), element);
        final Long tagId = this.fromApiJsonHelper.extractLongNamed(GLAccountJsonInputParams.TAGID.getValue(), element);

        return new GLAccountCommand(id, name, parentId, glCode, disabled, manualEntriesAllowed, type, usage, description, tagId);
    }
}