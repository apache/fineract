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
package org.apache.fineract.portfolio.savingsproductmix.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsProductMixRepository extends JpaRepository<SavingsProductMix, Long>, JpaSpecificationExecutor<SavingsProductMix> {

    public static final String GET_SAVINGSPRODUCTMIXES_BY_PRODUCTID_SCHEMA = "select pm from SavingsProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_SAVINGSPRODUCTIDS_SCHEMA = "Select pm.restrictedProduct.id from SavingsProductMix pm where pm.product.id = :productId";
    public static final String GET_RESTRICTED_SAVINGSPRODUCTS_SCHEMA = "select pm from SavingsProductMix pm where pm.restrictedProduct.id = :restrictedProductId";

    @Query(GET_SAVINGSPRODUCTMIXES_BY_PRODUCTID_SCHEMA)
    List<SavingsProductMix> findByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_SAVINGSPRODUCTIDS_SCHEMA)
    List<Long> findRestrictedProductIdsByProductId(@Param("productId") Long productId);

    @Query(GET_RESTRICTED_SAVINGSPRODUCTS_SCHEMA)
    List<SavingsProductMix> findRestrictedProducts(@Param("restrictedProductId") Long restrictedProductId);

}
