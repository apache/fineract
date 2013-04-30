/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.accounting.autoposting.data.AutoPostingData;
import org.mifosplatform.accounting.closure.exception.GLClosureNotFoundException;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.rule.data.AccountingRuleData;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AutoPostingReadPlatformServiceImpl implements AutoPostingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AutoPostingReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class AutoPostingMapper implements RowMapper<AutoPostingData> {

        private final String schemaSql;

        public AutoPostingMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder
                    .append("select  autoposting.id as id, autoposting.name as name, autoposting.description as description,")
                    .append(" autoposting.office_id as officeId, office.name as officeName, autoposting.product_type_enum as productTypeEnum,")
                    .append(" autoposting.product_id as productId, autoposting.charge_id as chargeId, autoposting.event as event,")
                    .append(" autoposting.event_attribute as eventAttribute, autoposting.accounting_rule_id as accountingRuleId,")
                    .append(" savingsproduct.name as savingProductName, loanproduct.name as loanProductName,")
                    .append(" charge.name as chargeName, charge.is_penalty as isPenalty,")
                    .append(" event.code_name as eventName, eventAttribute.code_value as eventAttributeName")
                    .append(" accountingRule.debit_account_id as debitAccountId, accountingRule.credit_account_id as creditAccountId")
                    .append(" from acc_auto_posting autoposting left join m_office office on autoposting.office_id=office.id")
                    .append(" left join m_savings_product savingsproduct on autoposting.product_id=savingsproduct.id")
                    .append(" left join m_product_loan loanproduct on autoposting.product_id=loanproduct.id")
                    .append(" left join m_charge charge on autoposting.charge_id=charge.id")
                    .append(" left join m_code event on autoposting.event=event.id")
                    .append(" left join m_code_value eventAttribute on autoposting.event_attribute=eventAttribute.id")
                    .append(" left join acc_accounting_rule accountingRule on autoposting.accounting_rule_id= accountingRule.id");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AutoPostingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final int productTypeEnum = rs.getInt("productTypeEnum");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final Long chargeId = JdbcSupport.getLong(rs, "chargeId");
            final Long accountingRuleId = JdbcSupport.getLong(rs, "accountingRuleId");
            final Long eventId = JdbcSupport.getLong(rs, "eventId");
            final Long eventAttributeId = JdbcSupport.getLong(rs, "eventAttributeId");
            final Long debitAccountId = JdbcSupport.getLong(rs, "debitAccountId");
            final Long creditAccountId = JdbcSupport.getLong(rs, "creditAccountId");
            final String savingProductName = rs.getString("savingProductName");
            final String loanProductName = rs.getString("loanProductName");
            final String chargeName = rs.getString("chargeName");
            final String eventName = rs.getString("eventName");
            final String eventAttributeName = rs.getString("eventAttributeName");
            final boolean chargeIsPenalty = rs.getBoolean("isPenalty");

            OfficeData officeData = null;
            if (officeId != null) {
                officeData = OfficeData.dropdown(officeId, officeName, null);
            }

            SavingsProductData savingsProductData = null;
            LoanProductData loanProductData = null;

            PortfolioProductType portfolioProductType = PortfolioProductType.fromInt(productTypeEnum);
            EnumOptionData productType = AccountingEnumerations.portfolioProductType(portfolioProductType);
            if (portfolioProductType.isLoanProduct() && productId != null) {
                savingsProductData = SavingsProductData.lookup(productId, savingProductName);
            } else if (portfolioProductType.isSavingProduct() && productId != null) {
                loanProductData = LoanProductData.lookup(productId, loanProductName);
            }

            ChargeData chargeData = null;
            if (chargeId != null) {
                chargeData = ChargeData.lookup(chargeId, chargeName, chargeIsPenalty);
            }

            CodeData event = CodeData.instance(eventId, eventName, true);

            CodeValueData eventAttribute = null;
            if (eventAttributeId != null) {
                eventAttribute = CodeValueData.instance(eventAttributeId, eventAttributeName);
            }

            AccountingRuleData accountingRuleData = new AccountingRuleData(accountingRuleId, debitAccountId, creditAccountId);

            return new AutoPostingData(id, name, description, officeData, productType, loanProductData, savingsProductData, chargeData,
                    event, eventAttribute, accountingRuleData);
        }
    }

    @Override
    public List<AutoPostingData> retrieveAllAutoPostingRules() {
        final AutoPostingMapper rm = new AutoPostingMapper();

        String sql = "select " + rm.schema();
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public AutoPostingData retrieveAutoPostingRuleById(final long autopostingId) {
        try {

            final AutoPostingMapper rm = new AutoPostingMapper();
            final String sql = "select " + rm.schema() + " and glClosure.id = ?";

            final AutoPostingData autoPostingData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { autopostingId });

            return autoPostingData;
        } catch (final EmptyResultDataAccessException e) {
            throw new GLClosureNotFoundException(autopostingId);
        }
    }

}
