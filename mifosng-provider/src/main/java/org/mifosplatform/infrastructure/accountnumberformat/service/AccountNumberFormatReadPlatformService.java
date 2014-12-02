package org.mifosplatform.infrastructure.accountnumberformat.service;

import java.util.List;

import org.mifosplatform.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;

public interface AccountNumberFormatReadPlatformService {

    List<AccountNumberFormatData> getAllAccountNumberFormats();

    AccountNumberFormatData getAccountNumberFormat(Long id);

    AccountNumberFormatData retrieveTemplate(EntityAccountType entityAccountTypeForTemplate);

}
