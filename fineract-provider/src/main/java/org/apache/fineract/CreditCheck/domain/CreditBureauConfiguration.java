package org.apache.fineract.CreditCheck.domain;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name="m_creditbureau_configuration")
public class CreditBureauConfiguration extends AbstractPersistable<Long> 
{

    private String configkey;
    
    private String value;
    
    private String description;
    
    @OneToOne
    private OrganisationCreditBureau organisation_creditbureau;
    
    public CreditBureauConfiguration()
    {
        
    }
    
    public CreditBureauConfiguration(String configkey,String value,String description,OrganisationCreditBureau organisation_creditbureau)
    {
        this.configkey=configkey;
        this.value=value;
        this.description=description;
        this.organisation_creditbureau=organisation_creditbureau;
        
    }
    
    public CreditBureauConfiguration fromJson(JsonCommand command,OrganisationCreditBureau organisation_creditbureau)
    {
        final String configkey=command.stringValueOfParameterNamed("configkey");
        final String value=command.stringValueOfParameterNamed("value");
        final String description=command.stringValueOfParameterNamed("description");
        
        return new CreditBureauConfiguration(configkey,value,description,organisation_creditbureau);
        
    }
    
    
}
