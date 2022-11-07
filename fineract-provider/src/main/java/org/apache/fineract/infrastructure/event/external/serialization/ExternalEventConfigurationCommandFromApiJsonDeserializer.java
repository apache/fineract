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
package org.apache.fineract.infrastructure.event.external.serialization;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.event.external.command.ExternalEventConfigurationCommand;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExternalEventConfigurationCommandFromApiJsonDeserializer
        extends AbstractFromApiJsonDeserializer<ExternalEventConfigurationCommand> {

    private static final String EXTERNAL_EVENT_CONFIGURATIONS = "externalEventConfigurations";
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(EXTERNAL_EVENT_CONFIGURATIONS));
    private final FromJsonHelper fromApiJsonHelper;

    @Override
    public ExternalEventConfigurationCommand commandFromApiJson(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        return fromApiJsonHelper.fromJson(json, ExternalEventConfigurationCommand.class);
    }
}
