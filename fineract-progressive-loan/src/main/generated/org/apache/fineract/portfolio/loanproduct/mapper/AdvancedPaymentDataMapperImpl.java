package org.apache.fineract.portfolio.loanproduct.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.data.AdvancedPaymentData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:33-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class AdvancedPaymentDataMapperImpl implements AdvancedPaymentDataMapper {

    @Override
    public List<AdvancedPaymentData> mapLoanProductPaymentAllocationRule(List<LoanProductPaymentAllocationRule> paymentAllocationRule) {
        if ( paymentAllocationRule == null ) {
            return null;
        }

        List<AdvancedPaymentData> list = new ArrayList<AdvancedPaymentData>( paymentAllocationRule.size() );
        for ( LoanProductPaymentAllocationRule loanProductPaymentAllocationRule : paymentAllocationRule ) {
            list.add( mapLoanProductPaymentAllocationRule( loanProductPaymentAllocationRule ) );
        }

        return list;
    }

    @Override
    public List<AdvancedPaymentData> mapLoanPaymentAllocationRule(List<LoanPaymentAllocationRule> paymentAllocationRule) {
        if ( paymentAllocationRule == null ) {
            return null;
        }

        List<AdvancedPaymentData> list = new ArrayList<AdvancedPaymentData>( paymentAllocationRule.size() );
        for ( LoanPaymentAllocationRule loanPaymentAllocationRule : paymentAllocationRule ) {
            list.add( mapLoanPaymentAllocationRule( loanPaymentAllocationRule ) );
        }

        return list;
    }

    @Override
    public AdvancedPaymentData mapLoanProductPaymentAllocationRule(LoanProductPaymentAllocationRule paymentAllocationRule) {
        if ( paymentAllocationRule == null ) {
            return null;
        }

        List<AdvancedPaymentData.PaymentAllocationOrder> paymentAllocationOrder = null;
        String transactionType = null;
        String futureInstallmentAllocationRule = null;

        paymentAllocationOrder = mapAllocationTypes( paymentAllocationRule.getAllocationTypes() );
        if ( paymentAllocationRule.getTransactionType() != null ) {
            transactionType = paymentAllocationRule.getTransactionType().name();
        }
        if ( paymentAllocationRule.getFutureInstallmentAllocationRule() != null ) {
            futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule().name();
        }

        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData( transactionType, futureInstallmentAllocationRule, paymentAllocationOrder );

        return advancedPaymentData;
    }

    @Override
    public AdvancedPaymentData mapLoanPaymentAllocationRule(LoanPaymentAllocationRule paymentAllocationRule) {
        if ( paymentAllocationRule == null ) {
            return null;
        }

        List<AdvancedPaymentData.PaymentAllocationOrder> paymentAllocationOrder = null;
        String transactionType = null;
        String futureInstallmentAllocationRule = null;

        paymentAllocationOrder = mapAllocationTypes( paymentAllocationRule.getAllocationTypes() );
        if ( paymentAllocationRule.getTransactionType() != null ) {
            transactionType = paymentAllocationRule.getTransactionType().name();
        }
        if ( paymentAllocationRule.getFutureInstallmentAllocationRule() != null ) {
            futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule().name();
        }

        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData( transactionType, futureInstallmentAllocationRule, paymentAllocationOrder );

        return advancedPaymentData;
    }
}
