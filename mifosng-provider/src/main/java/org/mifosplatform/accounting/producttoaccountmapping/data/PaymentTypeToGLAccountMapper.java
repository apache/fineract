package org.mifosplatform.accounting.producttoaccountmapping.data;

public class PaymentTypeToGLAccountMapper {

    @SuppressWarnings("unused")
    private final long paymentTypeId;
    @SuppressWarnings("unused")
    private final long fundSourceAccountId;

    public PaymentTypeToGLAccountMapper(long paymentTypeId, long fundSourceAccountId) {
        this.paymentTypeId = paymentTypeId;
        this.fundSourceAccountId = fundSourceAccountId;
    }

}
