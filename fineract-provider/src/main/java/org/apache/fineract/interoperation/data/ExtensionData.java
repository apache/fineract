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
package org.apache.fineract.interoperation.data;

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_KEY;
import static org.apache.fineract.interoperation.util.InteropUtil.PARAM_VALUE;

public class ExtensionData {

    public static final String[] PARAMS = {PARAM_KEY, PARAM_VALUE};

    @NotNull
    private final String key;

    private String value;

    public ExtensionData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ExtensionData validateAndParse(DataValidatorBuilder dataValidator, JsonObject element, FromJsonHelper jsonHelper) {
        if (element == null)
            return null;

        jsonHelper.checkForUnsupportedParameters(element, Arrays.asList(PARAMS));

        String key = jsonHelper.extractStringNamed(PARAM_KEY, element);
        DataValidatorBuilder  dataValidatorCopy = dataValidator.reset().parameter(PARAM_KEY).value(key).notBlank();

        String value = jsonHelper.extractStringNamed(PARAM_VALUE, element);

        dataValidator.merge(dataValidatorCopy);
        return dataValidator.hasError() ? null : new ExtensionData(key, value);
    }
}
