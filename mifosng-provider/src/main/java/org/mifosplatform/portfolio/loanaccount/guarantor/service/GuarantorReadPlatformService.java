package org.mifosplatform.portfolio.loanaccount.guarantor.service;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.guarantor.data.GuarantorData;

public interface GuarantorReadPlatformService {

    List<GuarantorData> retrieveGuarantorsForLoan(Long loanId);

    GuarantorData retrieveGuarantor(Long guarantorId);

    GuarantorData retrieveNewGuarantorDetails();

}