package org.apache.fineract.portfolio.delinquency.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyRangeData;
import org.apache.fineract.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:07-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanDelinquencyTagMapperImpl implements LoanDelinquencyTagMapper {

    private final DelinquencyRangeMapper delinquencyRangeMapper;

    @Autowired
    public LoanDelinquencyTagMapperImpl(DelinquencyRangeMapper delinquencyRangeMapper) {

        this.delinquencyRangeMapper = delinquencyRangeMapper;
    }

    @Override
    public LoanDelinquencyTagHistoryData map(LoanDelinquencyTagHistory source) {
        if ( source == null ) {
            return null;
        }

        Long loanId = null;
        Long id = null;
        DelinquencyRangeData delinquencyRange = null;
        LocalDate addedOnDate = null;
        LocalDate liftedOnDate = null;

        loanId = sourceLoanId( source );
        id = source.getId();
        delinquencyRange = delinquencyRangeMapper.map( source.getDelinquencyRange() );
        addedOnDate = source.getAddedOnDate();
        liftedOnDate = source.getLiftedOnDate();

        LoanDelinquencyTagHistoryData loanDelinquencyTagHistoryData = new LoanDelinquencyTagHistoryData( id, loanId, delinquencyRange, addedOnDate, liftedOnDate );

        return loanDelinquencyTagHistoryData;
    }

    @Override
    public List<LoanDelinquencyTagHistoryData> map(List<LoanDelinquencyTagHistory> sources) {
        if ( sources == null ) {
            return null;
        }

        List<LoanDelinquencyTagHistoryData> list = new ArrayList<LoanDelinquencyTagHistoryData>( sources.size() );
        for ( LoanDelinquencyTagHistory loanDelinquencyTagHistory : sources ) {
            list.add( map( loanDelinquencyTagHistory ) );
        }

        return list;
    }

    private Long sourceLoanId(LoanDelinquencyTagHistory loanDelinquencyTagHistory) {
        Loan loan = loanDelinquencyTagHistory.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getId();
    }
}
