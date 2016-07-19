package org.apache.fineract.portfolio.clientaddress.data;

public class ClientAddressData 
{

    private final long clientAddressId;
    
    private final long client_id;
    
    private final long address_id;
    
    private final long address_type_id;
    
    private final boolean is_active;
    
    private ClientAddressData(final long clientAddressId,final long client_id,final long address_id,
            final long address_type_id,final boolean is_active)
    {
        this.clientAddressId=clientAddressId;
        this.client_id=client_id;
        this.address_id=address_id;
        this.address_type_id=address_type_id;
        this.is_active=is_active;
    }
    
    public static ClientAddressData instance(final long clientAddressId,final long client_id,final long address_id,
            final long address_type_id,final boolean is_active)
    {
        return new ClientAddressData(clientAddressId,client_id,address_id,
                address_type_id,is_active);
    }
}
