/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.productmix.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMixRepository extends JpaRepository<ProductMix, Long>, JpaSpecificationExecutor<ProductMix> {

    public static final String GET_PRODUCTMIXES_BY_PRODUCTID_SCHEMA = "from ProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_PRODUCTIDS_SCHEMA = "Select pm.restrictedProduct.id from ProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_PRODUCTS_SCHEMA = "from ProductMix pm where pm.restrictedProduct.id = :restrictedProductId";

    @Query(GET_PRODUCTMIXES_BY_PRODUCTID_SCHEMA)
    List<ProductMix> findByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_PRODUCTIDS_SCHEMA)
    List<Long> findRestrictedProductIdsByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_PRODUCTS_SCHEMA)
    List<ProductMix> findRestrictedProducts(@Param("restrictedProductId") Long restrictedProductId);

}
