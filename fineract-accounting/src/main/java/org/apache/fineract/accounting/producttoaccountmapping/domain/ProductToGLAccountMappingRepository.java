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
package org.apache.fineract.accounting.producttoaccountmapping.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductToGLAccountMappingRepository
        extends JpaRepository<ProductToGLAccountMapping, Long>, JpaSpecificationExecutor<ProductToGLAccountMapping> {

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(Long productId, int productType,
            int financialAccountType, Long paymentType);

    @Query("select mapping from ProductToGLAccountMapping mapping where mapping.productId= :productId and mapping.productType= :productType and mapping.financialAccountType= :financialAccountType and mapping.charge.id= :chargeId")
    ProductToGLAccountMapping findProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(@Param("productId") Long productId,
            @Param("productType") int productType, @Param("financialAccountType") int financialAccountType,
            @Param("chargeId") Long ChargeId);

    @Query("select mapping from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=:financialAccountType and mapping.paymentType is NULL and mapping.charge is NULL")
    ProductToGLAccountMapping findCoreProductToFinAccountMapping(@Param("productId") Long productId, @Param("productType") int productType,
            @Param("financialAccountType") int financialAccountType);

    /***
     * The financial Account Type for a fund source will always be an asset (1)
     ***/
    @Query("select mapping from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=1 and mapping.paymentType is not NULL")
    List<ProductToGLAccountMapping> findAllPaymentTypeToFundSourceMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    /***
     * The financial Account Type for income from interest will always be 4
     ***/
    @Query("select mapping from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=4 and mapping.charge is not NULL")
    List<ProductToGLAccountMapping> findAllFeeToIncomeAccountMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    /***
     * The financial Account Type for income from interest will always be 5
     ***/
    @Query("select mapping from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=5 and mapping.charge is not NULL")
    List<ProductToGLAccountMapping> findAllPenaltyToIncomeAccountMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    List<ProductToGLAccountMapping> findByProductIdAndProductType(Long productId, int productType);
}
