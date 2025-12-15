package ng.com.createsoftware.fn_loaning_service.repository;

import ng.com.createsoftware.fn_loaning_service.model.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    LoanProduct findByProductCode(String productCode);
}
