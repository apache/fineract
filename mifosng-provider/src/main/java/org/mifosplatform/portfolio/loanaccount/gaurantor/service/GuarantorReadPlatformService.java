package org.mifosplatform.portfolio.loanaccount.gaurantor.service;

import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;

public interface GuarantorReadPlatformService {

    GuarantorData retrieveGuarantor(Long loanId);

    boolean existsGuarantor(Long loanId);

    GuarantorData getExternalGuarantor(Long loanId);

}