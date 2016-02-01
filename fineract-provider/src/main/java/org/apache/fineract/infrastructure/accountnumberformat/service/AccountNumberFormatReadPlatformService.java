/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.service;

import java.util.List;

import org.mifosplatform.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;

public interface AccountNumberFormatReadPlatformService {

    List<AccountNumberFormatData> getAllAccountNumberFormats();

    AccountNumberFormatData getAccountNumberFormat(Long id);

    AccountNumberFormatData retrieveTemplate(EntityAccountType entityAccountTypeForTemplate);

}
