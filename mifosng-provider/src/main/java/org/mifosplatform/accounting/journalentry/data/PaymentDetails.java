package org.mifosplatform.accounting.journalentry.data;

public class PaymentDetails {

    private final String accountNumber;
    private final String checkNumber;
    private final String receiptNumber;
    private final String bankNumber;
    private final String routineCode;
    private final String paymentType;

    public PaymentDetails(final String accountNumber,
                          final String checkNumber,
                          final String receiptNumber,
                          final String bankNumber,
                          final String routineCode,
                          final String paymentType)
    {
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
        this.routineCode = routineCode ;
        this.paymentType = paymentType;

    }
}
