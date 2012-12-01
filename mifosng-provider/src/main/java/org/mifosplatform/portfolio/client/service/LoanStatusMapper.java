package org.mifosplatform.portfolio.client.service;

public class LoanStatusMapper {

    private final Integer statusId;

    public LoanStatusMapper(Integer statusId) {
        this.statusId = statusId;
    }

    public boolean isPendingApproval() {
        return Integer.valueOf(100).equals(statusId);
    }

    public boolean isAwaitingDisbursal() {
        return Integer.valueOf(200).equals(statusId);
    }

    public boolean isOpen() {
        return Integer.valueOf(300).equals(statusId);
    }

    public boolean isWithdrawnByClient() {
        return Integer.valueOf(400).equals(statusId);
    }

    public boolean isRejected() {
        return Integer.valueOf(500).equals(statusId);
    }

    public boolean isClosed() {
        return Integer.valueOf(600).equals(statusId) || isWithdrawnByClient() || isRejected();
    }
}