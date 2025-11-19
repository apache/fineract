package org.apache.fineract.portfolio.delinquency.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:06-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class DelinquencyRangeMapperImpl implements DelinquencyRangeMapper {

    @Override
    public DelinquencyRangeData map(DelinquencyRange source) {
        if ( source == null ) {
            return null;
        }

        Long id = null;
        String classification = null;
        Integer minimumAgeDays = null;
        Integer maximumAgeDays = null;

        id = source.getId();
        classification = source.getClassification();
        minimumAgeDays = source.getMinimumAgeDays();
        maximumAgeDays = source.getMaximumAgeDays();

        DelinquencyRangeData delinquencyRangeData = new DelinquencyRangeData( id, classification, minimumAgeDays, maximumAgeDays );

        return delinquencyRangeData;
    }

    @Override
    public List<DelinquencyRangeData> map(List<DelinquencyRange> sources) {
        if ( sources == null ) {
            return null;
        }

        List<DelinquencyRangeData> list = new ArrayList<DelinquencyRangeData>( sources.size() );
        for ( DelinquencyRange delinquencyRange : sources ) {
            list.add( map( delinquencyRange ) );
        }

        return list;
    }
}
