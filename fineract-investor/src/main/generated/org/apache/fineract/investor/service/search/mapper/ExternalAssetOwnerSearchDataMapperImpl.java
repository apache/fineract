package org.apache.fineract.investor.service.search.mapper;

import javax.annotation.processing.Generated;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.support.ExternalIdMapper;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.domain.search.SearchedExternalAssetOwner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:33-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ExternalAssetOwnerSearchDataMapperImpl implements ExternalAssetOwnerSearchDataMapper {

    private final ExternalIdMapper externalIdMapper;

    @Autowired
    public ExternalAssetOwnerSearchDataMapperImpl(ExternalIdMapper externalIdMapper) {

        this.externalIdMapper = externalIdMapper;
    }

    @Override
    public ExternalTransferData map(SearchedExternalAssetOwner source) {
        if ( source == null ) {
            return null;
        }

        ExternalTransferData externalTransferData = new ExternalTransferData();

        externalTransferData.setOwner( toOwner( source ) );
        externalTransferData.setLoan( toLoanExternalId( source ) );
        externalTransferData.setTransferExternalId( toTransferExternalId( source ) );
        externalTransferData.setStatus( toStatus( source ) );
        externalTransferData.setSubStatus( toSubStatus( source ) );
        externalTransferData.setDetails( toDetails( source ) );
        externalTransferData.setTransferId( source.getTransferId() );
        externalTransferData.setTransferExternalGroupId( externalIdMapper.mapExternalId( source.getTransferExternalGroupId() ) );
        externalTransferData.setPurchasePriceRatio( source.getPurchasePriceRatio() );
        externalTransferData.setSettlementDate( source.getSettlementDate() );
        externalTransferData.setEffectiveFrom( source.getEffectiveFrom() );
        externalTransferData.setEffectiveTo( source.getEffectiveTo() );

        return externalTransferData;
    }
}
