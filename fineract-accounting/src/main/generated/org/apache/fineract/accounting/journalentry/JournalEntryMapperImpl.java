package org.apache.fineract.accounting.journalentry;

import javax.annotation.processing.Generated;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:30:14-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class JournalEntryMapperImpl implements JournalEntryMapper {

    @Override
    public JournalEntryData map(JournalEntry journalEntry) {
        if ( journalEntry == null ) {
            return null;
        }

        JournalEntryData journalEntryData = new JournalEntryData();

        journalEntryData.setId( journalEntry.getId() );
        journalEntryData.setOfficeId( journalEntryOfficeId( journalEntry ) );
        journalEntryData.setOfficeName( journalEntryOfficeName( journalEntry ) );
        journalEntryData.setGlAccountId( journalEntryGlAccountId( journalEntry ) );
        journalEntryData.setGlAccountCode( journalEntryGlAccountGlCode( journalEntry ) );
        journalEntryData.setGlAccountName( journalEntryGlAccountName( journalEntry ) );
        journalEntryData.setGlAccountType( mapGlAccountType( mapGlAccountType( journalEntryGlAccountType( journalEntry ) ) ) );
        journalEntryData.setTransactionDate( journalEntry.getTransactionDate() );
        journalEntryData.setEntryType( mapJournalEntryType( mapJournalEntryType( journalEntry.getType() ) ) );
        journalEntryData.setAmount( journalEntry.getAmount() );
        journalEntryData.setEntityType( mapEntityType( mapEntityType( journalEntry.getEntityType() ) ) );
        journalEntryData.setEntityId( journalEntry.getEntityId() );
        journalEntryData.setSubmittedOnDate( journalEntry.getSubmittedOnDate() );
        journalEntryData.setTransactionId( journalEntry.getTransactionId() );
        journalEntryData.setCurrency( mapCurrency( journalEntry.getCurrencyCode() ) );
        journalEntryData.setManualEntry( journalEntry.isManualEntry() );
        journalEntryData.setReversed( journalEntry.isReversed() );
        journalEntryData.setReferenceNumber( journalEntry.getReferenceNumber() );
        journalEntryData.setPaymentTypeId( journalEntryPaymentDetailPaymentTypeId( journalEntry ) );
        journalEntryData.setAccountNumber( journalEntryPaymentDetailAccountNumber( journalEntry ) );
        journalEntryData.setCheckNumber( journalEntryPaymentDetailCheckNumber( journalEntry ) );
        journalEntryData.setRoutingCode( journalEntryPaymentDetailRoutingCode( journalEntry ) );
        journalEntryData.setReceiptNumber( journalEntryPaymentDetailReceiptNumber( journalEntry ) );
        journalEntryData.setBankNumber( journalEntryPaymentDetailBankNumber( journalEntry ) );
        journalEntryData.setCurrencyCode( journalEntry.getCurrencyCode() );

        return journalEntryData;
    }

    private Long journalEntryOfficeId(JournalEntry journalEntry) {
        Office office = journalEntry.getOffice();
        if ( office == null ) {
            return null;
        }
        return office.getId();
    }

    private String journalEntryOfficeName(JournalEntry journalEntry) {
        Office office = journalEntry.getOffice();
        if ( office == null ) {
            return null;
        }
        return office.getName();
    }

    private Long journalEntryGlAccountId(JournalEntry journalEntry) {
        GLAccount glAccount = journalEntry.getGlAccount();
        if ( glAccount == null ) {
            return null;
        }
        return glAccount.getId();
    }

    private String journalEntryGlAccountGlCode(JournalEntry journalEntry) {
        GLAccount glAccount = journalEntry.getGlAccount();
        if ( glAccount == null ) {
            return null;
        }
        return glAccount.getGlCode();
    }

    private String journalEntryGlAccountName(JournalEntry journalEntry) {
        GLAccount glAccount = journalEntry.getGlAccount();
        if ( glAccount == null ) {
            return null;
        }
        return glAccount.getName();
    }

    private Integer journalEntryGlAccountType(JournalEntry journalEntry) {
        GLAccount glAccount = journalEntry.getGlAccount();
        if ( glAccount == null ) {
            return null;
        }
        return glAccount.getType();
    }

    private Long journalEntryPaymentDetailPaymentTypeId(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        PaymentType paymentType = paymentDetail.getPaymentType();
        if ( paymentType == null ) {
            return null;
        }
        return paymentType.getId();
    }

    private String journalEntryPaymentDetailAccountNumber(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        return paymentDetail.getAccountNumber();
    }

    private String journalEntryPaymentDetailCheckNumber(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        return paymentDetail.getCheckNumber();
    }

    private String journalEntryPaymentDetailRoutingCode(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        return paymentDetail.getRoutingCode();
    }

    private String journalEntryPaymentDetailReceiptNumber(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        return paymentDetail.getReceiptNumber();
    }

    private String journalEntryPaymentDetailBankNumber(JournalEntry journalEntry) {
        PaymentDetail paymentDetail = journalEntry.getPaymentDetail();
        if ( paymentDetail == null ) {
            return null;
        }
        return paymentDetail.getBankNumber();
    }
}
