package org.mifosplatform.portfolio.loanaccount.gaurantor.service;

import java.util.List;

import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;

public interface GuarantorReadPlatformService {

    List<GuarantorData> retrieveGuarantorsForLoan(Long loanId);

    GuarantorData retrieveGuarantor(Long guarantorId);

    GuarantorData retrieveNewGuarantorDetails();

}