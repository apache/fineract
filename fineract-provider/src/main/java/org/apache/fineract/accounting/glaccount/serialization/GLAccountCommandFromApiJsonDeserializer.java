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
package org.apache.fineract.accounting.glaccount.serialization;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.glaccount.api.GLAccountJsonInputParams;
import org.apache.fineract.accounting.glaccount.command.GLAccountCommand;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.GuarantorCommand;
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