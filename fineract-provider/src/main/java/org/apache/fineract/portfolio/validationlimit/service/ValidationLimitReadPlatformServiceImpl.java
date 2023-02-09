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
package org.apache.fineract.portfolio.validationlimit.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.validationlimit.data.ValidationLimitData;
import org.apache.fineract.portfolio.validationlimit.exception.ValidationLimitNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author Deepika
 *
 */
@Service
@SuppressWarnings("checkstyle:avoidHidingCauseException")
public class ValidationLimitReadPlatformServiceImpl implements ValidationLimitReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public ValidationLimitReadPlatformServiceImpl(RoutingDataSource dataSource, CodeValueReadPlatformService codeValueReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @Override
    @Cacheable(value = "ValidationLimit", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public Collection<ValidationLimitData> retrieveAllValidationLimits() {
        final ValidationLimitMapper rm = new ValidationLimitMapper();
        String sql = "select " + rm.validationLimitSchema() + " order by cvclientlevel.order_position";
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public ValidationLimitData retrieveTemplateDetails() {

        final List<CodeValueData> clientLevelOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_LEVELS));
        return ValidationLimitData.template(clientLevelOptions);
    }

    @Override
    public ValidationLimitData retrieveValidationLimit(final Long validationLimitId) {

        try {
            final ValidationLimitMapper rm = new ValidationLimitMapper();
            String sql = "select " + rm.validationLimitSchema() + " where v.id = ?; ";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { validationLimitId });
        } catch (final EmptyResultDataAccessException e) {
            throw new ValidationLimitNotFoundException(validationLimitId);
        }

    }

    private static final class ValidationLimitMapper implements RowMapper<ValidationLimitData> {

        public String validationLimitSchema() {
            return "v.id as id , v.client_level_cv_id as clientLevelId, cvclientlevel.code_value as clientLevelValue, v.maximum_single_deposit_amount as maximumSingleDepositAmount, "
                    + "v.maximum_cumulative_balance as maximumCumulativeBalance, "
                    + "v.maximum_transaction_limit as maximumSingleWithdrawLimit, v.maximum_daily_transaction_amount_limit as maximumDailyWithdrawLimit, "
                    + "v.max_client_specific_daily_withdrawal_limit as maximumClientSpecificDailyWithdrawLimit, v.max_client_specific_single_withdrawal_limit as  maximumClientSpecificSingleWithdrawLimit "
                    + "from m_validation_limits v " + "left join m_code_value cvclientlevel on cvclientlevel.id = v.client_level_cv_id ";

        }

        @Override
        public ValidationLimitData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            BigDecimal maximumSingleDepositAmount = rs.getBigDecimal("maximumSingleDepositAmount");
            BigDecimal maximumCumulativeBalance = rs.getBigDecimal("maximumCumulativeBalance");
            BigDecimal maximumSingleWithdrawLimit = rs.getBigDecimal("maximumSingleWithdrawLimit");
            BigDecimal maximumDailyWithdrawLimit = rs.getBigDecimal("maximumDailyWithdrawLimit");
            BigDecimal maximumClientSpecificDailyWithdrawLimit = rs.getBigDecimal("maximumClientSpecificDailyWithdrawLimit");
            BigDecimal maximumClientSpecificSingleWithdrawLimit = rs.getBigDecimal("maximumClientSpecificSingleWithdrawLimit");
            Long clientLevelId = rs.getLong("clientLevelId");
            String clientLevelValue = rs.getString("clientLevelValue");
            CodeValueData clientLevel = CodeValueData.instance(clientLevelId, clientLevelValue);

            return ValidationLimitData.instance(id, clientLevel, maximumSingleDepositAmount, maximumCumulativeBalance,
                    maximumSingleWithdrawLimit, maximumDailyWithdrawLimit, maximumClientSpecificDailyWithdrawLimit,
                    maximumClientSpecificSingleWithdrawLimit);
        }
    }
}
