package org.apache.fineract.portfolio.clientaddress.service;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.clientaddress.domain.Address;
import org.apache.fineract.portfolio.clientaddress.domain.AddressRepository;
import org.apache.fineract.portfolio.clientaddress.domain.ClientAddress;
import org.apache.fineract.portfolio.clientaddress.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.clientaddress.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.clientaddress.serialization.AddAddressCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class AddressWritePlatformServiceImpl implements AddressWritePlatformService
{
    private final PlatformSecurityContext context;
    private final CodeValueRepository codeValueRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper;
    private final AddAddressCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    
 
    @Autowired
    public AddressWritePlatformServiceImpl(final PlatformSecurityContext context,final CodeValueRepository codeValueRepository,
            final ClientAddressRepository clientAddressRepository,final ClientRepository clientRepository,final AddressRepository addressRepository,
            final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper,final AddAddressCommandFromApiJsonDeserializer fromApiJsonDeserializer)
    {
        this.context=context;
        this.codeValueRepository=codeValueRepository;
        this.clientAddressRepository=clientAddressRepository;
        this.clientRepository=clientRepository;
        this.addressRepository=addressRepository;
        this.clientAddressRepositoryWrapper=clientAddressRepositoryWrapper;
        this.fromApiJsonDeserializer=fromApiJsonDeserializer;
    }
    
    @Override
    public CommandProcessingResult addClientAddress(Long clientId,Long addressTypeId,JsonCommand command) {
        CodeValue stateIdobj=null;
        CodeValue countryIdObj=null;
        long stateId;
        long countryId;
        
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(),false);
        
        if(command.longValueOfParameterNamed("state_province_id")!=null)
        {
           stateId=command.longValueOfParameterNamed("state_province_id");
             stateIdobj=this.codeValueRepository.getOne(stateId); 
        }
        
       if(command.longValueOfParameterNamed("country_id")!=null)
       {
            countryId=command.longValueOfParameterNamed("country_id");
            countryIdObj=this.codeValueRepository.getOne(countryId); 
       }
       
 
        final CodeValue addressTypeIdObj=this.codeValueRepository.getOne(addressTypeId); 
      
        final Address add=Address.fromJson(command,stateIdobj,countryIdObj);
        this.addressRepository.save(add);
        Long addressid=add.getId();
        final Address addobj=this.addressRepository.getOne(addressid);
     
        final Client client=this.clientRepository.getOne(clientId);
        
        final ClientAddress clientAddressobj=ClientAddress.fromJson(command,client,addobj,addressTypeIdObj);
        this.clientAddressRepository.save(clientAddressobj);
        
      return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientAddressobj.getId()).build();
    }
    
    @Override
    public CommandProcessingResult addNewClientAddress(Client client,JsonCommand command)
    {
        CodeValue stateIdobj=null;
        CodeValue countryIdObj=null;
        long stateId;
        long countryId;
        
        final JsonArray addressArray=command.arrayOfParameterNamed("address");
       
        
        for(int i=0;i<addressArray.size();i++)
        {
            final JsonObject jsonObject = addressArray.get(i).getAsJsonObject();
            this.fromApiJsonDeserializer.validateForCreate(jsonObject.toString(),true);
            
            if(command.longValueOfParameterNamed("state_province_id")!=null)
            {
               stateId=command.longValueOfParameterNamed("state_province_id");
                 stateIdobj=this.codeValueRepository.getOne(stateId); 
            }
            
           if(command.longValueOfParameterNamed("country_id")!=null)
           {
                countryId=command.longValueOfParameterNamed("country_id");
                countryIdObj=this.codeValueRepository.getOne(countryId); 
           }
           
             
             final long addressTypeId=jsonObject.get("addressTypeId").getAsLong();
             final CodeValue addressTypeIdObj=this.codeValueRepository.getOne(addressTypeId); 
             
             final Address add=Address.fromJsonObject(jsonObject,stateIdobj,countryIdObj);
             this.addressRepository.save(add);
             Long addressid=add.getId();
             final Address addobj=this.addressRepository.getOne(addressid);
            
            
             
             final ClientAddress clientAddressobj=ClientAddress.fromJson(command,client,addobj,addressTypeIdObj);
             this.clientAddressRepository.save(clientAddressobj);
            
          
            
      
        }
        long typ=1;
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(typ).build();
    }
    
    
    @Override
    public CommandProcessingResult updateClientAddress(Long clientId,Long addressTypeId,Boolean status,JsonCommand command) {
        this.context.authenticatedUser();
       
        long stateId;
        
        long countryId;
        
        CodeValue stateIdobj;
        
        CodeValue countryIdObj;
        
        boolean is_address_update=false;
        
        boolean is_address_config_update=false;
        
        
       
        
        
        
      
        CodeValue addresstyp=this.codeValueRepository.getOne(addressTypeId);
                
        ClientAddress clientAddressObj=this.clientAddressRepositoryWrapper.findOneByClientIdAndAddressTypeAndIsActive(clientId, addresstyp,status);
        long addrId=clientAddressObj.getAddress().getId();
        Address addobj=this.addressRepository.getOne(addrId);
        
        
         if(!(command.stringValueOfParameterNamed("street").isEmpty()))
         {
             
             is_address_update=true;
            final String street=command.stringValueOfParameterNamed("street");
            addobj.setStreet(street);
            
         }
         
         if(!(command.stringValueOfParameterNamed("address_line_1").isEmpty()))
         {
             
             is_address_update=true;
             final String address_line_1=command.stringValueOfParameterNamed("address_line_1");
             addobj.setAddress_line_1(address_line_1);
             
         }
         if(!(command.stringValueOfParameterNamed("address_line_2").isEmpty()))
         {
             
             is_address_update=true;
             final String address_line_2=command.stringValueOfParameterNamed("address_line_2");
             addobj.setAddress_line_2(address_line_2);
             
         }
         
         if(!(command.stringValueOfParameterNamed("address_line_3").isEmpty()))
         {   
             is_address_update=true;
             final String address_line_3=command.stringValueOfParameterNamed("address_line_3");
             addobj.setAddress_line_3(address_line_3);
             
         }
         
         if(!(command.stringValueOfParameterNamed("town_village").isEmpty()))
         {
             
             is_address_update=true;
             final String town_village=command.stringValueOfParameterNamed("town_village");
             addobj.setTown_village(town_village);
         }
         
         if(!(command.stringValueOfParameterNamed("city").isEmpty()))
         {  
             is_address_update=true;
             final String city=command.stringValueOfParameterNamed("city");
             addobj.setCity(city);
         }
         
         if(!(command.stringValueOfParameterNamed("county_district").isEmpty()))
         {  
             is_address_update=true;
             final String county_district=command.stringValueOfParameterNamed("county_district");
             addobj.setCounty_district(county_district);
         }
        if(command.longValueOfParameterNamed("state_province_id")!=null)
        {  
            is_address_update=true;
            stateId=command.longValueOfParameterNamed("state_province_id");
            stateIdobj=this.codeValueRepository.getOne(stateId);
            addobj.setState_province(stateIdobj);
        }
        if(command.longValueOfParameterNamed("country_id")!=null)
        {  
            is_address_update=true;
            countryId=command.longValueOfParameterNamed("country_id"); 
            countryIdObj=this.codeValueRepository.getOne(countryId);
            addobj.setCountry(countryIdObj);
        }
        if(!(command.stringValueOfParameterNamed("postal_code").isEmpty()))
        {  
            is_address_update=true;
            final String postal_code=command.stringValueOfParameterNamed("postal_code");
            addobj.setPostal_code(postal_code);
        }
        if(command.bigDecimalValueOfParameterNamed("latitude")!=null)
        {  
        
            is_address_update=true;
            final BigDecimal latitude=command.bigDecimalValueOfParameterNamed("latitude");
         
            addobj.setLatitude(latitude);
        }
        if(command.bigDecimalValueOfParameterNamed("longitude")!=null)
        {  
            is_address_update=true;
            final BigDecimal longitude=command.bigDecimalValueOfParameterNamed("longitude");
            addobj.setLongitude(longitude);
           
        }
        
        if( is_address_update)
        {
            
            this.addressRepository.save(addobj);
        }
       
        
     /*   if(command.longValueOfParameterNamed("address_type_id")!=null)
        {
            is_address_config_update=true;
            final long newAddressTypeId=command.longValueOfParameterNamed("address_type_id");
            CodeValue addressTypeIdObj=this.codeValueRepository.getOne(newAddressTypeId);
            clientAddressObj.setAddressType(addressTypeIdObj);
            
        }*/
        final Boolean testActive=command.booleanPrimitiveValueOfParameterNamed("is_active");
        if(testActive!=null)
        {
            is_address_config_update=true;
            final boolean active=command.booleanPrimitiveValueOfParameterNamed("is_active");
            clientAddressObj.setIs_active(active);
            
        }
        
        if(is_address_config_update)
        {
           
            this.clientAddressRepository.save(clientAddressObj); 
        }
       
   
       
         
      
        
        
      return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(clientAddressObj.getId()).build();
    }
}
