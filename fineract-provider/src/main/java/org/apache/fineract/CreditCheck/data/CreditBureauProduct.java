package org.apache.fineract.CreditCheck.data;


public class CreditBureauProduct 
{
    
    private final long credit_bureau_product_id;
    
    private final String cb_product_name;
    
    private final long cb_master_id;
    
    
    private CreditBureauProduct(final long credit_bureau_product_id,final String cb_product_name,final long cb_master_id)
    {
    this.credit_bureau_product_id=credit_bureau_product_id;
    this.cb_product_name=cb_product_name;
    this.cb_master_id=cb_master_id;
    }

    public static CreditBureauProduct instance(final long credit_bureau_product_id,final String cb_product_name,final long cb_master_id)
    {
    return new CreditBureauProduct(credit_bureau_product_id,cb_product_name,cb_master_id);
    }
    
    public long getCredit_bureau_product_id() {
        return this.credit_bureau_product_id;
    }

    
    public String getCb_product_name() {
        return this.cb_product_name;
    }

    
    public long getCb_master_id() {
        return this.cb_master_id;
    }
    
    

}
