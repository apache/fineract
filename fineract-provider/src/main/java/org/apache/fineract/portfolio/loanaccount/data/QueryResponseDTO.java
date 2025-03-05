package org.apache.fineract.portfolio.loanaccount.data;

import java.util.List;
import lombok.Data;

//TODO move to proper place
@Data
public class QueryResponseDTO<T> {

    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private List<T> content;
}
