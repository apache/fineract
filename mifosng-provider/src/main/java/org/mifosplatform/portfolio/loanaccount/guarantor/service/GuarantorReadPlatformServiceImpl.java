/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
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

    private static final class GuarantorMapper implements RowMapper<GuarantorData> {

        private final StringBuilder sqlBuilder = new StringBuilder(
                " g.id as id, g.loan_id as loanId, g.client_reln_cv_id clientRelationshipTypeId, g.entity_id as entityId, g.type_enum guarantorType ,g.firstname as firstname, g.lastname as lastname, g.dob as dateOfBirth, g.address_line_1 as addressLine1, g.address_line_2 as addressLine2, g.city as city, g.state as state, g.country as country, g.zip as zip, g.house_phone_number as housePhoneNumber, g.mobile_number as mobilePhoneNumber, g.comment as comment, ")
                .append(" cv.code_value as typeName")//
                .append(" FROM m_guarantor g") //
                .append(" left JOIN m_code_value cv on g.client_reln_cv_id = cv.id");

        public String schema() {
            return this.sqlBuilder.toString();
        }

        @Override
        public GuarantorData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
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
            return new GuarantorData(id, loanId, clientRelationshipType, entityId, guarantorType, firstname, lastname, dob, addressLine1,
                    addressLine2, city, state, zip, country, mobileNumber, housePhoneNumber, comment, null, null, null, null, null);
        }
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
        sql += " where loan_id = ?";
        final List<GuarantorData> guarantorDatas = this.jdbcTemplate.query(sql, rm, new Object[] { loanId });

        final List<GuarantorData> mergedGuarantorDatas = new ArrayList<GuarantorData>();

        for (final GuarantorData guarantorData : guarantorDatas) {
            mergedGuarantorDatas.add(mergeDetailsForClientOrStaffGuarantor(guarantorData));
        }
        return mergedGuarantorDatas;
    }

    @Override
    public GuarantorData retrieveGuarantor(final Long loanId, final Long guarantorId) {
        final GuarantorMapper rm = new GuarantorMapper();
        String sql = "select " + rm.schema();
        sql += " where g.loan_id = ? and g.id = ?";
        final GuarantorData guarantorData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId, guarantorId });

        return mergeDetailsForClientOrStaffGuarantor(guarantorData);
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