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
package org.apache.fineract.portfolio.savingsproductmix.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savingsproductmix.data.SavingsProductMixData;
import org.apache.fineract.portfolio.savingsproductmix.exception.SavingsProductMixNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class SavingsProductMixReadPlatformServiceImpl implements SavingsProductMixReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;

    @Autowired
    public SavingsProductMixReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final SavingsProductReadPlatformService savingsProductReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;
    }

    @Override
    public SavingsProductMixData retrieveSavingsProductMixDetails(final Long productId) {
        try {

            this.context.authenticatedUser();

            final SavingsProductMixDataExtractor extractor = new SavingsProductMixDataExtractor(this.savingsProductReadPlatformService, productId);

            final String sql = "Select " + extractor.schema() + " where pm.product_id=? group by pm.product_id";

            final Map<Long, SavingsProductMixData> savingsProductMixData = this.jdbcTemplate.query(sql, extractor, new Object[] { productId });

            return savingsProductMixData.get(productId);

        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsProductMixNotFoundException(productId);
        }
    }

    @Override
    public Collection<SavingsProductMixData> retrieveAllSavingsProductMixes() {

        this.context.authenticatedUser();

        final SavingsProductMixDataExtractor extractor = new SavingsProductMixDataExtractor(this.savingsProductReadPlatformService, null);

        final String sql = "Select " + extractor.schema() + " group by pm.product_id";

        final Map<Long, SavingsProductMixData> savingsProductMixData = this.jdbcTemplate.query(sql, extractor, new Object[] {});

        return savingsProductMixData.values();
    }

    private static final class SavingsProductMixDataExtractor implements ResultSetExtractor<Map<Long, SavingsProductMixData>> {

        private final SavingsProductReadPlatformService savingsProductReadPlatformService;
        private final Long productId;

        public String schema() {
            return "pm.product_id as productId, sp.name as name from m_savings_product_mix pm join m_savings_product sp on sp.id=pm.product_id";
        }

        public SavingsProductMixDataExtractor(final SavingsProductReadPlatformService savingsProductReadPlatformService, final Long productId) {
            this.savingsProductReadPlatformService = savingsProductReadPlatformService;
            this.productId = productId;
        }

        @Override
        public Map<Long, SavingsProductMixData> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Map<Long, SavingsProductMixData> extractedData = new HashMap<>();

            if (!rs.next()) {
                final Collection<SavingsProductData> restrictedProducts = this.savingsProductReadPlatformService
                        .retrieveRestrictedSavingsProductsForMix(this.productId);
                final Collection<SavingsProductData> allowedProducts = this.savingsProductReadPlatformService
                        .retrieveAllowedSavingsProductsForMix(this.productId);
                final SavingsProductMixData savingsProductMixData = SavingsProductMixData.withRestrictedOptions(restrictedProducts, allowedProducts);
                extractedData.put(this.productId, savingsProductMixData);
                return extractedData;
            }
            /* move the cursor to starting of resultset */
            rs.beforeFirst();
            while (rs.next()) {
                final Long productId = rs.getLong("productId");
                final String name = rs.getString("name");
                final Collection<SavingsProductData> restrictedProducts = this.savingsProductReadPlatformService
                        .retrieveRestrictedSavingsProductsForMix(productId);
                final Collection<SavingsProductData> allowedProducts = this.savingsProductReadPlatformService
                        .retrieveAllowedSavingsProductsForMix(productId);
                final SavingsProductMixData savingsProductMixData = SavingsProductMixData.withDetails(productId, name, restrictedProducts, allowedProducts);
                extractedData.put(productId, savingsProductMixData);
            }
            return extractedData;
        }
    }

}
