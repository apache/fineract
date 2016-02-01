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
package org.apache.fineract.portfolio.shares.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.portfolio.accounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shares.data.ProductDividendsData;


public class ShareProductTempRepository {

    Map<Long, ShareProduct> cache = new HashMap<>() ;
    Map<Long, ArrayList<ShareAccount>> accountsCache = new HashMap<>() ;
    Map<Long, ProductDividendsData> dividendsCahe = new HashMap<>() ;
    
    private final static ShareProductTempRepository instance = new ShareProductTempRepository() ;
    
    private ShareProductTempRepository() {
        
    }
    
    public final static ShareProductTempRepository getInstance() {
        return instance ;
    }
    
    public ShareProduct fineOne(Long productId) {
        return this.cache.get(productId) ;
    }
    
    public Collection<ShareProduct> findAll() {
        return this.cache.values() ;
    }
    public void save(ShareProduct product) {
        Long id = new Long(cache.size() + 1) ;
        product.setTempId(id) ;
        this.cache.put(id, product) ;
    }
    
    public void addAccount(Long productId, ShareAccount account) {
        if(accountsCache.containsKey(productId)) {
            ArrayList<ShareAccount> list = accountsCache.get(productId) ;
            list.add(account) ;
        }else {
            ArrayList<ShareAccount> list = new ArrayList<>() ;
            list.add(account) ;
            accountsCache.put(productId, list) ;
        }
    }
    
    public ArrayList<ShareAccount> getAllAccounts(Long productId) {
        return accountsCache.get(productId) ; 
    }
    
    public void saveDividends(ProductDividendsData data) {
        Long id = new Long(dividendsCahe.size()+1) ;
        data.setId(id) ;
        dividendsCahe.put(data.getProductId(), data) ;
    }
}
