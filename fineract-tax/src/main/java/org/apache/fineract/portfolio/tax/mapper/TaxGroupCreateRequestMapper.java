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
package org.apache.fineract.portfolio.tax.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.data.TaxGroupComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupCreateRequest;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructMapperConfig.class, uses = {})
public interface TaxGroupCreateRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "taxGroupMappings", ignore = true) // handled in @AfterMapping
    TaxGroup map(TaxGroupCreateRequest source);

    @AfterMapping
    default void afterMapping(TaxGroupCreateRequest source, @MappingTarget TaxGroup target) {
        if (source.getTaxComponents() == null || source.getTaxComponents().isEmpty()) {
            return;
        }

        Locale locale = (source.getLocale() != null && !source.getLocale().isBlank()) ? Locale.forLanguageTag(source.getLocale())
                : Locale.getDefault();

        Set<TaxGroupMappings> mappings = source.getTaxComponents().stream()
                .map(componentData -> mapComponent(componentData, target,
                        (source.getDateFormat() != null ? DateTimeFormatter.ofPattern(source.getDateFormat(), locale) : null)))
                .collect(Collectors.toSet());

        target.setTaxGroupMappings(mappings);
    }

    default TaxGroupMappings mapComponent(TaxGroupComponentData data, TaxGroup taxGroup, DateTimeFormatter formatter) {
        var mapping = new TaxGroupMappings();
        mapping.setTaxGroup(taxGroup);

        // TaxComponent reference by ID
        var component = new TaxComponent();
        component.setId(data.getTaxComponentId());
        mapping.setTaxComponent(component);

        // Parse startDate if provided; otherwise default to business date
        var startDate = DateUtils.getBusinessLocalDate();

        if (data.getStartDate() != null && formatter != null) {
            startDate = LocalDate.parse(data.getStartDate(), formatter);
        }

        mapping.setStartDate(startDate);
        mapping.setEndDate(null);

        return mapping;
    }
}
