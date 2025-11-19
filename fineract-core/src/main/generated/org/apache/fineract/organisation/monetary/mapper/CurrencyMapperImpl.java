package org.apache.fineract.organisation.monetary.mapper;

import javax.annotation.processing.Generated;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:14:32-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class CurrencyMapperImpl implements CurrencyMapper {

    @Override
    public CurrencyData map(MonetaryCurrency source) {
        if ( source == null ) {
            return null;
        }

        CurrencyData currencyData = new CurrencyData();

        currencyData.setCode( source.getCode() );
        currencyData.setDecimalPlaces( source.getDigitsAfterDecimal() );
        currencyData.setInMultiplesOf( source.getInMultiplesOf() );

        return currencyData;
    }
}
