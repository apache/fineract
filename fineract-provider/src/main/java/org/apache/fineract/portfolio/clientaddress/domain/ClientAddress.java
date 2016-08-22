package org.apache.fineract.portfolio.clientaddress.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.client.domain.Client;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name="m_client_address")
public class ClientAddress extends AbstractPersistable<Long>
{

    @ManyToOne
    private Client client;
    
    @ManyToOne
    private Address address;
    
    @ManyToOne
    @JoinColumn(name="address_type_id")
    private CodeValue addressType;
    
    @Column(name="is_active")
    private boolean isActive; 
    
   private ClientAddress(Client client,Address address,CodeValue addressType,boolean isActive)
   {
       this.client=client;
       this.address=address;
       this.addressType=addressType;
       this.isActive=isActive;
       
   }
    
    public ClientAddress()
    {
        
    }
    
    public static ClientAddress fromJson(JsonCommand command,Client client,Address address,CodeValue address_type)
    {
        final boolean isActive=command.booleanPrimitiveValueOfParameterNamed("is_active");
        
        return new ClientAddress(client,address,address_type,isActive);
    }

    
    public Client getClient() {
        return this.client;
    }

    
    public Address getAddress() {
        return this.address;
    }

    
   public CodeValue getAddressType() {
        return this.addressType;
    }

    
    public void setAddressType(CodeValue addressType) {
        this.addressType = addressType;
    }

   /* public boolean isIs_active() {
        return this.isActive;
    }*/

    
    public void setClient(Client client) {
        this.client = client;
    }

    
    public void setAddress(Address address) {
        this.address = address;
    }

    
    public boolean isActive() {
        return this.isActive;
    }

    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    
  

    
  /*  public void setIs_active(boolean isActive) {
        this.isActive = isActive;
    }*/
    
    
    
}
