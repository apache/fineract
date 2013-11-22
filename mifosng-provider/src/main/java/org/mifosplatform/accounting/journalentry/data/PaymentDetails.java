package org.mifosplatform.accounting.journalentry.data;

public class PaymentDetails {

    @SuppressWarnings("unused")
    private final String accountNumber;
    @SuppressWarnings("unused")
    private final String checkNumber;
    @SuppressWarnings("unused")
    private final String receiptNumber;
    @SuppressWarnings("unused")
    private final String bankNumber;
    @SuppressWarnings("unused")
    private final String routineCode;
    @SuppressWarnings("unused")
    private final String paymentType;

    public PaymentDetails(final String accountNumber, final String checkNumber, final String receiptNumber, final String bankNumber,
            final String routineCode, final String paymentType) {
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
        this.routineCode = routineCode;
        this.paymentType = paymentType;
    }
}