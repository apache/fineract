/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.portfolio.shares.domain.ShareProductTempRepository;


public class ShareAccountTempRepository {
    
    private final static ShareAccountTempRepository instance = new ShareAccountTempRepository() ;
    
    private Map<Long, ShareAccount> cache = new HashMap<>() ;
    
    private ShareAccountTempRepository() {
        
    }
    
    public static ShareAccountTempRepository getInstance() {
        return instance ;
    }
    
    public void save(ShareAccount account) {
        Long id = new Long(cache.size()+1) ;
        account.setTempId(id) ;
        cache.put(id, account) ;
        ShareProductTempRepository.getInstance().addAccount(account.getShareProduct().getId(), account) ;
    }
    
    public ShareAccount findOne(Long id) {
        return cache.get(id) ;
    }
    
    public Collection<ShareAccount> findAll() {
        return cache.values() ;
    }
}
