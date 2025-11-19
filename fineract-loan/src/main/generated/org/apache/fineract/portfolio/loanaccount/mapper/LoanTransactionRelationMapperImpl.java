package org.apache.fineract.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:07-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanTransactionRelationMapperImpl implements LoanTransactionRelationMapper {

    @Override
    public LoanTransactionRelationData map(LoanTransactionRelation source) {
        if ( source == null ) {
            return null;
        }

        Long fromLoanTransaction = null;
        Long toLoanTransaction = null;
        Long toLoanCharge = null;
        BigDecimal amount = null;
        String paymentType = null;
        LoanTransactionRelationTypeEnum relationType = null;

        fromLoanTransaction = sourceFromTransactionId( source );
        toLoanTransaction = sourceToTransactionId( source );
        toLoanCharge = sourceToChargeId( source );
        amount = sourceToTransactionAmount( source );
        paymentType = sourceToTransactionPaymentDetailPaymentTypeName( source );
        relationType = source.getRelationType();

        LoanTransactionRelationData loanTransactionRelationData = new LoanTransactionRelationData( fromLoanTransaction, toLoanTransaction, toLoanCharge, relationType, amount, paymentType );

        return loanTransactionRelationData;
    }

    @Override
    public List<LoanTransactionRelationData> map(List<LoanTransactionRelation> sources) {
        if ( sources == null ) {
            return null;
        }

        List<LoanTransactionRelationData> list = new ArrayList<LoanTransactionRelationData>( sources.size() );
        for ( LoanTransactionRelation loanTransactionRelation : sources ) {
            list.add( map( loanTransactionRelation ) );
        }

        return list;
    }

    private Long sourceFromTransactionId(LoanTransactionRelation loanTransactionRelation) {
        LoanTransaction fromTransaction = loanTransactionRelation.getFromTransaction();
        if ( fromTransaction == null ) {
            return null;
        }
        return fromTransaction.getId();
    }

    private Long sourceToTransactionId(LoanTransactionRelation loanTransactionRelation) {
        LoanTransaction toTransaction = loanTransactionRelation.getToTransaction();
        if ( toTransaction == null ) {
            return null;
        }
        return toTransaction.getId();
    }

    private Long sourceToChargeId(LoanTransactionRelation loanTransactionRelation) {
        LoanCharge toCharge = loanTransactionRelation.getToCharge();
        if ( toCharge == null ) {
            return null;
        }
        return toCharge.getId();
    }

    private BigDecimal sourceToTransactionAmount(LoanTransactionRelation loanTransactionRelation) {
        LoanTransaction toTransaction = loanTransactionRelation.getToTransaction();
        if ( toTransaction == null ) {
            return null;
        }
        return toTransaction.getAmount();
    }

    private String sourceToTransactionPaymentDetailPaymentTypeName(LoanTransactionRelation loanTransactionRelation) {
        LoanTransaction toTransaction = loanTransactionRelation.getToTransaction();
        if ( toTransaction == null ) {
            return null;
        }
        PaymentDetail paymentDetail = toTransaction.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        PaymentType paymentType = paymentDetail.getPaymentType();
        if ( paymentType == null ) {
            return null;
        }
        return paymentType.getName();
    }
}
