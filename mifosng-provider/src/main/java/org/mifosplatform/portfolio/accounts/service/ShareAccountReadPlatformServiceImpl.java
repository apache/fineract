/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.accounts.constants.AccountsApiConstants;
import org.mifosplatform.portfolio.accounts.data.AccountData;
import org.mifosplatform.portfolio.accounts.domain.ShareAccount;
import org.mifosplatform.portfolio.accounts.domain.ShareAccountTempRepository;
import org.springframework.stereotype.Service;

@Service(value = "share"+AccountsApiConstants.READPLATFORM_NAME)
public class ShareAccountReadPlatformServiceImpl implements AccountReadPlatformService{

    @Override
    public AccountData retrieveOne(Long id) {
        return ShareAccountTempRepository.getInstance().findOne(id).toData();
    }

    @Override
    public Collection<AccountData> retrieveAll() {
        Collection<ShareAccount> collection = ShareAccountTempRepository.getInstance().findAll() ;
        Set<AccountData> set = new HashSet<>() ;
        for(ShareAccount data: collection) {
            set.add(data.toData()) ;
        }
        return set;
    }

    @Override
    public Set<String> getResponseDataParams() {
        return null;
    }
}
