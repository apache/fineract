package org.apache.fineract.portfolio.clientaddress.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.clientaddress.data.FieldConfigurationData;
import org.apache.fineract.portfolio.clientaddress.service.FieldConfigurationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AddAddressCommandFromApiJsonDeserializer 
{
    private final FromJsonHelper fromApiJsonHelper;
    private final FieldConfigurationReadPlatformService readservice;

    @Autowired
    public AddAddressCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper,final FieldConfigurationReadPlatformService readservice) 
    {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.readservice=readservice;
    }
    
    public void validateForCreate(final String json,final boolean fromNewClient)
    {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("Address");
        
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
       final List<FieldConfigurationData> configurationData=
                new ArrayList<>(this.readservice.retrieveFieldConfigurationList("client"));
       final List<String> enabledFieldList=new ArrayList<>();
        
  Map<String,Boolean> madatoryFieldsMap = new HashMap<String,Boolean>();
  Map<String,Boolean> enabledFieldsMap = new HashMap<String,Boolean>();
  Map<String,String> regexFieldsMap = new HashMap<String,String>();
        
        for(FieldConfigurationData data: configurationData)
        {
            madatoryFieldsMap.put(data.getField(), data.isIs_mandatory());
            enabledFieldsMap.put(data.getField(), data.isIs_enabled());
            regexFieldsMap.put(data.getField(), data.getValidation_regex());
            if(data.isIs_enabled())
            {
                enabledFieldList.add(data.getField());
            }
        }
        if(fromNewClient)
        {
            enabledFieldList.add("addressTypeId");
            madatoryFieldsMap.put("addressTypeId", true);
            
        }
       final Set<String> supportedParameters = new HashSet<>(enabledFieldList);
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        
        final String street=this.fromApiJsonHelper.extractStringNamed("street", element);
        
        
        if(enabledFieldsMap.get("street"))
        {
            if(madatoryFieldsMap.get("street"))
            {
                
                
                
                baseDataValidator.reset().parameter("street").value(street).notBlank();
               
            }
            if(!regexFieldsMap.get("street").isEmpty())
            {
                baseDataValidator.reset().parameter("street").value(street).matchesRegularExpression(regexFieldsMap.get("street"));
            }
            
            
        }
        final String address_line_1=this.fromApiJsonHelper.extractStringNamed("address_line_1", element);
        if(enabledFieldsMap.get("address_line_1"))
        {
            if(madatoryFieldsMap.get("address_line_1"))
            {
                baseDataValidator.reset().parameter("address_line_1").value(address_line_1).notBlank();
            }
            if(!regexFieldsMap.get("address_line_1").isEmpty())
            {
                baseDataValidator.reset().parameter("address_line_1").value(address_line_1).matchesRegularExpression(regexFieldsMap.get("address_line_1"));
            }
            
        }
        final String address_line_2=this.fromApiJsonHelper.extractStringNamed("address_line_2", element);
        if(enabledFieldsMap.get("address_line_2"))
        {
            if(madatoryFieldsMap.get("address_line_2"))
            {
                baseDataValidator.reset().parameter("address_line_2").value(address_line_2).notBlank();
            }
            if(!regexFieldsMap.get("address_line_2").isEmpty())
            {
                baseDataValidator.reset().parameter("address_line_2").value(address_line_2).matchesRegularExpression(regexFieldsMap.get("address_line_2"));
            }
        }
        final String address_line_3=this.fromApiJsonHelper.extractStringNamed("address_line_3", element);
        if(enabledFieldsMap.get("address_line_3"))
        {
            if(madatoryFieldsMap.get("address_line_3"))
            {
                baseDataValidator.reset().parameter("address_line_3").value(address_line_3).notBlank();
            }
            if(!regexFieldsMap.get("address_line_3").isEmpty())
            {
                baseDataValidator.reset().parameter("address_line_3").value(address_line_3).matchesRegularExpression(regexFieldsMap.get("address_line_3"));
            }
        }
        final String town_village=this.fromApiJsonHelper.extractStringNamed("town_village", element);
        if(enabledFieldsMap.get("town_village"))
        {
            if(madatoryFieldsMap.get("town_village"))
            {
                baseDataValidator.reset().parameter("town_village").value(town_village).notBlank();
            }
            if(!regexFieldsMap.get("town_village").isEmpty())
            {
                baseDataValidator.reset().parameter("town_village").value(town_village).matchesRegularExpression(regexFieldsMap.get("town_village"));
            }
        }
        final String city=this.fromApiJsonHelper.extractStringNamed("city", element);
       
        if(enabledFieldsMap.get("city"))
        {
            if(madatoryFieldsMap.get("city"))
            {
                baseDataValidator.reset().parameter("city").value(city).notBlank();
            }
            if(!regexFieldsMap.get("city").isEmpty())
            {
                baseDataValidator.reset().parameter("city").value(city).matchesRegularExpression(regexFieldsMap.get("city"));
            }
        }
        final String county_district=this.fromApiJsonHelper.extractStringNamed("county_district", element);
        if(enabledFieldsMap.get("county_district"))
        {
            if(madatoryFieldsMap.get("county_district"))
            {
                baseDataValidator.reset().parameter("county_district").value(county_district).notBlank();
            }
            if(!regexFieldsMap.get("county_district").isEmpty())
            {
                baseDataValidator.reset().parameter("county_district").value(county_district).matchesRegularExpression(regexFieldsMap.get("county_district"));
            }
        }
        
       
        if( this.fromApiJsonHelper.extractLongNamed("state_province_id", element)!=null)
        {

           final long state_province_id=this.fromApiJsonHelper.extractLongNamed("state_province_id", element);
    if(enabledFieldsMap.get("state_province_id"))
    {
        if(madatoryFieldsMap.get("state_province_id"))
        {
            baseDataValidator.reset().parameter("state_province_id").value(state_province_id).notBlank();
        }
        if(!regexFieldsMap.get("state_province_id").isEmpty())
        {
            baseDataValidator.reset().parameter("state_province_id").value(state_province_id).matchesRegularExpression(regexFieldsMap.get("state_province_id"));
        }
    }   
        }
        
        if(this.fromApiJsonHelper.extractLongNamed("country_id", element)!=null)
        {
            final long country_id=this.fromApiJsonHelper.extractLongNamed("country_id", element);
            if(enabledFieldsMap.get("country_id"))
            {
                if(madatoryFieldsMap.get("country_id"))
                {
                    baseDataValidator.reset().parameter("country_id").value(country_id).notBlank();
                }
                if(!regexFieldsMap.get("country_id").isEmpty())
                {
                    baseDataValidator.reset().parameter("country_id").value(country_id).matchesRegularExpression(regexFieldsMap.get("country_id"));
                }
            }  
        }
        
        
        final String postal_code=this.fromApiJsonHelper.extractStringNamed("postal_code", element);
        if(enabledFieldsMap.get("postal_code"))
        {
            if(madatoryFieldsMap.get("postal_code"))
            {
                baseDataValidator.reset().parameter("postal_code").value(postal_code).notBlank();
            }
            if(!regexFieldsMap.get("postal_code").isEmpty())
            {
                baseDataValidator.reset().parameter("postal_code").value(postal_code).matchesRegularExpression(regexFieldsMap.get("postal_code"));
            }
        }
        
        if(this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("latitude",element)!=null)
        {
            final BigDecimal latitude=this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("latitude",element);
            if(enabledFieldsMap.get("latitude"))
            {
                if(madatoryFieldsMap.get("latitude"))
                {
                    baseDataValidator.reset().parameter("latitude").value(latitude).notBlank();
                }
                if(!regexFieldsMap.get("latitude").isEmpty())
                {
                    baseDataValidator.reset().parameter("latitude").value(latitude).matchesRegularExpression(regexFieldsMap.get("latitude"));
                }
            }   
        }
        
        if(this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("longitude",element)!=null)
        {
            final BigDecimal longitude=this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("longitude",element);
            if(enabledFieldsMap.get("longitude"))
            {
                if(madatoryFieldsMap.get("longitude"))
                {
                    baseDataValidator.reset().parameter("longitude").value(longitude).notBlank();
                }
                if(!regexFieldsMap.get("longitude").isEmpty())
                {
                    baseDataValidator.reset().parameter("longitude").value(longitude).matchesRegularExpression(regexFieldsMap.get("longitude"));
                }
            } 
        }
        
      
        
       
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
         }
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
   
    
    
    

}
