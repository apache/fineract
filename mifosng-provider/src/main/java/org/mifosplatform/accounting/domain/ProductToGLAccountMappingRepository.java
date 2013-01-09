package org.mifosplatform.accounting.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductToGLAccountMappingRepository extends JpaRepository<ProductToGLAccountMapping, Long>,
        JpaSpecificationExecutor<ProductToGLAccountMapping> {

    ProductToGLAccountMapping findByProductIdAndProductTypeAndFinancialAccountType(Long productId, int productType, int financialAccountType);

    List<ProductToGLAccountMapping> findByProductIdAndProductType(Long productId, int productType);
}
