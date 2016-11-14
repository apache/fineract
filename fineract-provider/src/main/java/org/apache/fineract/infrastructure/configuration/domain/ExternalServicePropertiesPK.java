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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    
    @Override 
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final ExternalServicePropertiesPK rhs = (ExternalServicePropertiesPK) obj;
		return new EqualsBuilder() //
				.append(this.externalServiceId, rhs.externalServiceId) //
				.append(this.name, rhs.name) //
				.isEquals();
	}

    @Override 
    public int hashCode() {
        return new HashCodeBuilder(17, 37) //
                .append(this.externalServiceId) //
                .append(this.name) //
                .toHashCode();
    }
}
