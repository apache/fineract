package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;

public interface GuarantorReadPlatformService {

    /**
     * Validates the passed in loanId before retrieving Loans for the same
     * 
     * @param loanId
     * @return
     */
    List<GuarantorData> retrieveGuarantorsForValidLoan(Long loanId);

    /**
     * Methods Returns all Guarantors for a Given loan Id (if the loan Id is
     * valid and Exists)
     * 
     * @param loanId
     * @return
     */
    List<GuarantorData> retrieveGuarantorsForLoan(Long loanId);

    GuarantorData retrieveGuarantor(Long loanId, Long guarantorId);

    GuarantorData retrieveNewGuarantorDetails();

}