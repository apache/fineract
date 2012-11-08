package org.mifosng.platform.user.domain;

public class PermissionBuilder {

	private String code;
	private String name;
	private String description;

	public Permission build() {
		return new Permission(this.code, this.description, this.name);
	}

	public PermissionBuilder userCreation() {
		this.code = "CAN_CREATE_APPLICATION_USER_ROLE";
		this.name = "User Creation";
		this.description = "Allows an application user to create other users for the application.";
		return this;
	}

	public PermissionBuilder roleCreation() {
		this.code = "CAN_CREATE_APPLICATION_ROLE_ROLE";
		this.name = "Role Creation";
		this.description = "Allows an application user to create other application roles for assigning to users.";
		return this;
	}

	public PermissionBuilder viewUsersAndRoles() {
		this.code = "CAN_VIEW_USERS_AND_ROLES_ROLE";
		this.name = "View users and roles";
		this.description = "Allows an application user to view other application users and roles in their organisation.";
		return this;
	}

	public PermissionBuilder updateApplicationPermissions() {
		this.code = "CAN_UPDATE_APPLICATION_PERMISSION_NAMES_AND_DESCRIPTIONS_ROLE";
		this.name = "Update name and descriptions of application permissions";
		this.description = "Allows an application user to update the names and description of application permissions for their organisation.";
		return this;
	}

	public PermissionBuilder viewOrganisationsOfficesStaffAndProducts() {
		this.code = "CAN_VIEW_ORGANISATION_WIDE_VIEW_OF_OFFICES_STAFF_AND_PRODUCTS_ROLE";
		this.name = "View organisations offices, staff and products";
		this.description = "Allows an application user to view all offices, staff and products for their organisation.";
		return this;
	}

	public PermissionBuilder addOffice() {
		this.code = "CAN_ADD_OFFICE_ROLE";
		this.name = "Add offices";
		this.description = "Allows an application user add new offices for their organisation.";
		return this;
	}

	public PermissionBuilder addStaff() {
		this.code = "CAN_ADD_STAFF_ROLE";
		this.name = "Add staff";
		this.description = "Allows an application user add new staff members for their organisation.";
		return this;
	}

	public PermissionBuilder addLoanProduct() {
		this.code = "CAN_ADD_LOAN_PRODUCT_ROLE";
		this.name = "Add loan product";
		this.description = "Allows an application user add new loan products for their organisation.";
		return this;
	}

	public PermissionBuilder viewLoanPortfolio() {
		this.code = "CAN_VIEW_LOAN_PORTFOLIO_ROLE";
		this.name = "View client, group and loan information";
		this.description = "Allows an application user to view client, group and loan information for their organisation.";
		return this;
	}

	public PermissionBuilder addLoan() {
		this.code = "CAN_ADD_LOAN_ROLE";
		this.name = "Add loan.";
		this.description = "Allows an application user to add a loan.";
		return this;
	}

	public PermissionBuilder addBackdatedLoan() {
		this.code = "CAN_ADD_BACKDATED_LOAN_ROLE";
		this.name = "Add backdated loan.";
		this.description = "Allows an application user to add a loan where the submitted on date is in the past.";
		return this;
	}

	public PermissionBuilder approveLoan() {
		this.code = "CAN_APPROVE_LOAN_ROLE";
		this.name = "Approve loan.";
		this.description = "Allows an application user to approve a loan.";
		return this;
	}

	public PermissionBuilder approveLoanInThePast() {
		this.code = "CAN_APPROVE_LOAN_IN_THE_PAST_ROLE";
		this.name = "Approve loan with date in past.";
		this.description = "Allows an application user to approve a loan with a date in the past.";
		return this;
	}

	public PermissionBuilder rejectLoan() {
		this.code = "CAN_REJECT_LOAN_ROLE";
		this.name = "Reject loan.";
		this.description = "Allows an application user to reject the loan application.";
		return this;
	}

	public PermissionBuilder rejectLoanInThePast() {
		this.code = "CAN_REJECT_LOAN_IN_THE_PAST_ROLE";
		this.name = "Reject loan with date in past.";
		this.description = "Allows an application user to reject the loan application with a date in the past.";
		return this;
	}

	public PermissionBuilder withdrawLoan() {
		this.code = "CAN_WITHDRAW_LOAN_ROLE";
		this.name = "Withdraw loan.";
		this.description = "Allows an application user to withdraw the loan application.";
		return this;
	}

	public PermissionBuilder withdrawLoanInThePast() {
		this.code = "CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE";
		this.name = "Withdraw loan with date in past.";
		this.description = "Allows an application user to withraw the loan application with a date in the past.";
		return this;
	}

	public PermissionBuilder undoLoanApproval() {
		this.code = "CAN_UNDO_LOAN_APPROVAL_ROLE";
		this.name = "Undo loan approval.";
		this.description = "Allows an application user to undo a loan approval.";
		return this;
	}

	public PermissionBuilder disburseLoan() {
		this.code = "CAN_DISBURSE_LOAN_ROLE";
		this.name = "Disburse loan.";
		this.description = "Allows an application user to disburse the loan.";
		return this;
	}

	public PermissionBuilder disburseLoanInThePast() {
		this.code = "CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE";
		this.name = "Disburse loan with date in past.";
		this.description = "Allows an application user to disburse the loan with a date in the past.";
		return this;
	}

	public PermissionBuilder undoLoanDisbursal() {
		this.code = "CAN_UNDO_LOAN_DISBURSAL_ROLE";
		this.name = "Undo loan disbursal.";
		this.description = "Allows an application user to undo a loan disbursal (if no payments already made).";
		return this;
	}

	public PermissionBuilder makeLoanRepayment() {
		this.code = "CAN_MAKE_LOAN_REPAYMENT_ROLE";
		this.name = "Make loan repayment.";
		this.description = "Allows an application user to make a repayment against a loan.";
		return this;
	}

	public PermissionBuilder makeLoanRepaymentInThePast() {
		this.code = "CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE";
		this.name = "Make loan repayment with date in past.";
		this.description = "Allows an application user to disburse the loan with a date in the past.";
		return this;
	}

	public PermissionBuilder writeoffLoan() {
		this.code = "CAN_WRITEOFF_LOAN_ROLE";
		this.name = "Write off loan.";
		this.description = "Allows an application user to write off the loan application.";
		return this;
	}

	public PermissionBuilder writeoffLoanInThePast() {
		this.code = "CAN_WRITEOFF_LOAN_IN_THE_PAST_ROLE";
		this.name = "Write off loan with date in past.";
		this.description = "Allows an application user to write off the loan application with a date in the past.";
		return this;
	}

	public PermissionBuilder rescheduleLoan() {
		this.code = "CAN_RESCHEDULE_LOAN_ROLE";
		this.name = "Reschedule loan.";
		this.description = "Allows an application user to reschedule the loan application.";
		return this;
	}

	public PermissionBuilder rescheduleInThePast() {
		this.code = "CAN_RESCHEDULE_LOAN_IN_THE_PAST_ROLE";
		this.name = "Reschedule loan with date in past.";
		this.description = "Allows an application user to reschedule the loan application with a date in the past.";
		return this;
	}

	public PermissionBuilder dataMigration() {
		this.code = "CAN_MIGRATE_DATA_ROLE";
		this.name = "Upload client, group and loan data onto application in bulk";
		this.description = "Allows an application user to upload client, group and loan information in bulk for their organisation.";
		return this;
	}
}