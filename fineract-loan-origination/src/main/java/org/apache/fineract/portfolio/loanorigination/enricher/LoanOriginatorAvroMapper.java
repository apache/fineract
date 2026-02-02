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
package org.apache.fineract.portfolio.loanorigination.enricher;

import org.apache.fineract.avro.generic.v1.CodeValueDataV1;
import org.apache.fineract.avro.loan.v1.OriginatorDetailsV1;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorAvroMapper {

    /**
     * Converts a LoanOriginator entity to OriginatorDetailsV1 Avro
     */
    public OriginatorDetailsV1 toAvro(final LoanOriginator originator) {
        if (originator == null) {
            return null;
        }

        final OriginatorDetailsV1.Builder builder = OriginatorDetailsV1.newBuilder();

        builder.setId(originator.getId());
        builder.setExternalId(originator.getExternalId() != null ? originator.getExternalId().getValue() : null);
        builder.setName(originator.getName());
        builder.setStatus(originator.getStatus() != null ? originator.getStatus().getValue() : null);
        builder.setOriginatorType(mapCodeValue(originator.getOriginatorType()));
        builder.setChannelType(mapCodeValue(originator.getChannelType()));

        return builder.build();
    }

    /**
     * Converts a CodeValue entity to CodeValueDataV1 Avro
     */
    private CodeValueDataV1 mapCodeValue(CodeValue codeValue) {
        if (codeValue == null) {
            return null;
        }

        CodeValueDataV1.Builder builder = CodeValueDataV1.newBuilder();
        builder.setId(codeValue.getId());
        builder.setName(codeValue.getLabel());
        builder.setPosition(codeValue.getPosition());
        builder.setDescription(codeValue.getDescription());
        builder.setActive(codeValue.isActive());
        builder.setMandatory(codeValue.isMandatory());

        return builder.build();
    }
}
