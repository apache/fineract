/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.portfolio.accounts.domain.ShareAccount;
import org.mifosplatform.portfolio.shares.data.ProductDividendsData;


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
