package org.apache.fineract.investor.service;

import javax.annotation.processing.Generated;
import org.apache.fineract.investor.data.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:33-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ExternalAssetOwnerLoanProductAttributesMapperImpl implements ExternalAssetOwnerLoanProductAttributesMapper {

    @Override
    public ExternalTransferLoanProductAttributesData mapLoanProductAttributes(ExternalAssetOwnerLoanProductAttributes attributes) {
        if ( attributes == null ) {
            return null;
        }

        ExternalTransferLoanProductAttributesData externalTransferLoanProductAttributesData = new ExternalTransferLoanProductAttributesData();

        externalTransferLoanProductAttributesData.setAttributeId( attributes.getId() );
        externalTransferLoanProductAttributesData.setLoanProductId( attributes.getLoanProductId() );
        externalTransferLoanProductAttributesData.setAttributeKey( attributes.getAttributeKey() );
        externalTransferLoanProductAttributesData.setAttributeValue( attributes.getAttributeValue() );

        return externalTransferLoanProductAttributesData;
    }
}
