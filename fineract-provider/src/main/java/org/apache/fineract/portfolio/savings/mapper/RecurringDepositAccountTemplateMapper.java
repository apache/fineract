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
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

public class RecurringDepositAccountTemplateMapper extends DepositAccountTemplateMapper {

    public static final String PRE_CLOSURE_PENAL_APPLICABLE = "preClosurePenalApplicable";
    public static final String PRE_CLOSURE_PENAL_INTEREST = "preClosurePenalInterest";
    public static final String PRE_CLOSURE_PENAL_INTEREST_ON_ID = "preClosurePenalInterestOnId";
    public static final String MIN_DEPOSIT_TERM = "minDepositTerm";
    public static final String MAX_DEPOSIT_TERM = "maxDepositTerm";
    public static final String MIN_DEPOSIT_TERM_TYPE_ID = "minDepositTermTypeId";
    public static final String MAX_DEPOSIT_TERM_TYPE_ID = "maxDepositTermTypeId";
    public static final String IN_MULTIPLES_OF_DEPOSIT_TERM = "inMultiplesOfDepositTerm";
    public static final String IN_MULTIPLES_OF_DEPOSIT_TERM_TYPE_ID = "inMultiplesOfDepositTermTypeId";
    public static final String IS_MANDATORY_DEPOSIT = "isMandatoryDeposit";
    public static final String ALLOW_WITHDRAWAL = "allowWithdrawal";
    public static final String ADJUST_ADVANCE_TOWARDS_FUTURE_PAYMENTS = "adjustAdvanceTowardsFuturePayments";
    private final String schemaSql;

    public RecurringDepositAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
        super(client, group);
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append(this.selectFieldsSql());

        sqlBuilder.append(", dptp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
        sqlBuilder.append("dptp.pre_closure_penal_interest as preClosurePenalInterest, ");
        sqlBuilder.append("dptp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
        sqlBuilder.append("dprd.is_mandatory as isMandatoryDeposit, ");
        sqlBuilder.append("dprd.allow_withdrawal as allowWithdrawal, ");
        sqlBuilder.append("dprd.adjust_advance_towards_future_payments as adjustAdvanceTowardsFuturePayments, ");
        sqlBuilder.append("dptp.min_deposit_term as minDepositTerm, ");
        sqlBuilder.append("dptp.max_deposit_term as maxDepositTerm, ");
        sqlBuilder.append("dptp.min_deposit_term_type_enum as minDepositTermTypeId, ");
        sqlBuilder.append("dptp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
        sqlBuilder.append("dptp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
        sqlBuilder.append("dptp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId ");

        sqlBuilder.append(this.selectTablesSql());
        sqlBuilder.append("left join m_deposit_product_recurring_detail dprd on sa.id = dprd.savings_product_id ");

        this.schemaSql = sqlBuilder.toString();
    }

    @Override
    public String schema() {
        return this.schemaSql;
    }

    @Override
    public RecurringDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

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
        final boolean isMandatoryDeposit = rs.getBoolean(IS_MANDATORY_DEPOSIT);
        final boolean allowWithdrawal = rs.getBoolean(ALLOW_WITHDRAWAL);
        final boolean adjustAdvanceTowardsFuturePayments = rs.getBoolean(ADJUST_ADVANCE_TOWARDS_FUTURE_PAYMENTS);
        final boolean isCalendarInherited = false;
        final boolean isRateChartOverridden = false;

        final BigDecimal depositAmount = null;
        final BigDecimal maturityAmount = null;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = null;
        final EnumOptionData depositPeriodFrequencyType = null;
        final LocalDate expectedFirstDepositOnDate = null;
        final BigDecimal mandatoryRecommendedDepositAmount = null;
        final EnumOptionData onAccountClosureType = null;
        final BigDecimal totalOverdueAmount = null;
        final Integer noOfOverdueInstallments = null;

        return RecurringDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                depositPeriodFrequencyType, mandatoryRecommendedDepositAmount, onAccountClosureType, expectedFirstDepositOnDate,
                totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments,
                isCalendarInherited, isRateChartOverridden);
    }
}
