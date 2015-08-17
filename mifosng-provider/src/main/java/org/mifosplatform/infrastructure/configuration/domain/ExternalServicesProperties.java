/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.configuration.data.ExternalServicesPropertiesData;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants.EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesConstants.SMTP_JSON_INPUT_PARAMS;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "c_external_service_properties")
public class ExternalServicesProperties {

    @EmbeddedId
    ExternalServicePropertiesPK externalServicePropertiesPK;

    @Column(name = "value", length = 250)
    private String value;

    protected ExternalServicesProperties() {

    }

    private ExternalServicesProperties(final ExternalServicePropertiesPK externalServicePropertiesPK, final String value) {
        this.externalServicePropertiesPK = externalServicePropertiesPK;

        this.value = value;

    }

    public static ExternalServicesProperties fromJson(final ExternalService externalService, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed(EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS.NAME.getValue());
        final String value = command.stringValueOfParameterNamed(EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS.VALUE.getValue());
        return new ExternalServicesProperties(new ExternalServicePropertiesPK(externalService.getId(), name), value);
    }

    public Map<String, Object> update(final JsonCommand command, String paramName) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        final String valueParamName = EXTERNALSERVICEPROPERTIES_JSON_INPUT_PARAMS.VALUE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, this.value)) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            if (paramName.equals(SMTP_JSON_INPUT_PARAMS.PASSWORD.getValue()) && newValue.equals("XXXX")) {
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
        return new ExternalServicesPropertiesData(this.externalServicePropertiesPK.getName(), this.value);
    }

}
