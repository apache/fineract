package org.apache.fineract.CreditCheck.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name="m_organisation_creditbureau")
public class OrganisationCreditBureau extends AbstractPersistable<Long> 
{

    private String alias;
    
    @OneToOne
    private CreditBureau creditbureau;
    
    private boolean is_active;
    
    @OneToMany(mappedBy="organisation_creditbureau",cascade=CascadeType.ALL)
    private List<CreditBureauLpMapping> creditBureauLpMapping=new ArrayList<>();
    
    public OrganisationCreditBureau(String alias,CreditBureau creditbureau,boolean is_active)
    {
        this.alias=alias;
        this.creditbureau=creditbureau;
        this.is_active=is_active;
    }
    
    public OrganisationCreditBureau()
    {
        
    }
    
    public static OrganisationCreditBureau fromJson(final JsonCommand command,CreditBureau creditbureau)
    {
        final String alias=command.stringValueOfParameterNamed("alias");
        final boolean is_active=command.booleanPrimitiveValueOfParameterNamed("is_active");
        
        return new OrganisationCreditBureau(alias,creditbureau,is_active);
    }

    
    public String getAlias() {
        return this.alias;
    }

    
    public void setAlias(String alias) {
        this.alias = alias;
    }

    
    public CreditBureau getCreditbureau() {
        return this.creditbureau;
    }

    
    public void setCreditbureau(CreditBureau creditbureau) {
        this.creditbureau = creditbureau;
    }

    
    public boolean isIs_active() {
        return this.is_active;
    }

    
    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    
    public List<CreditBureauLpMapping> getCreditBureauLpMapping() {
        return this.creditBureauLpMapping;
    }

    
    public void setCreditBureauLpMapping(List<CreditBureauLpMapping> creditBureauLpMapping) {
        this.creditBureauLpMapping = creditBureauLpMapping;
    }
    
    
    
    
     
}
