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
package org.apache.fineract.portfolio.loanproduct.productmix.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMixRepository extends JpaRepository<ProductMix, Long>, JpaSpecificationExecutor<ProductMix> {

    public static final String GET_PRODUCTMIXES_BY_PRODUCTID_SCHEMA = "select pm from ProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_PRODUCTIDS_SCHEMA = "Select pm.restrictedProduct.id from ProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_PRODUCTS_SCHEMA = "select pm from ProductMix pm where pm.restrictedProduct.id = :restrictedProductId";

    @Query(GET_PRODUCTMIXES_BY_PRODUCTID_SCHEMA)
    List<ProductMix> findByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_PRODUCTIDS_SCHEMA)
    List<Long> findRestrictedProductIdsByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_PRODUCTS_SCHEMA)
    List<ProductMix> findRestrictedProducts(@Param("restrictedProductId") Long restrictedProductId);

}
