package org.apache.fineract.cob.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:29:42-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class BusinessStepMapperImpl implements BusinessStepMapper {

    @Override
    public BusinessStep map(BatchBusinessStep source) {
        if ( source == null ) {
            return null;
        }

        BusinessStep businessStep = new BusinessStep();

        businessStep.setOrder( source.getStepOrder() );
        businessStep.setStepName( source.getStepName() );

        return businessStep;
    }

    @Override
    public List<BusinessStep> map(List<BatchBusinessStep> source) {
        if ( source == null ) {
            return null;
        }

        List<BusinessStep> list = new ArrayList<BusinessStep>( source.size() );
        for ( BatchBusinessStep batchBusinessStep : source ) {
            list.add( map( batchBusinessStep ) );
        }

        return list;
    }
}
