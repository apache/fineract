/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.Id;

@Embeddable
public class ExternalServicePropertiesPK implements Serializable {

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "external_service_id")
    private Long externalServiceId;

    public ExternalServicePropertiesPK() {

    }

    public ExternalServicePropertiesPK(Long externalServiceId, String name) {
        this.externalServiceId = externalServiceId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getExternalService() {
        return externalServiceId;
    }

}
