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
package org.apache.fineract.infrastructure.survey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.survey.api.LikelihoodApiConstants;
import org.apache.fineract.infrastructure.survey.data.LikelihoodStatus;

@Entity
@Table(name = "ppi_likelihoods_ppi")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public final class Likelihood extends AbstractPersistableCustom<Long> {

    @Column(name = "ppi_name", nullable = false)
    private String ppiName;

    @Column(name = "likelihood_id", nullable = false)
    private Long likelihoodId;

    @Column(name = "enabled", nullable = false)
    private Long enabled;

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final boolean enabled = command.booleanPrimitiveValueOfParameterNamed(LikelihoodApiConstants.ACTIVE);

        Long changeToValue = null;

        if (enabled) {
            changeToValue = LikelihoodStatus.ENABLED;
        } else {
            changeToValue = LikelihoodStatus.DISABLED;
        }

        if (!changeToValue.equals(this.enabled)) {
            actualChanges.put(LikelihoodApiConstants.ACTIVE, enabled);
            this.enabled = changeToValue;
        }

        return actualChanges;
    }

    public boolean isActivateCommand(final JsonCommand command) {
        return command.booleanPrimitiveValueOfParameterNamed(LikelihoodApiConstants.ACTIVE);
    }
}
