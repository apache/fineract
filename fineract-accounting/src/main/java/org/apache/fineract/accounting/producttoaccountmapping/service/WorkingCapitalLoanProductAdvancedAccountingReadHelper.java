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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.producttoaccountmapping.data.AdvancedMappingToExpenseAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanProductAdvancedAccountingReadHelper {

    private final ProductToGLAccountMappingRepository productToGLAccountMappingRepository;
    private final CodeValueMapper codeValueMapper;
    private final GLAccountRepository glAccountRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappings(final Long wcLoanProductId) {
        final List<ProductToGLAccountMapping> mappings = productToGLAccountMappingRepository.findAllPaymentTypeMappings(wcLoanProductId,
                PortfolioProductType.WORKING_CAPITAL_LOAN.getValue());
        final List<PaymentTypeToGLAccountMapper> result = new ArrayList<>();
        for (final ProductToGLAccountMapping mapping : mappings) {
            final PaymentTypeData paymentTypeData = PaymentTypeData.builder().id(mapping.getPaymentType().getId())
                    .name(mapping.getPaymentType().getName()).build();
            final GLAccountData gLAccountData = new GLAccountData().setId(mapping.getGlAccount().getId())
                    .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());
            result.add(new PaymentTypeToGLAccountMapper().setPaymentType(paymentTypeData).setFundSourceAccount(gLAccountData));
        }
        return result.isEmpty() ? null : result;
    }

    public List<ChargeToGLAccountMapper> fetchFeeToIncomeMappings(final Long wcLoanProductId) {
        return fetchChargeToIncomeMappings(wcLoanProductId, false);
    }

    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeMappings(final Long wcLoanProductId) {
        return fetchChargeToIncomeMappings(wcLoanProductId, true);
    }

    public List<AdvancedMappingToExpenseAccountData> fetchChargeOffReasonMappings(final Long wcLoanProductId) {
        return fetchReasonMappings(productToGLAccountMappingRepository.findAllChargeOffReasonsMappings(wcLoanProductId,
                PortfolioProductType.WORKING_CAPITAL_LOAN.getValue()));
    }

    public List<AdvancedMappingToExpenseAccountData> fetchWriteOffReasonMappings(final Long wcLoanProductId) {
        return fetchReasonMappings(productToGLAccountMappingRepository.findAllWriteOffReasonsMappings(wcLoanProductId,
                PortfolioProductType.WORKING_CAPITAL_LOAN.getValue()));
    }

    private List<ChargeToGLAccountMapper> fetchChargeToIncomeMappings(final Long wcLoanProductId, final boolean penalty) {
        // The charge name lives on m_charge, not on ProductToGLAccountMapping (which only stores charge_id), and the
        // accounting module is decoupled from the charge domain, so we read the charge -> income-account rows via
        // JdbcTemplate rather than a JPA native query (EclipseLink does not bind Spring Data ":name" markers in native
        // queries). Column order: [0] gl_account_id, [1] charge id, [2] charge name. The penalty flag is known from the
        // method argument, so it is inlined as a boolean literal (never user input) rather than bound.
        final String sql = "select apm.gl_account_id, c.id, c.name "
                + "from acc_product_mapping apm inner join m_charge c on c.id = apm.charge_id "
                + "where apm.product_id = ? and apm.product_type = ? and c.is_penalty = " + penalty;
        final List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] { rs.getLong(1), rs.getLong(2), rs.getString(3) },
                wcLoanProductId, PortfolioProductType.WORKING_CAPITAL_LOAN.getValue());

        final List<ChargeToGLAccountMapper> result = new ArrayList<>();
        for (final Object[] row : rows) {
            final Long glAccountId = (Long) row[0];
            final Long chargeId = (Long) row[1];
            final String chargeName = (String) row[2];
            final GLAccount glAccount = glAccountRepository.findById(glAccountId).orElseThrow();
            final GLAccountData gLAccountData = new GLAccountData().setId(glAccount.getId()).setName(glAccount.getName())
                    .setGlCode(glAccount.getGlCode());
            final ChargeData chargeData = ChargeData.builder().id(chargeId).name(chargeName).penalty(penalty).build();
            result.add(new ChargeToGLAccountMapper().setCharge(chargeData).setIncomeAccount(gLAccountData));
        }
        return result.isEmpty() ? null : result;
    }

    private List<AdvancedMappingToExpenseAccountData> fetchReasonMappings(final List<ProductToGLAccountMapping> mappings) {
        final List<AdvancedMappingToExpenseAccountData> result = new ArrayList<>();
        for (final ProductToGLAccountMapping mapping : mappings) {
            final GLAccountData expenseAccount = new GLAccountData().setId(mapping.getGlAccount().getId())
                    .setName(mapping.getGlAccount().getName()).setGlCode(mapping.getGlAccount().getGlCode());
            final CodeValueData codeValue = mapping.getChargeOffReason() != null ? codeValueMapper.map(mapping.getChargeOffReason())
                    : codeValueMapper.map(mapping.getWriteOffReason());
            result.add(new AdvancedMappingToExpenseAccountData().setReasonCodeValue(codeValue).setExpenseAccount(expenseAccount));
        }
        return result.isEmpty() ? null : result;
    }
}
