package org.apache.fineract.CreditCheck.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_creditbureau")
public class CreditBureau extends AbstractPersistable<Long> 
{

 /*   public static CreditBureau fromJson(final JsonCommand command, CreditBureauMaster creditBureauMaster,
            CreditBureauProduct creditBureauProduct) {
        String edate=null;
        final String alias = command.stringValueOfParameterNamed("alias");
        final String country = command.stringValueOfParameterNamed("country");
       LocalDate start_date = command.localDateValueOfParameterNamed("start_date"); 
        LocalDate end_date = command.localDateValueOfParameterNamed("end_date");
        String sdate=start_date.toString();

        if(edate!=null)
         edate=end_date.toString();

        final boolean is_active = command.booleanPrimitiveValueOfParameterNamed("is_active");
        return new CreditBureau(creditBureauMaster, creditBureauProduct, alias, country, sdate, edate, is_active);

    }*/


  /*  @ManyToOne
    private CreditBureauMaster cb_master;

    @OneToOne
    private CreditBureauProduct cb_product;
    
    @OneToMany(mappedBy="cb", cascade=CascadeType.ALL)
    private List<CreditBureauLpMapping> CreditBureauLpMapping=new ArrayList<>();*/
    
    /*    public CreditBureau(CreditBureauMaster creditBureauMaster, CreditBureauProduct creditBureauProduct, String alias, String country,
    String start_date, String end_date, boolean is_active) {
this.cb_master = creditBureauMaster;
this.cb_product = creditBureauProduct;
this.country = country;



}*/
    
   
    
    private String name;
    
    private String product;

    private String country;

    private String implementationKey;
    
    @OneToMany(mappedBy="organisation_creditbureau", cascade=CascadeType.ALL)
    private List<CreditBureauLpMapping> CreditBureauLpMapping=new ArrayList<>();
    
    public CreditBureau(String name,String product,String country,String implementationKey)
    {
       this.name=name;
       this.product=product;
       this.country=country;
       this.implementationKey=implementationKey;
    }

    public CreditBureau()
    {
        
    }
    
    public static CreditBureau fromJson(final JsonCommand command) {
        
        final String tname = command.stringValueOfParameterNamed("name");
        final String tproduct = command.stringValueOfParameterNamed("product");
        final String tcountry=command.stringValueOfParameterNamed("country");
        final String timplementationKey=command.stringValueOfParameterNamed("implementationKey");

    
        return new CreditBureau(tname, tproduct, tcountry, timplementationKey);

    }

    

}
