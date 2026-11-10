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
package org.apache.fineract.portfolio.savings.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.FixedDepositAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

public class FixedDepositAccountMapper extends DepositAccountMapper {

    public static final String PRE_CLOSURE_PENAL_APPLICABLE = "preClosurePenalApplicable";
    public static final String PRE_CLOSURE_PENAL_INTEREST = "preClosurePenalInterest";
    public static final String PRE_CLOSURE_PENAL_INTEREST_ON_ID = "preClosurePenalInterestOnId";
    public static final String MIN_DEPOSIT_TERM = "minDepositTerm";
    public static final String MAX_DEPOSIT_TERM = "maxDepositTerm";
    public static final String MIN_DEPOSIT_TERM_TYPE_ID = "minDepositTermTypeId";
    public static final String MAX_DEPOSIT_TERM_TYPE_ID = "maxDepositTermTypeId";
    public static final String IN_MULTIPLES_OF_DEPOSIT_TERM = "inMultiplesOfDepositTerm";
    public static final String IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_ID = "inMultiplesOfDepositTermTypeId";
    public static final String DEPOSIT_AMOUNT = "depositAmount";
    public static final String MATURITY_AMOUNT = "maturityAmount";
    public static final String MATURITY_DATE = "maturityDate";
    public static final String DEPOSIT_PERIOD = "depositPeriod";
    public static final String DEPOSIT_PERIOD_FREQUENCY_TYPE_ID = "depositPeriodFrequencyTypeId";
    public static final String ON_ACCOUNT_CLOSURE_ID = "onAccountClosureId";
    public static final String TRANSFER_INTEREST_TO_SAVINGS = "transferInterestToSavings";
    public static final String TRANSFER_TO_SAVINGS_ID = "transferToSavingsId";
    public static final String IS_RATE_CHART_OVERRIDDEN = "isRateChartOverridden";
    private final String schemaSql;

    public FixedDepositAccountMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append(super.selectFieldsSql());

        sqlBuilder.append(", datp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
        sqlBuilder.append("datp.pre_closure_penal_interest as preClosurePenalInterest, ");
        sqlBuilder.append("datp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
        sqlBuilder.append("datp.min_deposit_term as minDepositTerm, ");
        sqlBuilder.append("datp.max_deposit_term as maxDepositTerm, ");
        sqlBuilder.append("datp.min_deposit_term_type_enum as minDepositTermTypeId, ");
        sqlBuilder.append("datp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
        sqlBuilder.append("datp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
        sqlBuilder.append("datp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId, ");
        sqlBuilder.append("datp.deposit_amount as depositAmount, ");
        sqlBuilder.append("datp.maturity_amount as maturityAmount, ");
        sqlBuilder.append("datp.maturity_date as maturityDate, ");
        sqlBuilder.append("datp.deposit_period as depositPeriod, ");
        sqlBuilder.append("datp.deposit_period_frequency_enum as depositPeriodFrequencyTypeId, ");
        sqlBuilder.append("datp.on_account_closure_enum as onAccountClosureId, ");
        sqlBuilder.append("datp.transfer_interest_to_linked_account as transferInterestToSavings, ");
        sqlBuilder.append("datp.transfer_to_savings_account_id as transferToSavingsId, ");
        sqlBuilder.append("datp.is_rate_chart_overridden as isRateChartOverridden ");

        sqlBuilder.append(super.selectTablesSql());

        this.schemaSql = sqlBuilder.toString();
    }

    @Override
    public String schema() {
        return this.schemaSql;
    }

    @Override
    public FixedDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final DepositAccountData depositAccountData = super.mapRow(rs);
        final boolean preClosurePenalApplicable = rs.getBoolean(PRE_CLOSURE_PENAL_APPLICABLE);
        final BigDecimal preClosurePenalInterest = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, PRE_CLOSURE_PENAL_INTEREST);
        final Integer preClosurePenalInterestOnTypeId = JdbcSupport.getInteger(rs, PRE_CLOSURE_PENAL_INTEREST_ON_ID);
        final EnumOptionData preClosurePenalInterestOnType = (preClosurePenalInterestOnTypeId == null) ? null
                : SavingsEnumerations.preClosurePenaltyInterestOnType(preClosurePenalInterestOnTypeId);

        final Integer minDepositTerm = JdbcSupport.getInteger(rs, MIN_DEPOSIT_TERM);
        final Integer maxDepositTerm = JdbcSupport.getInteger(rs, MAX_DEPOSIT_TERM);
        final Integer minDepositTermTypeId = JdbcSupport.getInteger(rs, MIN_DEPOSIT_TERM_TYPE_ID);
        final EnumOptionData minDepositTermType = (minDepositTermTypeId == null) ? null
                : SavingsEnumerations.depositTermFrequencyType(minDepositTermTypeId);
        final Integer maxDepositTermTypeId = JdbcSupport.getInteger(rs, MAX_DEPOSIT_TERM_TYPE_ID);
        final EnumOptionData maxDepositTermType = (maxDepositTermTypeId == null) ? null
                : SavingsEnumerations.depositTermFrequencyType(maxDepositTermTypeId);
        final Integer inMultiplesOfDepositTerm = JdbcSupport.getInteger(rs, IN_MULTIPLES_OF_DEPOSIT_TERM);
        final Integer inMultiplesOfDepositTermTypeId = JdbcSupport.getInteger(rs, IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_ID);
        final EnumOptionData inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null
                : SavingsEnumerations.depositTermFrequencyType(inMultiplesOfDepositTermTypeId);

        final BigDecimal depositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, DEPOSIT_AMOUNT);
        final BigDecimal maturityAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, MATURITY_AMOUNT);
        final LocalDate maturityDate = JdbcSupport.getLocalDate(rs, MATURITY_DATE);
        final Integer depositPeriod = JdbcSupport.getInteger(rs, DEPOSIT_PERIOD);
        final Integer depositPeriodFrequencyTypeId = JdbcSupport.getInteger(rs, DEPOSIT_PERIOD_FREQUENCY_TYPE_ID);
        final EnumOptionData depositPeriodFrequencyType = (depositPeriodFrequencyTypeId == null) ? null
                : SavingsEnumerations.depositPeriodFrequency(depositPeriodFrequencyTypeId);

        final Integer onAccountClosureId = JdbcSupport.getInteger(rs, ON_ACCOUNT_CLOSURE_ID);
        final EnumOptionData onAccountClosureType = (onAccountClosureId == null) ? null
                : SavingsEnumerations.depositAccountOnClosureType(onAccountClosureId);
        final Boolean transferInterestToSavings = rs.getBoolean(TRANSFER_INTEREST_TO_SAVINGS);

        final Long transferToSavingsId = JdbcSupport.getLong(rs, TRANSFER_TO_SAVINGS_ID);

        final boolean isRateChartOverridden = rs.getBoolean(IS_RATE_CHART_OVERRIDDEN);

        return FixedDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                depositPeriodFrequencyType, onAccountClosureType, transferInterestToSavings, transferToSavingsId, isRateChartOverridden);
    }
}
