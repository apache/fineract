/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductToGLAccountMappingRepository extends JpaRepository<ProductToGLAccountMapping, Long>,
        JpaSpecificationExecutor<ProductToGLAccountMapping> {

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentTypeId(Long productId, int productType,
            int financialAccountType, Long paymentType);

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountTypeAndChargeId(Long productId, int productType,
            int financialAccountType, Long chargeId);

    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=:financialAccountType and mapping.paymentType is NULL and mapping.charge is NULL")
    ProductToGLAccountMapping findCoreProductToFinAccountMapping(@Param("productId") Long productId, @Param("productType") int productType,
            @Param("financialAccountType") int financialAccountType);

    /*** The financial Account Type for a fund source will always be an asset (1) ***/
    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=1 and mapping.paymentType is not NULL")
    List<ProductToGLAccountMapping> findAllPaymentTypeToFundSourceMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    /*** The financial Account Type for income from interest will always be 4 ***/
    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=4 and mapping.charge is not NULL")
    List<ProductToGLAccountMapping> findAllFeeToIncomeAccountMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    /*** The financial Account Type for income from interest will always be 5 ***/
    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=5 and mapping.charge is not NULL")
    List<ProductToGLAccountMapping> findAllPenaltyToIncomeAccountMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    List<ProductToGLAccountMapping> findByProductIdAndProductType(Long productId, int productType);
}
