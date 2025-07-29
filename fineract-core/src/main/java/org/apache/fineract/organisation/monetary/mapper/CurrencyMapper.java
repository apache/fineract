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
package org.apache.fineract.organisation.monetary.mapper;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateResponse;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.mapstruct.Mapping;

@org.mapstruct.Mapper(config = MapstructMapperConfig.class)
public interface CurrencyMapper {

    @Mapping(target = "nameCode", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "displaySymbol", ignore = true)
    @Mapping(target = "displayLabel", ignore = true)
    @Mapping(source = "code", target = "code")
    @Mapping(source = "digitsAfterDecimal", target = "decimalPlaces")
    @Mapping(source = "inMultiplesOf", target = "inMultiplesOf")
    CurrencyData map(MonetaryCurrency source);

    @Mapping(target = "id", ignore = true)
    ApplicationCurrency mapToEntity(CurrencyCreateRequest request);

    @Mapping(target = "displayLabel", expression = "java(computeDisplayLabel(entity))")
    CurrencyCreateResponse mapToResponse(ApplicationCurrency entity);

    // Helper method for generating displayLabel
    default String computeDisplayLabel(ApplicationCurrency entity) {
        StringBuilder builder = new StringBuilder(20);

        if (entity.getName() != null) {
            builder.append(entity.getName()).append(' ');
        }

        if (entity.getDisplaySymbol() != null && !entity.getDisplaySymbol().trim().isEmpty()) {
            builder.append('(').append(entity.getDisplaySymbol()).append(')');
        } else {
            builder.append('[').append(entity.getCode()).append(']');
        }
        return builder.toString();
    }
}
