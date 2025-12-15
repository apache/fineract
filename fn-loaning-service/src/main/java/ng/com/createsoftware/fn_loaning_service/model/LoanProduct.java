package ng.com.createsoftware.fn_loaning_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="loan_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;
    private String name;
    private Double interestRate;
    private Integer maxTenure;
    private Double maxAmount;

    public LoanProduct(String productCode, String name, Double interestRate, Integer maxTenure, Double maxAmount) {
        this.productCode = productCode;
        this.name = name;
        this.interestRate = interestRate;
        this.maxTenure = maxTenure;
        this.maxAmount = maxAmount;
    }
}
