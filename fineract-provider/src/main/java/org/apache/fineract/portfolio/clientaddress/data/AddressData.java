package org.apache.fineract.portfolio.clientaddress.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public class AddressData 
{
    private final Long client_id;
    
    private final String addressType;
    
    private final Long addressId;
    
    private final Long addressTypeId;
    
    private final Boolean is_active;
    
    private final  String street;

    private final  String address_line_1;

    private final  String address_line_2;

    private final  String address_line_3;

    private final  String town_village;
 
    private final  String city;

    private final  String county_district;

    private final  Long state_province_id;
    
    private final String country_name;
    
    private final String state_name;

    private final  Long country_id;

    private final  String postal_code;

    private final  BigDecimal latitude;

    private final  BigDecimal longitude;

    private final  String created_by;

    private final  Date created_on;

    private final  String updated_by;

    private final  Date updated_on;
    
    //template holder
    private final Collection<CodeValueData> countryIdOptions;
    private final Collection<CodeValueData> stateProvinceIdOptions;
    private final Collection<CodeValueData> addressTypeIdOptions;
    
    
    private AddressData(String addressType,Long client_id,Long addressId,Long addressTypeId,Boolean is_active,String street,String address_line_1,String address_line_2,String address_line_3,
            String town_village,String city,String county_district,Long state_province_id,
            Long country_id,String state_name,String country_name,String postal_code,BigDecimal latitude,BigDecimal longitude,String created_by,Date created_on
            ,String updated_by,Date updated_on,Collection<CodeValueData> countryIdOptions,Collection<CodeValueData> stateProvinceIdOptions,
            Collection<CodeValueData> addressTypeIdOptions)
    {   
        this.addressType=addressType;
        this.client_id=client_id;
        this.addressId=addressId;
        this.addressTypeId=addressTypeId;
        this.is_active=is_active;
        this.street=street;
        this.address_line_1=address_line_1;
        this.address_line_2=address_line_2;
        this.address_line_3=address_line_3;
        this.town_village=town_village;
        this.city=city;
        this.county_district=county_district;
        this.state_province_id=state_province_id;
        this.country_id=country_id;
        this.state_name=state_name;
        this.country_name=country_name;
        this.postal_code=postal_code;
        this.latitude=latitude;
        this.longitude=longitude;
        this.created_by=created_by;
        this.created_on=created_on;
        this.updated_by=updated_by;
        this.updated_on=updated_on;
        this.countryIdOptions=countryIdOptions;
        this.stateProvinceIdOptions=stateProvinceIdOptions;
        this.addressTypeIdOptions=addressTypeIdOptions;
    }
    
    public static AddressData instance(String addressType,Long client_id,Long addressId,Long addressTypeId,Boolean is_active,String street,String address_line_1,String address_line_2,String address_line_3,
            String town_village,String city,String county_district,Long state_province_id,
            Long country_id,String state_name,String country_name,String postal_code,BigDecimal latitude,BigDecimal longitude,String created_by,Date created_on
            ,String updated_by,Date updated_on)
    {
        
        
        return new AddressData(addressType,client_id,addressId,addressTypeId,is_active,street,address_line_1,address_line_2,address_line_3,
                town_village,city,county_district,state_province_id,
                country_id,state_name,country_name,postal_code,latitude,longitude,created_by,created_on
                ,updated_by,updated_on,null,null,null);
    }
    
    
    public static AddressData instance1(Long addressId,String street,String address_line_1,String address_line_2,String address_line_3,
            String town_village,String city,String county_district,Long state_province_id,
            Long country_id,String postal_code,BigDecimal latitude,BigDecimal longitude,String created_by,Date created_on
            ,String updated_by,Date updated_on)
    {
        return new AddressData(null,null,addressId,null,false,street,address_line_1,address_line_2,address_line_3,
                town_village,city,county_district,state_province_id,
                country_id,null,null,postal_code,latitude,longitude,created_by,created_on
                ,updated_by,updated_on,null,null,null);
    }
    
    public static AddressData template(Collection<CodeValueData> countryIdOptions,Collection<CodeValueData> stateProvinceIdOptions,
            Collection<CodeValueData> addressTypeIdOptions)
    {
        final Long client_idtemp=null;
        
        
        final Long addressIdtemp=null;
       
        final Long addressTypeIdtemp=null;
       
        final Boolean is_activetemp=null;
       
        final  String streettemp=null;

        final  String address_line_1temp=null;

        final  String address_line_2temp=null;

        final  String address_line_3temp=null;

        final  String town_villagetemp=null;
    
        final  String citytemp=null;

        final  String county_districttemp=null;

        final  Long state_province_idtemp=null;

        final  Long country_idtemp=null;

        final  String postal_codetemp=null;

        final  BigDecimal latitudetemp=null;

        final  BigDecimal longitudetemp=null;

        final  String created_bytemp=null;

        final  Date created_ontemp=null;

        final  String updated_bytemp=null;

        final  Date updated_ontemp=null;
        
        
        return new AddressData(null,client_idtemp,addressIdtemp,addressTypeIdtemp,is_activetemp,streettemp,address_line_1temp,address_line_2temp,address_line_3temp,
                town_villagetemp,citytemp,county_districttemp,state_province_idtemp,
                country_idtemp,null,null,postal_codetemp,latitudetemp,longitudetemp,created_bytemp,created_ontemp
                ,updated_bytemp,updated_ontemp,countryIdOptions,stateProvinceIdOptions,addressTypeIdOptions);
    }
    

}
