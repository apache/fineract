package org.mifosplatform.accounting.api.data;

public class ProductToGLAccountMappingData {

    private final Long id;
    private final Long glAccountId;
    private final Long productId;
    private final Integer productType;
    private final Integer financialAccountType;

    public ProductToGLAccountMappingData(Long id, Long glAccountId, Long productId, Integer productType, Integer financialAccountType) {
        super();
        this.id = id;
        this.glAccountId = glAccountId;
        this.productId = productId;
        this.productType = productType;
        this.financialAccountType = financialAccountType;
    }

    public Long getId() {
        return this.id;
    }

    public Long getGlAccountId() {
        return this.glAccountId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Integer getProductType() {
        return this.productType;
    }

    public Integer getFinancialAccountType() {
        return this.financialAccountType;
    }

}