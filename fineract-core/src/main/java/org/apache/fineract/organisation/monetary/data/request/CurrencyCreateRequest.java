package org.apache.fineract.organisation.monetary.data.request;

import java.io.Serializable;

public record CurrencyCreateRequest(String code, String name, Integer decimalPlaces, Integer inMultiplesOf, String displaySymbol, String nameCode) implements Serializable {

}
