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
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.tax.data.TaxComponentCreateRequest;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = {})
public interface TaxComponentCreateRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "taxComponentHistories", ignore = true)
    @Mapping(target = "taxGroupMappings", ignore = true)
    @Mapping(target = "debitAccount", source = "debitAccountId", qualifiedByName = "mapGLAccount")
    @Mapping(target = "creditAccount", source = "creditAccountId", qualifiedByName = "mapGLAccount")
    @Mapping(target = "startDate", ignore = true)
    TaxComponent map(TaxComponentCreateRequest source);

    @Named("mapGLAccount")
    default GLAccount mapGLAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        var account = new GLAccount();
        account.setId(accountId);
        return account;
    }

    @AfterMapping
    default void afterMapping(TaxComponentCreateRequest source, @MappingTarget TaxComponent target) {
        var startDate = DateUtils.getBusinessLocalDate();

        if (source.getStartDate() != null && source.getDateFormat() != null) {
            var locale = (source.getLocale() != null && !source.getLocale().isBlank()) ? Locale.forLanguageTag(source.getLocale())
                    : Locale.getDefault();
            var formatter = DateTimeFormatter.ofPattern(source.getDateFormat(), locale);
            startDate = LocalDate.parse(source.getStartDate(), formatter);
        }

        target.setStartDate(startDate);
    }
}
