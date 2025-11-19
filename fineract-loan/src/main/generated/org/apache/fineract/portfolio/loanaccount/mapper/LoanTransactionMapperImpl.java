package org.apache.fineract.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.StringEnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:06-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanTransactionMapperImpl implements LoanTransactionMapper {

    private final LoanTransactionRelationMapper loanTransactionRelationMapper;
    private final LoanChargePaidByMapper loanChargePaidByMapper;
    private final CurrencyMapper currencyMapper;

    @Autowired
    public LoanTransactionMapperImpl(LoanTransactionRelationMapper loanTransactionRelationMapper, LoanChargePaidByMapper loanChargePaidByMapper, CurrencyMapper currencyMapper) {

        this.loanTransactionRelationMapper = loanTransactionRelationMapper;
        this.loanChargePaidByMapper = loanChargePaidByMapper;
        this.currencyMapper = currencyMapper;
    }

    @Override
    public LoanTransactionData mapLoanTransaction(LoanTransaction loanTransaction) {
        if ( loanTransaction == null ) {
            return null;
        }

        LocalDate date = null;
        Collection<LoanChargePaidByData> loanChargePaidByList = null;
        boolean manuallyReversed = false;
        List<LoanTransactionRelationData> transactionRelations = null;
        Long officeId = null;
        String officeName = null;
        Long loanId = null;
        ExternalId externalLoanId = null;
        BigDecimal netDisbursalAmount = null;
        CurrencyData currency = null;
        Long id = null;
        BigDecimal amount = null;
        BigDecimal principalPortion = null;
        BigDecimal interestPortion = null;
        BigDecimal feeChargesPortion = null;
        BigDecimal penaltyChargesPortion = null;
        BigDecimal unrecognizedIncomePortion = null;
        ExternalId externalId = null;
        BigDecimal outstandingLoanBalance = null;
        LocalDate submittedOnDate = null;
        LocalDate transactionDate = null;
        ExternalId reversalExternalId = null;
        LocalDate reversedOnDate = null;

        date = loanTransaction.getDateOf();
        loanChargePaidByList = loanChargePaidBySetToLoanChargePaidByDataCollection( loanTransaction.getLoanChargesPaid() );
        manuallyReversed = loanTransaction.isManuallyAdjustedOrReversed();
        transactionRelations = loanTransactionRelationSetToLoanTransactionRelationDataList( loanTransaction.getLoanTransactionRelations() );
        officeId = loanTransactionOfficeId( loanTransaction );
        officeName = loanTransactionOfficeName( loanTransaction );
        loanId = loanTransactionLoanId( loanTransaction );
        externalLoanId = loanTransactionLoanExternalId( loanTransaction );
        netDisbursalAmount = loanTransactionLoanNetDisbursalAmount( loanTransaction );
        currency = currencyMapper.map( loanTransactionLoanCurrency( loanTransaction ) );
        id = loanTransaction.getId();
        amount = loanTransaction.getAmount();
        principalPortion = loanTransaction.getPrincipalPortion();
        interestPortion = loanTransaction.getInterestPortion();
        feeChargesPortion = loanTransaction.getFeeChargesPortion();
        penaltyChargesPortion = loanTransaction.getPenaltyChargesPortion();
        unrecognizedIncomePortion = loanTransaction.getUnrecognizedIncomePortion();
        externalId = loanTransaction.getExternalId();
        outstandingLoanBalance = loanTransaction.getOutstandingLoanBalance();
        submittedOnDate = loanTransaction.getSubmittedOnDate();
        transactionDate = loanTransaction.getTransactionDate();
        reversalExternalId = loanTransaction.getReversalExternalId();
        reversedOnDate = loanTransaction.getReversedOnDate();

        Integer numberOfRepayments = null;
        List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallments = null;
        Collection<CodeValueData> writeOffReasonOptions = null;
        Collection<CodeValueData> chargeOffReasonOptions = null;
        Collection<CodeValueData> reAgeReasonOptions = null;
        Collection<CodeValueData> reAmortizationReasonOptions = null;
        Collection<PeriodFrequencyType> periodFrequencyOptions = null;
        Collection<StringEnumOptionData> reAgeInterestHandlingOptions = null;
        Collection<StringEnumOptionData> reAmortizationInterestHandlingOptions = null;
        Collection<CodeValueData> classificationOptions = null;
        Collection<PaymentTypeData> paymentTypeOptions = null;
        BigDecimal overpaymentPortion = null;
        AccountTransferData transfer = null;
        BigDecimal fixedEmiAmount = null;
        String transactionType = loanTransaction.getTypeOf().name();
        LoanTransactionEnumData type = org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.transactionType(loanTransaction.getTypeOf());
        PaymentDetailData paymentDetailData = loanTransaction.getPaymentDetail() != null ? loanTransaction.getPaymentDetail().toData() : null;
        LocalDate possibleNextRepaymentDate = null;
        BigDecimal availableDisbursementAmountWithOverApplied = null;
        Integer rowIndex = null;
        String dateFormat = null;
        String locale = null;
        Long paymentTypeId = null;
        String accountNumber = null;
        Integer checkNumber = null;
        Integer routingCode = null;
        Integer receiptNumber = null;
        Integer bankNumber = null;
        Long accountId = null;
        BigDecimal transactionAmount = null;
        CodeValueData classification = loanTransaction.getClassification() != null ? loanTransaction.getClassification().toData() : null;

        LoanTransactionData loanTransactionData = new LoanTransactionData( id, loanId, externalLoanId, officeId, officeName, type, date, currency, paymentDetailData, amount, netDisbursalAmount, principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, overpaymentPortion, unrecognizedIncomePortion, externalId, transfer, fixedEmiAmount, outstandingLoanBalance, submittedOnDate, manuallyReversed, possibleNextRepaymentDate, availableDisbursementAmountWithOverApplied, loanChargePaidByList, paymentTypeOptions, writeOffReasonOptions, numberOfRepayments, rowIndex, dateFormat, locale, transactionAmount, transactionDate, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber, accountId, transactionType, loanRepaymentScheduleInstallments, reversalExternalId, reversedOnDate, transactionRelations, chargeOffReasonOptions, classificationOptions, classification, reAgeReasonOptions, periodFrequencyOptions, reAgeInterestHandlingOptions, reAmortizationReasonOptions, reAmortizationInterestHandlingOptions );

        return loanTransactionData;
    }

    protected Collection<LoanChargePaidByData> loanChargePaidBySetToLoanChargePaidByDataCollection(Set<LoanChargePaidBy> set) {
        if ( set == null ) {
            return null;
        }

        Collection<LoanChargePaidByData> collection = new ArrayList<LoanChargePaidByData>( set.size() );
        for ( LoanChargePaidBy loanChargePaidBy : set ) {
            collection.add( loanChargePaidByMapper.map( loanChargePaidBy ) );
        }

        return collection;
    }

    protected List<LoanTransactionRelationData> loanTransactionRelationSetToLoanTransactionRelationDataList(Set<LoanTransactionRelation> set) {
        if ( set == null ) {
            return null;
        }

        List<LoanTransactionRelationData> list = new ArrayList<LoanTransactionRelationData>( set.size() );
        for ( LoanTransactionRelation loanTransactionRelation : set ) {
            list.add( loanTransactionRelationMapper.map( loanTransactionRelation ) );
        }

        return list;
    }

    private Long loanTransactionOfficeId(LoanTransaction loanTransaction) {
        Office office = loanTransaction.getOffice();
        if ( office == null ) {
            return null;
        }
        return office.getId();
    }

    private String loanTransactionOfficeName(LoanTransaction loanTransaction) {
        Office office = loanTransaction.getOffice();
        if ( office == null ) {
            return null;
        }
        return office.getName();
    }

    private Long loanTransactionLoanId(LoanTransaction loanTransaction) {
        Loan loan = loanTransaction.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getId();
    }

    private ExternalId loanTransactionLoanExternalId(LoanTransaction loanTransaction) {
        Loan loan = loanTransaction.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getExternalId();
    }

    private BigDecimal loanTransactionLoanNetDisbursalAmount(LoanTransaction loanTransaction) {
        Loan loan = loanTransaction.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getNetDisbursalAmount();
    }

    private MonetaryCurrency loanTransactionLoanCurrency(LoanTransaction loanTransaction) {
        Loan loan = loanTransaction.getLoan();
        if ( loan == null ) {
            return null;
        }
        return loan.getCurrency();
    }
}
