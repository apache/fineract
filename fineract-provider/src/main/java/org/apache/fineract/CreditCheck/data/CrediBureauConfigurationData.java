package org.apache.fineract.CreditCheck.data;


public class CrediBureauConfigurationData 
{
    private final long creditbureauConfigurationId;
    
    private final String configkey;
    
    private final String value;
    
    private final long organisationCreditbureauId;
    
    private final String description;
    
    private CrediBureauConfigurationData(final long creditbureauConfigurationId,final String configkey,final String value,
            final long organisationCreditbureauId,final String description)
    {
       this.creditbureauConfigurationId=creditbureauConfigurationId;
       this.configkey=configkey;
       this.value=value;
       this.organisationCreditbureauId=organisationCreditbureauId;
       this.description=description;
        
    }
    
    public CrediBureauConfigurationData instance(final long creditbureauConfigurationId,final String configkey,final String value,
            final long organisationCreditbureauId,final String description)
    {
        return new CrediBureauConfigurationData(creditbureauConfigurationId,configkey,value,
                organisationCreditbureauId,description);
    }

    
    public long getCreditbureauConfigurationId() {
        return this.creditbureauConfigurationId;
    }

    
    public String getConfigKey() {
        return this.configkey;
    }

    
    public String getValue() {
        return this.value;
    }

    
    public long getOrganisationCreditbureauId() {
        return this.organisationCreditbureauId;
    }

    
    public String getDescription() {
        return this.description;
    }

    
}
