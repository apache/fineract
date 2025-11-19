package org.apache.fineract.portfolio.delinquency.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRange;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:07-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class DelinquencyBucketMapperImpl implements DelinquencyBucketMapper {

    @Override
    public DelinquencyBucketData map(DelinquencyBucket source) {
        if ( source == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        List<DelinquencyRangeData> ranges = null;

        id = source.getId();
        name = source.getName();
        ranges = delinquencyRangeListToDelinquencyRangeDataList( source.getRanges() );

        DelinquencyBucketData delinquencyBucketData = new DelinquencyBucketData( id, name, ranges );

        return delinquencyBucketData;
    }

    @Override
    public List<DelinquencyBucketData> map(List<DelinquencyBucket> sources) {
        if ( sources == null ) {
            return null;
        }

        List<DelinquencyBucketData> list = new ArrayList<DelinquencyBucketData>( sources.size() );
        for ( DelinquencyBucket delinquencyBucket : sources ) {
            list.add( map( delinquencyBucket ) );
        }

        return list;
    }

    protected DelinquencyRangeData delinquencyRangeToDelinquencyRangeData(DelinquencyRange delinquencyRange) {
        if ( delinquencyRange == null ) {
            return null;
        }

        Long id = null;
        String classification = null;
        Integer minimumAgeDays = null;
        Integer maximumAgeDays = null;

        id = delinquencyRange.getId();
        classification = delinquencyRange.getClassification();
        minimumAgeDays = delinquencyRange.getMinimumAgeDays();
        maximumAgeDays = delinquencyRange.getMaximumAgeDays();

        DelinquencyRangeData delinquencyRangeData = new DelinquencyRangeData( id, classification, minimumAgeDays, maximumAgeDays );

        return delinquencyRangeData;
    }

    protected List<DelinquencyRangeData> delinquencyRangeListToDelinquencyRangeDataList(List<DelinquencyRange> list) {
        if ( list == null ) {
            return null;
        }

        List<DelinquencyRangeData> list1 = new ArrayList<DelinquencyRangeData>( list.size() );
        for ( DelinquencyRange delinquencyRange : list ) {
            list1.add( delinquencyRangeToDelinquencyRangeData( delinquencyRange ) );
        }

        return list1;
    }
}
