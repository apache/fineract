/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savings.data.SavingsAccountData;

public interface SavingsAccountReadPlatformService {

    Collection<SavingsAccountData> retrieveAll();

    SavingsAccountData retrieveOne(Long accountId);

    SavingsAccountData retrieveTemplate(Long clientId, Long groupId, Long productId);
}