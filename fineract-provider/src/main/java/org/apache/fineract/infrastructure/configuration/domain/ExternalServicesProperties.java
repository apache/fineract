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
package org.apache.fineract.infrastructure.configuration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.ExternalservicePropertiesJSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMTPJSONinputParams;
import org.apache.fineract.infrastructure.core.api.JsonCommand;

@Entity
@Table(name = "c_external_service_properties")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ExternalServicesProperties {

    @EmbeddedId
    ExternalServicePropertiesPK externalServicePropertiesPK;

    @Column(name = "value", length = 250)
    private String value;

    public static ExternalServicesProperties fromJson(final ExternalService externalService, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(ExternalservicePropertiesJSONinputParams.NAME.getValue());
        final String value = command.stringValueOfParameterNamed(ExternalservicePropertiesJSONinputParams.VALUE.getValue());
        return new ExternalServicesProperties().setExternalServicePropertiesPK(
                new ExternalServicePropertiesPK().setExternalServiceId(externalService.getId()).setName(name)).setValue(value);
    }

    public Map<String, Object> update(final JsonCommand command, String paramName) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        final String valueParamName = ExternalservicePropertiesJSONinputParams.VALUE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.value)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            if (paramName.equals(SMTPJSONinputParams.PASSWORD.getValue()) && newValue.equals("XXXX")) {
                // If Param Name is Password and ParamValue is XXXX that means
                // the password has not been changed.
            } else {
                actualChanges.put(valueParamName, newValue);
            }
            this.value = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public ExternalServicesPropertiesData toData() {
        return new ExternalServicesPropertiesData().setName(this.externalServicePropertiesPK.getName()).setValue(this.value);
    }
}
