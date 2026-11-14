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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.springframework.jdbc.core.RowMapper;

final class LoanDisbursementDetailMapper implements RowMapper<DisbursementData> {

    private final DatabaseSpecificSQLGenerator sqlGenerator;

    LoanDisbursementDetailMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public String schema() {
        return "dd.id as id, dd.loan_id as loanId, dd.expected_disburse_date as expectedDisbursementdate, dd.disbursedon_date as actualDisbursementdate, "
                + "dd.principal as principal,dd.net_disbursal_amount as netDisbursalAmount, sum(lc.amount) as chargeAmount, coalesce(sum(cad.amount), 0) as disburseChargeAmount, "
                + "lc.amount_waived_derived waivedAmount, coalesce(" + sqlGenerator.groupConcat("lc.id") + ", "
                + sqlGenerator.groupConcat("cad.id") + ") loanChargeId "
                + "from m_loan l inner join m_loan_disbursement_detail dd on dd.loan_id = l.id "
                + "left join m_loan_tranche_disbursement_charge tdc on tdc.disbursement_detail_id = dd.id "
                + "left join m_loan_charge lc on  lc.id = tdc.loan_charge_id and lc.is_active = true "
                + "left join (select lch.id, lch.loan_id, lch.amount from m_loan_charge lch inner join m_charge ch on ch.id = lch.charge_id and ch.charge_time_enum = 1) cad on cad.loan_id = l.id";
    }

    @Override
    public DisbursementData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        final Long id = rs.getLong("id");
        final Long loanId = rs.getLong("loanId");
        final LocalDate expectedDisbursementdate = JdbcSupport.getLocalDate(rs, "expectedDisbursementdate");
        final LocalDate actualDisbursementdate = JdbcSupport.getLocalDate(rs, "actualDisbursementdate");
        final BigDecimal principal = rs.getBigDecimal("principal");
        final String loanChargeId = rs.getString("loanChargeId");
        final BigDecimal netDisbursalAmount = rs.getBigDecimal("netDisbursalAmount");
        BigDecimal chargeAmount = rs.getBigDecimal("chargeAmount");
        final BigDecimal waivedAmount = rs.getBigDecimal("waivedAmount");
        if (chargeAmount != null && waivedAmount != null) {
            chargeAmount = chargeAmount.subtract(waivedAmount);
        }
        BigDecimal disburseChargeAmount = rs.getBigDecimal("disburseChargeAmount");
        return new DisbursementData(id, loanId, expectedDisbursementdate, actualDisbursementdate, principal, netDisbursalAmount,
                loanChargeId, chargeAmount, waivedAmount).setDisburseChargeAmount(disburseChargeAmount);
    }

}
