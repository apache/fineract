/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductToGLAccountMappingRepository extends JpaRepository<ProductToGLAccountMapping, Long>,
        JpaSpecificationExecutor<ProductToGLAccountMapping> {

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountType(Long productId, int productType, int financialAccountType);

    List<ProductToGLAccountMapping> findByProductIdAndProductType(Long productId, int productType);
}
