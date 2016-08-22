package org.apache.fineract.CreditCheck.data;

public class CreditBureauData {

    private final long creditBureauId;

    private final String creditBureauName;

    private final String country;

    private final String productName;
    
    private final String cbSummary;

    private final long implementationkey;

    private CreditBureauData(final long creditBureauId, final String creditBureauName, final String country, final String productName,
            final String cbSummary, final long implementationkey) {
        this.creditBureauId = creditBureauId;
        this.creditBureauName = creditBureauName;
        this.country = country;
        this.productName = productName;
        this.cbSummary=cbSummary;
        this.implementationkey = implementationkey;

    }

    public static CreditBureauData instance(final long creditbureau_id, final String creditbureau_name, final String country,
            final String product_name, final String cbSummary,final long implementation_key) {

        return new CreditBureauData(creditbureau_id, creditbureau_name, country, product_name,cbSummary, implementation_key);
    }

    
    
    public String getCbSummary() {
        return this.cbSummary;
    }

    public long getCreditBureauId() {
        return this.creditBureauId;
    }

    
    public String getCreditBureauName() {
        return this.creditBureauName;
    }

    
    public String getCountry() {
        return this.country;
    }

    
    public String getProductName() {
        return this.productName;
    }

    
    public long getImplementationkey() {
        return this.implementationkey;
    }

    
}
