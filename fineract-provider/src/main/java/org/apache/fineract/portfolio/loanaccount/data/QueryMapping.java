package org.apache.fineract.portfolio.loanaccount.data;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.fineract.portfolio.loanaccount.service.QueryParamToPredicate;

@AllArgsConstructor
@Getter
public class QueryMapping<E, D, T> {

    private Function<D, T> paramExtractor;
    private QueryParamToPredicate<E, T> queryParamToPredicate;
}
