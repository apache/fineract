package org.mifosng.platform.guarantor;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.GuarantorData;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.exceptions.GuarantorNotFoundException;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuarantorReadPlatformServiceImpl implements GuarantorReadPlatformService {

    // Table for storing external Guarantor Details
    public static final String EXTERNAL_GUARANTOR_TABLE_NAME = "m_guarantor_external";
    
    private final LoanRepository loanRepository;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    @Autowired
    public GuarantorReadPlatformServiceImpl(final LoanRepository loanRepository,
            final ReadWriteNonCoreDataService readWriteNonCoreDataService) {
        this.loanRepository = loanRepository;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
    }

    @Override
    public boolean existsGuarantor(Long loanId) {
        Loan loan = retrieveLoanById(loanId);
        // return if internal guarantor exists
        if (loan.getGuarantor() != null) { return true; }
        // return if an external guarantor exists
        if (null != getExternalGuarantor(loanId)) { return true; }
        // else no guarantor exists
        return false;
    }

    @Override
    public GuarantorData retrieveGuarantor(Long loanId) {
        GuarantorData guarantorData = null;
        Loan loan = retrieveLoanById(loanId);
        // does an internal guarantor exist
        if (loan.getGuarantor() != null) {
            Client guarantor = loan.getGuarantor();
            LocalDate localDate = new LocalDate(guarantor.getJoiningDate());
            guarantorData = new GuarantorData(guarantor.getId(), guarantor.getFirstName(), guarantor.getLastName(),
                    guarantor.getExternalId(), guarantor.getOffice().getName(), localDate);
        } else {
            guarantorData = getExternalGuarantor(loanId);
        }
        // throw error if guarantor does not exist
        if (guarantorData == null) { throw new GuarantorNotFoundException(loanId); }
        return guarantorData;
    }

    private Loan retrieveLoanById(final Long loanId) {
        
        // FIXME - KW - On the read side of services - we would prefer not to use the repository/ORM pattern
        Loan loan = loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        return loan;
    }

    @Override
    public GuarantorData getExternalGuarantor(final Long loanId) {
        
        GenericResultsetData genericResultDataSet = readWriteNonCoreDataService.retrieveDataTableGenericResultSet(
                EXTERNAL_GUARANTOR_TABLE_NAME, loanId, null, null);
        if (genericResultDataSet.getData().size() == 1) {
            List<String> guarantorRow = genericResultDataSet.getData().get(0).getRow();
            String firstname = guarantorRow.get(1);
            String lastname = guarantorRow.get(2);

            LocalDate dateOfBirth = null;
            if (!StringUtils.isBlank(guarantorRow.get(3))) {
                dateOfBirth = new LocalDate(guarantorRow.get(3));
            }
            String addressLine1 = guarantorRow.get(4);
            String addressLine2 = guarantorRow.get(5);
            String city = guarantorRow.get(6);
            String state = guarantorRow.get(7);
            String country = guarantorRow.get(8);
            String zip = guarantorRow.get(9);
            String housePhoneNumber = guarantorRow.get(10);
            String mobileNumber = guarantorRow.get(11);
            String comment = guarantorRow.get(12);
            GuarantorData guarantorData = new GuarantorData(firstname, lastname, dateOfBirth, addressLine1, addressLine2, city, state, zip,
                    country, mobileNumber, housePhoneNumber, comment);
            return guarantorData;
        }
        return null;
    }
}