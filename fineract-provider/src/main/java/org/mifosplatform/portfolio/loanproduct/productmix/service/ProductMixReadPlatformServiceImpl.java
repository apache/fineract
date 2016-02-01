/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.productmix.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.productmix.data.ProductMixData;
import org.mifosplatform.portfolio.loanproduct.productmix.exception.ProductMixNotFoundException;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
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
