package org.apache.fineract.infrastructure.event.external.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationItemResponse;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:14:32-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class ExternalEventsConfigurationMapperImpl implements ExternalEventsConfigurationMapper {

    @Override
    public List<ExternalEventConfigurationItemResponse> map(List<ExternalEventConfiguration> source) {
        if ( source == null ) {
            return null;
        }

        List<ExternalEventConfigurationItemResponse> list = new ArrayList<ExternalEventConfigurationItemResponse>( source.size() );
        for ( ExternalEventConfiguration externalEventConfiguration : source ) {
            list.add( externalEventConfigurationToExternalEventConfigurationItemResponse( externalEventConfiguration ) );
        }

        return list;
    }

    protected ExternalEventConfigurationItemResponse externalEventConfigurationToExternalEventConfigurationItemResponse(ExternalEventConfiguration externalEventConfiguration) {
        if ( externalEventConfiguration == null ) {
            return null;
        }

        ExternalEventConfigurationItemResponse externalEventConfigurationItemResponse = new ExternalEventConfigurationItemResponse();

        externalEventConfigurationItemResponse.setType( externalEventConfiguration.getType() );
        externalEventConfigurationItemResponse.setEnabled( externalEventConfiguration.isEnabled() );

        return externalEventConfigurationItemResponse;
    }
}
