/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.accounts.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.portfolio.shares.domain.ShareProductTempRepository;


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
