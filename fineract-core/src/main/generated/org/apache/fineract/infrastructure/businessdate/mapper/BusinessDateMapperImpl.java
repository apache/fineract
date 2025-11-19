package org.apache.fineract.infrastructure.businessdate.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateResponse;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateResponse;
import org.apache.fineract.infrastructure.businessdate.data.service.BusinessDateDTO;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:14:32-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class BusinessDateMapperImpl implements BusinessDateMapper {

    @Override
    public BusinessDateDTO mapEntity(BusinessDate source) {
        if ( source == null ) {
            return null;
        }

        BusinessDateDTO businessDateDTO = new BusinessDateDTO();

        businessDateDTO.setDescription( sourceTypeDescription( source ) );
        businessDateDTO.setType( source.getType() );
        businessDateDTO.setDate( source.getDate() );

        return businessDateDTO;
    }

    @Override
    public List<BusinessDateDTO> mapEntity(List<BusinessDate> sources) {
        if ( sources == null ) {
            return null;
        }

        List<BusinessDateDTO> list = new ArrayList<BusinessDateDTO>( sources.size() );
        for ( BusinessDate businessDate : sources ) {
            list.add( mapEntity( businessDate ) );
        }

        return list;
    }

    @Override
    public BusinessDateDTO mapUpdateRequest(BusinessDateUpdateRequest source) {
        if ( source == null ) {
            return null;
        }

        BusinessDateDTO businessDateDTO = new BusinessDateDTO();

        businessDateDTO.setDescription( org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()).getDescription() );
        businessDateDTO.setType( org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()) );
        businessDateDTO.setDate( org.apache.fineract.infrastructure.core.service.DateUtils.toLocalDate(source.getLocale(), source.getDate(), source.getDateFormat()) );

        return businessDateDTO;
    }

    @Override
    public List<BusinessDateResponse> mapFetchResponse(List<BusinessDateDTO> sources) {
        if ( sources == null ) {
            return null;
        }

        List<BusinessDateResponse> list = new ArrayList<BusinessDateResponse>( sources.size() );
        for ( BusinessDateDTO businessDateDTO : sources ) {
            list.add( mapFetchResponse( businessDateDTO ) );
        }

        return list;
    }

    @Override
    public BusinessDateResponse mapFetchResponse(BusinessDateDTO source) {
        if ( source == null ) {
            return null;
        }

        BusinessDateResponse businessDateResponse = new BusinessDateResponse();

        businessDateResponse.setDescription( source.getDescription() );
        businessDateResponse.setType( source.getType() );
        businessDateResponse.setDate( source.getDate() );

        return businessDateResponse;
    }

    @Override
    public BusinessDateUpdateResponse mapUpdateResponse(BusinessDateDTO source) {
        if ( source == null ) {
            return null;
        }

        BusinessDateUpdateResponse businessDateUpdateResponse = new BusinessDateUpdateResponse();

        businessDateUpdateResponse.setDescription( source.getDescription() );
        businessDateUpdateResponse.setType( source.getType() );
        businessDateUpdateResponse.setDate( source.getDate() );
        Map<BusinessDateType, LocalDate> map = source.getChanges();
        if ( map != null ) {
            businessDateUpdateResponse.setChanges( new LinkedHashMap<BusinessDateType, LocalDate>( map ) );
        }

        return businessDateUpdateResponse;
    }

    private String sourceTypeDescription(BusinessDate businessDate) {
        BusinessDateType type = businessDate.getType();
        if ( type == null ) {
            return null;
        }
        return type.getDescription();
    }
}
