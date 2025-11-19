package org.apache.fineract.integrationtests.datatable;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T11:30:20-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
public class DatatableUniqueAndIndexColumnTest$NameUniqueIndexedHeaderData$MapperImpl implements DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData.Mapper {

    @Override
    public DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData map(ResultsetColumnHeaderData source) {
        if ( source == null ) {
            return null;
        }

        String name = null;
        boolean unique = false;
        boolean indexed = false;

        name = source.getColumnName();
        if ( source.getIsColumnUnique() != null ) {
            unique = source.getIsColumnUnique();
        }
        if ( source.getIsColumnIndexed() != null ) {
            indexed = source.getIsColumnIndexed();
        }

        DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData nameUniqueIndexedHeaderData = new DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData( name, unique, indexed );

        return nameUniqueIndexedHeaderData;
    }

    @Override
    public List<DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData> map(List<ResultsetColumnHeaderData> source) {
        if ( source == null ) {
            return null;
        }

        List<DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData> list = new ArrayList<DatatableUniqueAndIndexColumnTest.NameUniqueIndexedHeaderData>( source.size() );
        for ( ResultsetColumnHeaderData resultsetColumnHeaderData : source ) {
            list.add( map( resultsetColumnHeaderData ) );
        }

        return list;
    }
}
