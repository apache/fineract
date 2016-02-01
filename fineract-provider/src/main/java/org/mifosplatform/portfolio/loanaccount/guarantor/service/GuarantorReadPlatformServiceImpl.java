/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.account.domain.AccountAssociationType;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorFundingData;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorTransactionData;
import org.mifosplatform.portfolio.savings.data.DepositAccountOnHoldTransactionData;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GuarantorReadPlatformServiceImpl implements GuarantorReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final LoanRepository loanRepository;

    @Autowired
    public GuarantorReadPlatformServiceImpl(final RoutingDataSource dataSource, final ClientReadPlatformService clientReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final LoanRepository loanRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.loanRepository = loanRepository;
    }

    @Override
    public List<GuarantorData> retrieveGuarantorsForValidLoan(final Long loanId) {
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return retrieveGuarantorsForLoan(loanId);
    }

    @Override
    public List<GuarantorData> retrieveGuarantorsForLoan(final Long loanId) {
        final GuarantorMapper rm = new GuarantorMapper();
        String sql = "select " + rm.schema();
        sql += " where loan_id = ?  group by g.id,gfd.id";
        final List<GuarantorData> guarantorDatas = this.jdbcTemplate.query(sql, rm,
                new Object[] { AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue(),
                        loanId });

        final List<GuarantorData> mergedGuarantorDatas = new ArrayList<>();

        for (final GuarantorData guarantorData : guarantorDatas) {
            mergedGuarantorDatas.add(mergeDetailsForClientOrStaffGuarantor(guarantorData));
        }
        return mergedGuarantorDatas;
    }

    @Override
    public GuarantorData retrieveGuarantor(final Long loanId, final Long guarantorId) {
        final GuarantorMapper rm = new GuarantorMapper();
        String sql = "select " + rm.schema();
        sql += " where g.loan_id = ? and g.id = ? group by g.id,gfd.id";
        final GuarantorData guarantorData = this.jdbcTemplate.queryForObject(sql, rm,
                new Object[] { AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue(),
                        loanId, guarantorId });

        return mergeDetailsForClientOrStaffGuarantor(guarantorData);
    }

    private static final class GuarantorMapper implements RowMapper<GuarantorData> {

        private GuarantorTransactionMapper guarantorTransactionMapper = new GuarantorTransactionMapper();
        private GuarantorFundingMapper guarantorFundingMapper = new GuarantorFundingMapper(guarantorTransactionMapper);

        private final StringBuilder sqlBuilder = new StringBuilder(
                " g.id as id, g.loan_id as loanId, g.client_reln_cv_id clientRelationshipTypeId, g.entity_id as entityId, g.type_enum guarantorType ,g.firstname as firstname, g.lastname as lastname, g.dob as dateOfBirth, g.address_line_1 as addressLine1, g.address_line_2 as addressLine2, g.city as city, g.state as state, g.country as country, g.zip as zip, g.house_phone_number as housePhoneNumber, g.mobile_number as mobilePhoneNumber, g.comment as comment, ")
                .append(" g.is_active as guarantorStatus,")//
                .append(" cv.code_value as typeName, ")//
                .append("gfd.amount,")//
                .append(this.guarantorFundingMapper.schema())//
                .append(",")//
                .append(this.guarantorTransactionMapper.schema())//
                .append(" FROM m_guarantor g") //
                .append(" left JOIN m_code_value cv on g.client_reln_cv_id = cv.id")//
                .append(" left JOIN m_guarantor_funding_details gfd on g.id = gfd.guarantor_id")//
                .append(" left JOIN m_portfolio_account_associations aa on gfd.account_associations_id = aa.id and aa.is_active = 1 and aa.association_type_enum = ?")//
                .append(" left JOIN m_savings_account sa on sa.id = aa.linked_savings_account_id ")//
                .append(" left join m_guarantor_transaction gt on gt.guarantor_fund_detail_id = gfd.id") //
                .append(" left join m_deposit_account_on_hold_transaction oht on oht.id = gt.deposit_on_hold_transaction_id");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public GuarantorData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long loanId = rs.getLong("loanId");
            final Long clientRelationshipTypeId = JdbcSupport.getLong(rs, "clientRelationshipTypeId");
            CodeValueData clientRelationshipType = null;

            if (clientRelationshipTypeId != null) {
                final String typeName = rs.getString("typeName");
                clientRelationshipType = CodeValueData.instance(clientRelationshipTypeId, typeName);
            }

            final Integer guarantorTypeId = rs.getInt("guarantorType");
            final EnumOptionData guarantorType = GuarantorEnumerations.guarantorType(guarantorTypeId);
            final Long entityId = rs.getLong("entityId");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final LocalDate dob = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final String addressLine1 = rs.getString("addressLine1");
            final String addressLine2 = rs.getString("addressLine2");
            final String city = rs.getString("city");
            final String state = rs.getString("state");
            final String zip = rs.getString("zip");
            final String country = rs.getString("country");
            final String mobileNumber = rs.getString("mobilePhoneNumber");
            final String housePhoneNumber = rs.getString("housePhoneNumber");
            final String comment = rs.getString("comment");
            final boolean status = rs.getBoolean("guarantorStatus");
            final Collection<PortfolioAccountData> accountLinkingOptions = null;
            List<GuarantorFundingData> guarantorFundingDetails = null;
            GuarantorFundingData guarantorFundingData = this.guarantorFundingMapper.mapRow(rs, rowNum);
            if (guarantorFundingData != null) {
                guarantorFundingDetails = new ArrayList<>();
                guarantorFundingDetails.add(guarantorFundingData);
                while (rs.next()) {
                    Long tempId = rs.getLong("id");
                    if (tempId.equals(id)) {
                        guarantorFundingData = this.guarantorFundingMapper.mapRow(rs, rowNum);
                        guarantorFundingDetails.add(guarantorFundingData);
                    } else {
                        rs.previous();
                        break;
                    }

                }
            }

           

            return new GuarantorData(id, loanId, clientRelationshipType, entityId, guarantorType, firstname, lastname, dob, addressLine1,
                    addressLine2, city, state, zip, country, mobileNumber, housePhoneNumber, comment, null, null, null, status,
                    guarantorFundingDetails, null, null, accountLinkingOptions);
        }
    }

    private static final class GuarantorFundingMapper implements RowMapper<GuarantorFundingData> {

        private final String sql;
        private final GuarantorTransactionMapper guarantorTransactionMapper;

        public GuarantorFundingMapper(final GuarantorTransactionMapper guarantorTransactionMapper) {
            this.guarantorTransactionMapper = guarantorTransactionMapper;
            StringBuilder sb = new StringBuilder(" gfd.id as gfdId,");
            sb.append(" gfd.amount as amount, gfd.amount_released_derived as amountReleased, ");
            sb.append(" gfd.amount_remaining_derived as amountRemaining, ");
            sb.append(" gfd.amount_transfered_derived as amountTransfered, gfd.status_enum as statusEnum, ");
            sb.append(" sa.id as savingsId, sa.account_no as accountNumber ");
            sql = sb.toString();
        }

        public String schema() {
            return this.sql;
        }

        @Override
        public GuarantorFundingData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            GuarantorFundingData guarantorFundingData = null;
            final Long id = rs.getLong("gfdId");
            if (id != null && id > 0) {
                final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
                final BigDecimal amountReleased = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountReleased");
                final BigDecimal amountRemaining = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountRemaining");
                final BigDecimal amountTransfered = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountTransfered");
                final int statusEnum = rs.getInt("statusEnum");
                final EnumOptionData status = GuarantorEnumerations.guarantorFundStatusType(statusEnum);
                final Long savingsId = rs.getLong("savingsId");
                final String savingsAccountNumber = rs.getString("accountNumber");
                final PortfolioAccountData portfolioAccountData = PortfolioAccountData.lookup(savingsId, savingsAccountNumber);
                List<GuarantorTransactionData> guarantorTransactions = new ArrayList<>();
                if (this.guarantorTransactionMapper != null) {
                    GuarantorTransactionData guarantorTransactionData = this.guarantorTransactionMapper.mapRow(rs, rowNum);
                    if (guarantorTransactionData != null) {
                        guarantorTransactions.add(guarantorTransactionData);
                        while (rs.next()) {
                            final Long tempFundId = rs.getLong("gfdId");
                            if (tempFundId != null && tempFundId.equals(id)) {
                                guarantorTransactionData = this.guarantorTransactionMapper.mapRow(rs, rowNum);
                                guarantorTransactions.add(guarantorTransactionData);
                            } else {
                                rs.previous();
                                break;
                            }
                        }
                    }
                }
                guarantorFundingData = GuarantorFundingData.instance(id, status, portfolioAccountData, amount, amountReleased,
                        amountRemaining, amountTransfered, guarantorTransactions);
            }
            return guarantorFundingData;
        }
    }

    private static final class GuarantorTransactionMapper implements RowMapper<GuarantorTransactionData> {

        private final String sql;

        public GuarantorTransactionMapper() {
            StringBuilder sb = new StringBuilder(" gt.id as gtId,");
            sb.append("gt.is_reversed as reversed,");
            sb.append(" oht.id as ohtId, oht.amount as transactionAmount, ");
            sb.append(" oht.transaction_type_enum as transactionType, ");
            sb.append(" oht.transaction_date as transactionDate, oht.is_reversed as transactionReversed ");
            sql = sb.toString();
        }

        public String schema() {
            return this.sql;
        }

        @Override
        public GuarantorTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            GuarantorTransactionData guarantorTransactionData = null;
            final Long id = rs.getLong("gtId");
            final Long transactionId = rs.getLong("ohtId");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final int transactionTypeEnum = rs.getInt("transactionType");
            EnumOptionData transactionType = SavingsEnumerations.onHoldTransactionType(transactionTypeEnum);
            final boolean reversed = rs.getBoolean("reversed");
            final boolean transactionReversed = rs.getBoolean("transactionReversed");
            if (id != null) {
                DepositAccountOnHoldTransactionData onHoldTransactionData = DepositAccountOnHoldTransactionData.instance(transactionId,
                        amount, transactionType, date, transactionReversed);
                guarantorTransactionData = GuarantorTransactionData.instance(id, onHoldTransactionData, null, reversed);
            }
            return guarantorTransactionData;
        }

    }

    /**
     * @param guarantorData
     */
    private GuarantorData mergeDetailsForClientOrStaffGuarantor(final GuarantorData guarantorData) {
        if (guarantorData.isExistingClient()) {
            final ClientData clientData = this.clientReadPlatformService.retrieveOne(guarantorData.getEntityId());
            return GuarantorData.mergeClientData(clientData, guarantorData);
        } else if (guarantorData.isStaffMember()) {
            final StaffData staffData = this.staffReadPlatformService.retrieveStaff(guarantorData.getEntityId());
            return GuarantorData.mergeStaffData(staffData, guarantorData);
        }
        return guarantorData;
    }

}