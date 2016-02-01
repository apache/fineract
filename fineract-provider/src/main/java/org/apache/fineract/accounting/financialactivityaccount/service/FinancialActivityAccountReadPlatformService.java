/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.service;

import java.util.List;

import org.mifosplatform.accounting.financialactivityaccount.data.FinancialActivityAccountData;

public interface FinancialActivityAccountReadPlatformService {

    List<FinancialActivityAccountData> retrieveAll();

    FinancialActivityAccountData retrieve(Long mappingId);

    FinancialActivityAccountData addTemplateDetails(FinancialActivityAccountData financialActivityAccountData);

    FinancialActivityAccountData getFinancialActivityAccountTemplate();

}