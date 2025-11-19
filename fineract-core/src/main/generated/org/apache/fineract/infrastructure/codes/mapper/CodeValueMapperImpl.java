package org.apache.fineract.infrastructure.codes.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:14:32-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class CodeValueMapperImpl implements CodeValueMapper {

    @Override
    public CodeValueData map(CodeValue source) {
        if ( source == null ) {
            return null;
        }

        CodeValueData codeValueData = new CodeValueData();

        codeValueData.setName( source.getLabel() );
        codeValueData.setId( source.getId() );
        codeValueData.setPosition( source.getPosition() );
        codeValueData.setDescription( source.getDescription() );
        codeValueData.setActive( source.isActive() );
        codeValueData.setMandatory( source.isMandatory() );

        return codeValueData;
    }

    @Override
    public List<CodeValueData> map(List<CodeValue> source) {
        if ( source == null ) {
            return null;
        }

        List<CodeValueData> list = new ArrayList<CodeValueData>( source.size() );
        for ( CodeValue codeValue : source ) {
            list.add( map( codeValue ) );
        }

        return list;
    }
}
