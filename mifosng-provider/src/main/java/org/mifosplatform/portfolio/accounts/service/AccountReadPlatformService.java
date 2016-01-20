/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.service;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.portfolio.accounts.data.AccountData;


public interface AccountReadPlatformService {

    public AccountData retrieveOne(Long id) ;
    
    public Collection<AccountData> retrieveAll() ;
    
    public Set<String> getResponseDataParams();
}
