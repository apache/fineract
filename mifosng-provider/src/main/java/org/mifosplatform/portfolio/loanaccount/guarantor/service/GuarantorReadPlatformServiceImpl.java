package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.mifosplatform.portfolio.loanaccount.guarantor.domain.GuarantorType;
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
    public GuarantorReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final LoanRepository loanRepository) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.loanRepository = loanRepository;
    }

    private static final class GuarantorMapper implements RowMapper<GuarantorData> {

        public String schema() {
            return "  g.id as id, g.loan_id as loanId, g.entity_id as entityId, g.type_enum guarantorType ,g.firstname as firstname,"
                    + " g.lastname as lastname, g.dob as dateOfBirth, g.address_line_1 as addressLine1, g.address_line_2 as addressLine2,"
                    + " g.city as city, g.state as state, g.country as country, g.zip as zip, g.house_phone_number as housePhoneNumber, "
                    + " g.mobile_number as mobilePhoneNumber, g.comment as comment from m_guarantor g";
        }

        @Override
        public GuarantorData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Long loanId = rs.getLong("loanId");
            Integer guarantorTypeId = rs.getInt("guarantorType");
            final EnumOptionData guarantorType = GuarantorEnumerations.guarantorType(guarantorTypeId);
            Long entityId = rs.getLong("entityId");
            String firstname = rs.getString("firstname");
            String lastname = rs.getString("lastname");
            LocalDate dob = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            String addressLine1 = rs.getString("addressLine1");
            String addressLine2 = rs.getString("addressLine2");
            String city = rs.getString("city");
            String state = rs.getString("state");
            String zip = rs.getString("zip");
            String country = rs.getString("country");
            String mobileNumber = rs.getString("mobilePhoneNumber");
            String housePhoneNumber = rs.getString("housePhoneNumber");
            String comment = rs.getString("comment");
            return new GuarantorData(id, loanId, entityId, guarantorType, firstname, lastname, dob, addressLine1, addressLine2, city,
                    state, zip, country, mobileNumber, housePhoneNumber, comment, null, null, null, null);
        }
    }

    @Override
    public List<GuarantorData> retrieveGuarantorsForValidLoan(Long loanId) {
        Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return retrieveGuarantorsForLoan(loanId);
    }

    @Override
    public List<GuarantorData> retrieveGuarantorsForLoan(Long loanId) {
        final GuarantorMapper rm = new GuarantorMapper();
        String sql = "select " + rm.schema();
        sql += " where loan_id = ?";
        List<GuarantorData> guarantorDatas = this.jdbcTemplate.query(sql, rm, new Object[] { loanId });

        List<GuarantorData> mergedGuarantorDatas = new ArrayList<GuarantorData>();

        for (GuarantorData guarantorData : guarantorDatas) {
            mergedGuarantorDatas.add(mergeDetailsForClientOrStaffGuarantor(guarantorData));
        }
        return mergedGuarantorDatas;
    }

    @Override
    public GuarantorData retrieveGuarantor(Long loanId, Long guarantorId) {
        final GuarantorMapper rm = new GuarantorMapper();
        String sql = "select " + rm.schema();
        sql += " where g.loan_id = ? and g.id = ?";
        GuarantorData guarantorData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId, guarantorId });

        return mergeDetailsForClientOrStaffGuarantor(guarantorData);
    }

    @Override
    public GuarantorData retrieveNewGuarantorDetails() {
        final List<EnumOptionData> guarantorTypeOptions = GuarantorEnumerations.guarantorType(GuarantorType.values());
        return GuarantorData.template(guarantorTypeOptions);
    }

    /**
     * @param guarantorData
     */
    private GuarantorData mergeDetailsForClientOrStaffGuarantor(GuarantorData guarantorData) {
        if (guarantorData.isExistingClient()) {
            ClientData clientData = clientReadPlatformService.retrieveIndividualClient(guarantorData.getEntityId());
            return GuarantorData.mergeClientData(clientData, guarantorData);
        } else if (guarantorData.isStaffMember()) {
            StaffData staffData = staffReadPlatformService.retrieveStaff(guarantorData.getEntityId());
            return GuarantorData.mergeStaffData(staffData, guarantorData);
        }
        return guarantorData;
    }

}