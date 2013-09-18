package org.mifosplatform.portfolio.charge.domain;

public enum ChargePaymentMode {

    REGULAR(0, "chargepaymentmode.regular"), //
    ACCOUNT_TRANSFER(1, "chargepaymentmode.accounttransfer");

    private final Integer value;
    private final String code;

    private ChargePaymentMode(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    public static ChargePaymentMode fromInt(final Integer paymentMode) {
        ChargePaymentMode chargeAppliesToType = ChargePaymentMode.REGULAR;
        switch (paymentMode) {
            case 1:
                chargeAppliesToType = ACCOUNT_TRANSFER;
            break;
            default:
                chargeAppliesToType = REGULAR;
            break;
        }
        return chargeAppliesToType;
    }
    
    public boolean isPaymentModeAccountTransfer(){
        return this.value.equals(ChargePaymentMode.ACCOUNT_TRANSFER.getValue());
    }
}