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

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountTypeAndPaymentType(Long productId, int productType,
            int financialAccountType, Long paymentType);

    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=:financialAccountType and mapping.paymentType is NULL")
    ProductToGLAccountMapping findCoreProductToFinAccountMapping(@Param("productId") Long productId, @Param("productType") int productType,
            @Param("financialAccountType") int financialAccountType);

    /*** The financial Account Type for a fund source will always be an asset (1) ***/
    @Query("from ProductToGLAccountMapping mapping where mapping.productId =:productId and mapping.productType =:productType and mapping.financialAccountType=1 and mapping.paymentType is not NULL")
    List<ProductToGLAccountMapping> findAllPaymentTypeToFundSourceMappings(@Param("productId") Long productId,
            @Param("productType") int productType);

    List<ProductToGLAccountMapping> findByProductIdAndProductType(Long productId, int productType);
}
