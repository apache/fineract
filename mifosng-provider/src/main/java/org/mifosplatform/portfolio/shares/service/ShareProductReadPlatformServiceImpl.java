/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mifosplatform.portfolio.products.data.ProductData;
import org.mifosplatform.portfolio.products.service.ProductReadPlatformService;
import org.mifosplatform.portfolio.shares.domain.ShareProduct;
import org.mifosplatform.portfolio.shares.domain.ShareProductTempRepository;
import org.springframework.stereotype.Service;


@Service(value = "shareReadPlatformService")
public class ShareProductReadPlatformServiceImpl implements ProductReadPlatformService{

    ShareProductTempRepository repo = ShareProductTempRepository.getInstance() ;
    
    @Override
    public Collection<ProductData> retrieveAllProducts() {
        Collection<ShareProduct> entities = repo.findAll() ;
        List<ProductData> toReturn = new ArrayList<>() ;
        for(ShareProduct entity: entities) {
            toReturn.add(entity.toData()) ;
        }
        return toReturn;
    }

    @Override
    public ProductData retrieveOne(Long productId) {
        ShareProduct product = repo.fineOne(productId) ;
        return product.toData() ;
    }

    @Override
    public ProductData retrieveTemplate() {
        return null;
    }

    @Override
    public Set<String> getResponseDataParams() {
        return null;
    }

}
