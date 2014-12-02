package org.mifosplatform.infrastructure.accountnumberformat.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface AccountNumberFormatWritePlatformService {

    CommandProcessingResult createAccountNumberFormat(JsonCommand command);

    CommandProcessingResult updateAccountNumberFormat(Long accountNumberFormatId, JsonCommand command);

    CommandProcessingResult deleteAccountNumberFormat(Long accountNumberFormatId);

}
