package org.mifosng.platform.guarantor;

import org.mifosng.platform.api.data.GuarantorData;

public interface GuarantorReadPlatformService {

	GuarantorData retrieveGuarantor(Long loanId);

	boolean existsGuarantor(Long loanId);

	GuarantorData getExternalGuarantor(Long loanId);

}