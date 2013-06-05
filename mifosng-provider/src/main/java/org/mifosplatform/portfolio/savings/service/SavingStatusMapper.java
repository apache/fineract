package org.mifosplatform.portfolio.savings.service;

public class SavingStatusMapper {
	
	private final Integer statusId;
	
	public SavingStatusMapper(Integer statusId) {
		this.statusId = statusId;
	}
	
	public boolean isPendingApproval() {
        return Integer.valueOf(100).equals(statusId);
    }
	
	public boolean isOpen() {
        return Integer.valueOf(300).equals(statusId);
    }

}
