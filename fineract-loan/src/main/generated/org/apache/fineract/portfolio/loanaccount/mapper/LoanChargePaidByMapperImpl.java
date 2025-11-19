package org.apache.fineract.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:06-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanChargePaidByMapperImpl implements LoanChargePaidByMapper {

    @Override
    public LoanChargePaidByData map(LoanChargePaidBy source) {
        if ( source == null ) {
            return null;
        }

        Long transactionId = null;
        Long chargeId = null;
        String name = null;
        Long id = null;
        BigDecimal amount = null;
        Integer installmentNumber = null;

        transactionId = sourceLoanTransactionId( source );
        chargeId = sourceLoanChargeId( source );
        name = sourceLoanChargeChargeName( source );
        id = source.getId();
        amount = source.getAmount();
        installmentNumber = source.getInstallmentNumber();

        LoanChargePaidByData loanChargePaidByData = new LoanChargePaidByData( id, amount, installmentNumber, chargeId, transactionId, name );

        return loanChargePaidByData;
    }

    @Override
    public List<LoanChargePaidByData> map(List<LoanChargePaidBy> sources) {
        if ( sources == null ) {
            return null;
        }

        List<LoanChargePaidByData> list = new ArrayList<LoanChargePaidByData>( sources.size() );
        for ( LoanChargePaidBy loanChargePaidBy : sources ) {
            list.add( map( loanChargePaidBy ) );
        }

        return list;
    }

    private Long sourceLoanTransactionId(LoanChargePaidBy loanChargePaidBy) {
        LoanTransaction loanTransaction = loanChargePaidBy.getLoanTransaction();
        if ( loanTransaction == null ) {
            return null;
        }
        return loanTransaction.getId();
    }

    private Long sourceLoanChargeId(LoanChargePaidBy loanChargePaidBy) {
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        if ( loanCharge == null ) {
            return null;
        }
        return loanCharge.getId();
    }

    private String sourceLoanChargeChargeName(LoanChargePaidBy loanChargePaidBy) {
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        if ( loanCharge == null ) {
            return null;
        }
        Charge charge = loanCharge.getCharge();
        if ( charge == null ) {
            return null;
        }
        return charge.getName();
    }
}
