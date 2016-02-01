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
package org.apache.fineract.portfolio.loanproduct.productmix.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixData;
import org.apache.fineract.portfolio.loanproduct.productmix.exception.ProductMixNotFoundException;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

@Service
public class ProductMixReadPlatformServiceImpl implements ProductMixReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final LoanProductReadPlatformService loanProductReadPlatformService;

    @Autowired
    public ProductMixReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final LoanProductReadPlatformService loanProductReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.loanProductReadPlatformService = loanProductReadPlatformService;
    }

    @Override
    public ProductMixData retrieveLoanProductMixDetails(final Long productId) {
        try {

            this.context.authenticatedUser();

            final ProductMixDataExtractor extractor = new ProductMixDataExtractor(this.loanProductReadPlatformService, productId);

            final String sql = "Select " + extractor.schema() + " where pm.product_id=? group by pm.product_id";

            final Map<Long, ProductMixData> productMixData = this.jdbcTemplate.query(sql, extractor, new Object[] { productId });

            return productMixData.get(productId);

        } catch (final EmptyResultDataAccessException e) {
            throw new ProductMixNotFoundException(productId);
        }
    }

    @Override
    public Collection<ProductMixData> retrieveAllProductMixes() {

        this.context.authenticatedUser();

        final ProductMixDataExtractor extractor = new ProductMixDataExtractor(this.loanProductReadPlatformService, null);

        final String sql = "Select " + extractor.schema() + " group by pm.product_id";

        final Map<Long, ProductMixData> productMixData = this.jdbcTemplate.query(sql, extractor, new Object[] {});

        return productMixData.values();
    }

    private static final class ProductMixDataExtractor implements ResultSetExtractor<Map<Long, ProductMixData>> {

        private final LoanProductReadPlatformService loanProductReadPlatformService;
        private final Long productId;

        public String schema() {
            return "pm.product_id as productId, lp.name as name from m_product_mix pm join m_product_loan lp on lp.id=pm.product_id";
        }

        public ProductMixDataExtractor(final LoanProductReadPlatformService loanProductReadPlatformService, final Long productId) {
            this.loanProductReadPlatformService = loanProductReadPlatformService;
            this.productId = productId;
        }

        @Override
        public Map<Long, ProductMixData> extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Map<Long, ProductMixData> extractedData = new HashMap<>();

            if (!rs.next()) {
                final Collection<LoanProductData> restrictedProducts = this.loanProductReadPlatformService
                        .retrieveRestrictedProductsForMix(this.productId);
                final Collection<LoanProductData> allowedProducts = this.loanProductReadPlatformService
                        .retrieveAllowedProductsForMix(this.productId);
                final ProductMixData productMixData = ProductMixData.withRestrictedOptions(restrictedProducts, allowedProducts);
                extractedData.put(this.productId, productMixData);
                return extractedData;
            }
            /* move the cursor to starting of resultset */
            rs.beforeFirst();
            while (rs.next()) {
                final Long productId = rs.getLong("productId");
                final String name = rs.getString("name");
                final Collection<LoanProductData> restrictedProducts = this.loanProductReadPlatformService
                        .retrieveRestrictedProductsForMix(productId);
                final Collection<LoanProductData> allowedProducts = this.loanProductReadPlatformService
                        .retrieveAllowedProductsForMix(productId);
                final ProductMixData productMixData = ProductMixData.withDetails(productId, name, restrictedProducts, allowedProducts);
                extractedData.put(productId, productMixData);
            }
            return extractedData;
        }
    }

}
