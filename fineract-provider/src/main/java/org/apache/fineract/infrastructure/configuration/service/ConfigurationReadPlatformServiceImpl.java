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
package org.apache.fineract.infrastructure.configuration.service;

import static org.apache.fineract.query.QCConfiguration.cConfiguration;
import static org.apache.fineract.query.QXRegisteredTable.xRegisteredTable;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationData;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationAllowedValues;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConfigurationReadPlatformServiceImpl implements ConfigurationReadPlatformService {

    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepositoryWrapper configurationRepositoryWrapper;
    private final SQLQueryFactory queryFactory;

    @Autowired
    public ConfigurationReadPlatformServiceImpl(final PlatformSecurityContext context,
            final GlobalConfigurationRepositoryWrapper configurationRepositoryWrapper, final SQLQueryFactory queryFactory) {
        this.context = context;
        this.configurationRepositoryWrapper = configurationRepositoryWrapper;
        this.queryFactory = queryFactory;
    }

    @Override
    public GlobalConfigurationData retrieveGlobalConfiguration(final boolean survey) {

        this.context.authenticatedUser();

        SQLQuery<Tuple> query = queryFactory.select(cConfiguration.id, cConfiguration.name, cConfiguration.enabled, cConfiguration.value,
                cConfiguration.dateValue, cConfiguration.description, cConfiguration.stringValue, cConfiguration.isTrapDoor)
                .from(cConfiguration);

        if (survey) {
            query.join(xRegisteredTable).on(xRegisteredTable.registeredTableName.eq(cConfiguration.name))
                    .where(xRegisteredTable.category.eq(DataTableApiConstant.CATEGORY_PPI));
        }

        final List<GlobalConfigurationPropertyData> globalConfiguration = query.orderBy(cConfiguration.id.asc()).fetch().stream()
                .map(this::toData).toList();

        return new GlobalConfigurationData().setGlobalConfiguration(globalConfiguration);
    }

    @Override
    public GlobalConfigurationPropertyData retrieveGlobalConfiguration(final String name) {

        this.context.authenticatedUser();

        return configurationRepositoryWrapper.findOneByNameWithNotFoundDetection(name);
    }

    @Override
    public GlobalConfigurationPropertyData retrieveGlobalConfiguration(Long configId) {

        this.context.authenticatedUser();

        GlobalConfigurationProperty globalConfigurationProperty = configurationRepositoryWrapper.findOneWithNotFoundDetection(configId);

        return globalConfigurationProperty.toData();
    }

    private GlobalConfigurationPropertyData toData(final Tuple row) {
        final Integer value = row.get(cConfiguration.value);
        final Date dateValue = row.get(cConfiguration.dateValue);
        return GlobalConfigurationAllowedValues.withAllowedValues(new GlobalConfigurationPropertyData()
                .setName(row.get(cConfiguration.name)).setEnabled(Boolean.TRUE.equals(row.get(cConfiguration.enabled)))
                .setValue(value != null ? value.longValue() : 0L).setDateValue(dateValue != null ? dateValue.toLocalDate() : null)
                .setStringValue(row.get(cConfiguration.stringValue)).setId(Objects.requireNonNullElse(row.get(cConfiguration.id), 0L))
                .setDescription(row.get(cConfiguration.description)).setTrapDoor(Boolean.TRUE.equals(row.get(cConfiguration.isTrapDoor))));
    }

}
