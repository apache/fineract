package org.mifosng.data;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

@XmlRootElement(name = "loan")
@JsonFilter("myFilter")
public class LoanAccountData {

	private boolean open;
	private boolean openWithRepaymentMade;
	private boolean closed;
	private boolean interestRebateOutstanding;
	private boolean pendingApproval;
	private boolean waitingForDisbursal;
	private String lifeCycleStatusText;
	private LocalDate lifeCycleStatusDate;

	private Long id;
	private String externalId;
	private String loanProductName;

	private LocalDate submittedOnDate;
	private LocalDate approvedOnDate;

	private LocalDate expectedDisbursementDate;
	private LocalDate actualDisbursementDate;
	private boolean undoDisbursalAllowed;
	
	private LocalDate expectedFirstRepaymentOnDate;
	private LocalDate interestCalculatedFromDate;

	private LocalDate closedOnDate;

	private LocalDate expectedMaturityDate;

	private MoneyData principal;
	private BigDecimal interestRatePerYear;
	private BigDecimal interestRatePerPeriod;
	private Integer interestPeriodFrequencyType;
	private String interestPeriodFrequencyText;
	private Integer interestMethodType;
	private String interestMethodText;
	private Integer amortizationMethodValue;
	private String amortizationMethodText;
	private Integer numberOfRepayments;

	private Integer repaymentFrequencyNumber;
	private Integer repaymentFrequencyTypeEnumOrdinal;
	private String repaymentFrequencyTypeText;

	private MoneyData interestRebateOwed;

	private DerivedLoanData loanData;
	private MoneyData arrearsTolerance;
	
	// permissions on actions against loan based on the user who request loan data.
	private boolean waiveAllowed;
	private boolean makeRepaymentAllowed;
	private boolean rejectAllowed;
	private boolean withdrawnByApplicantAllowed;
	private boolean undoApprovalAllowed;
	private boolean disbursalAllowed;

	public LoanAccountData() {
		//
	}

	public LoanAccountData(
			final boolean closed, 
			final boolean open,
			final boolean openWithRepaymentMade,
			final boolean interestRebateOutstanding,
			final boolean pendingApproval, 
			final boolean waitingForDisbursal,
			final boolean undoDisbursalAllowed,
			final boolean makeRepaymentAllowed,
			final boolean rejectAllowed,
			final boolean withdrawnByApplicantAllowed,  final boolean undoApprovalAllowed, final boolean disbursalAllowed,
			final String lifeCycleStatusText,
			final LocalDate lifeCycleStatusDate,
			final Long id, String externalId,
			final String loanProductName, final LocalDate closedOnDate,
			final LocalDate submittedOnDate,
			final LocalDate approvedOnDate,
			final LocalDate expectedDisbursementDate,
			final LocalDate actualDisbursementDate,
			final LocalDate expectedMaturityDate,
			final LocalDate expectedFirstRepaymentOnDate, 
			final LocalDate interestCalculatedFromDate, 
			final MoneyData principal, final BigDecimal interestRatePerYear,
			final BigDecimal interestRatePerPeriod,
			final Integer interestPeriodFrequencyType,
			final String interestPeriodFrequencyText,
			final Integer interestMethodType,
			final String interestMethodText,
			final Integer amortizationMethodValue,
			final String amortizationMethodText,
			final Integer numberOfRepayments, 
			final Integer repayEvery,
			final Integer repaymentFrequencyValue,
			final String repaymentFrequencyText,
			final MoneyData arrearsTolerance, final DerivedLoanData derivedLoanData, 
			boolean waiveAllowed, final MoneyData interestRebateOwed) {
		this.closed = closed;
		this.open = open;
		this.openWithRepaymentMade = openWithRepaymentMade;
		this.makeRepaymentAllowed = makeRepaymentAllowed;
		this.rejectAllowed = rejectAllowed;
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
		this.undoApprovalAllowed = undoApprovalAllowed;
		this.disbursalAllowed = disbursalAllowed;
		this.lifeCycleStatusDate = lifeCycleStatusDate;
		this.externalId = externalId;
		this.undoDisbursalAllowed = undoDisbursalAllowed;
		this.interestRebateOutstanding = interestRebateOutstanding;
		this.pendingApproval = pendingApproval;
		this.waitingForDisbursal = waitingForDisbursal;
		this.lifeCycleStatusText = lifeCycleStatusText;
		this.id = id;
		this.loanProductName = loanProductName;
		this.closedOnDate = closedOnDate;
		this.submittedOnDate = submittedOnDate;
		this.approvedOnDate = approvedOnDate;
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.actualDisbursementDate = actualDisbursementDate;
		this.expectedMaturityDate = expectedMaturityDate;
		this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
		this.interestCalculatedFromDate = interestCalculatedFromDate;
		this.principal = principal;
		this.interestRatePerYear = interestRatePerYear;
		
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.interestPeriodFrequencyType = interestPeriodFrequencyType;
		this.interestPeriodFrequencyText = interestPeriodFrequencyText;
		this.interestMethodType = interestMethodType;
		this.interestMethodText = interestMethodText;
		this.amortizationMethodValue = amortizationMethodValue;
		this.amortizationMethodText = amortizationMethodText;
		this.numberOfRepayments = numberOfRepayments;
		
		this.repaymentFrequencyNumber = repayEvery;
		this.repaymentFrequencyTypeEnumOrdinal = repaymentFrequencyValue;
		this.repaymentFrequencyTypeText = repaymentFrequencyText;
		this.arrearsTolerance = arrearsTolerance;
		this.waiveAllowed = waiveAllowed;
		
		this.loanData = derivedLoanData;
		this.interestRebateOwed = interestRebateOwed;
	}
	
	public boolean isAnyActionOnLoanAllowed() {
		return isRejectAllowed() || isWithdrawnByApplicantAllowed() || isPendingApproval() || isUndoDisbursalAllowed() || isMakeRepaymentAllowed();
	}

	public int getMaxSubmittedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getSubmittedOnDate().toDateMidnight().toDateTime())
				.getDays();
	}

	public int getMaxApprovedOnOffsetFromToday() {
		
		int offset = 0;
		if (this.getApprovedOnDate() != null) {
			offset =  Days.daysBetween(new DateTime(),
					this.getApprovedOnDate().toDateMidnight().toDateTime())
					.getDays();
		}
		
		return offset;
	}

	public int getMaxDisbursedOnOffsetFromToday() {
		
		int offset = 0;
		if (this.getActualDisbursementDate() != null) {
			offset = Days.daysBetween(new DateTime(),
					this.getActualDisbursementDate().toDateMidnight().toDateTime())
					.getDays();
		}
		
		return offset;
	}
	
	public int getActualLoanTermInDays() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		if (getClosedOnDate() != null) {
			closingDateToUse = getClosedOnDate();
		}
		
		return  Days.daysBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getDays();
	}
	
	public int getActualLoanTermInMonths() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		if (getClosedOnDate() != null) {
			closingDateToUse = getClosedOnDate();
		}
		
		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getMonths();
	}
	
	public int getLoanTermInDays() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		
		return  Days.daysBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getDays();
	}
	
	public int getLoanTermInMonths() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		
		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getMonths();
	}

	public boolean isOpen() {
		return this.open;
	}

	public void setOpen(final boolean open) {
		this.open = open;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public void setClosed(final boolean closed) {
		this.closed = closed;
	}

	public boolean isInterestRebateOutstanding() {
		return this.interestRebateOutstanding;
	}

	public void setInterestRebateOutstanding(
			final boolean interestRebateOutstanding) {
		this.interestRebateOutstanding = interestRebateOutstanding;
	}

	public boolean isPendingApproval() {
		return this.pendingApproval;
	}

	public void setPendingApproval(final boolean pendingApproval) {
		this.pendingApproval = pendingApproval;
	}

	public boolean isWaitingForDisbursal() {
		return this.waitingForDisbursal;
	}

	public void setWaitingForDisbursal(final boolean waitingForDisbursal) {
		this.waitingForDisbursal = waitingForDisbursal;
	}

	public LocalDate getClosedOnDate() {
		return this.closedOnDate;
	}

	public void setClosedOnDate(final LocalDate closedOnDate) {
		this.closedOnDate = closedOnDate;
	}

	public LocalDate getSubmittedOnDate() {
		return this.submittedOnDate;
	}

	public void setSubmittedOnDate(final LocalDate submittedOnDate) {
		this.submittedOnDate = submittedOnDate;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public LocalDate getExpectedDisbursementDate() {
		return this.expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(
			final LocalDate expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public LocalDate getActualDisbursementDate() {
		return this.actualDisbursementDate;
	}

	public void setActualDisbursementDate(final LocalDate actualDisbursementDate) {
		this.actualDisbursementDate = actualDisbursementDate;
	}

	public MoneyData getPrincipal() {
		return this.principal;
	}

	public void setPrincipal(final MoneyData principal) {
		this.principal = principal;
	}

	public BigDecimal getInterestRatePerYear() {
		return this.interestRatePerYear;
	}

	public void setInterestRatePerYear(final BigDecimal interestRatePerYear) {
		this.interestRatePerYear = interestRatePerYear;
	}

	public Integer getRepaymentFrequencyNumber() {
		return this.repaymentFrequencyNumber;
	}

	public void setRepaymentFrequencyNumber(
			final Integer repaymentFrequencyNumber) {
		this.repaymentFrequencyNumber = repaymentFrequencyNumber;
	}

	public Integer getRepaymentFrequencyTypeEnumOrdinal() {
		return this.repaymentFrequencyTypeEnumOrdinal;
	}

	public void setRepaymentFrequencyTypeEnumOrdinal(
			final Integer repaymentFrequencyTypeEnumOrdinal) {
		this.repaymentFrequencyTypeEnumOrdinal = repaymentFrequencyTypeEnumOrdinal;
	}

	public String getRepaymentFrequencyTypeText() {
		return this.repaymentFrequencyTypeText;
	}

	public void setRepaymentFrequencyTypeText(
			final String repaymentFrequencyTypeText) {
		this.repaymentFrequencyTypeText = repaymentFrequencyTypeText;
	}

	public String getLoanProductName() {
		return this.loanProductName;
	}

	public void setLoanProductName(final String loanProductName) {
		this.loanProductName = loanProductName;
	}

	public LocalDate getApprovedOnDate() {
		return this.approvedOnDate;
	}

	public void setApprovedOnDate(final LocalDate approvedOnDate) {
		this.approvedOnDate = approvedOnDate;
	}

	public MoneyData getInterestRebateOwed() {
		return this.interestRebateOwed;
	}

	public void setInterestRebateOwed(final MoneyData interestRebateOwed) {
		this.interestRebateOwed = interestRebateOwed;
	}

	public String getLifeCycleStatusText() {
		return this.lifeCycleStatusText;
	}

	public void setLifeCycleStatusText(final String lifeCycleStatusText) {
		this.lifeCycleStatusText = lifeCycleStatusText;
	}

	public LocalDate getExpectedMaturityDate() {
		return this.expectedMaturityDate;
	}

	public void setMaturityDate(final LocalDate maturityDate) {
		this.expectedMaturityDate = maturityDate;
	}

	public boolean isOpenWithRepaymentMade() {
		return this.openWithRepaymentMade;
	}

	public void setOpenWithRepaymentMade(final boolean openWithRepaymentMade) {
		this.openWithRepaymentMade = openWithRepaymentMade;
	}

	public boolean isUndoDisbursalAllowed() {
		return this.undoDisbursalAllowed;
	}

	public void setUndoDisbursalAllowed(final boolean undoDisbursalAllowed) {
		this.undoDisbursalAllowed = undoDisbursalAllowed;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public Integer getInterestPeriodFrequencyType() {
		return interestPeriodFrequencyType;
	}

	public void setInterestPeriodFrequencyType(Integer interestPeriodFrequencyType) {
		this.interestPeriodFrequencyType = interestPeriodFrequencyType;
	}

	public String getInterestPeriodFrequencyText() {
		return interestPeriodFrequencyText;
	}

	public void setInterestPeriodFrequencyText(String interestPeriodFrequencyText) {
		this.interestPeriodFrequencyText = interestPeriodFrequencyText;
	}

	public Integer getInterestMethodType() {
		return interestMethodType;
	}

	public void setInterestMethodType(Integer interestMethodType) {
		this.interestMethodType = interestMethodType;
	}

	public String getInterestMethodText() {
		return interestMethodText;
	}

	public void setInterestMethodText(String interestMethodText) {
		this.interestMethodText = interestMethodText;
	}

	public Integer getAmortizationMethodValue() {
		return amortizationMethodValue;
	}

	public void setAmortizationMethodValue(Integer amortizationMethodValue) {
		this.amortizationMethodValue = amortizationMethodValue;
	}

	public String getAmortizationMethodText() {
		return amortizationMethodText;
	}

	public void setAmortizationMethodText(String amortizationMethodText) {
		this.amortizationMethodText = amortizationMethodText;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public DerivedLoanData getLoanData() {
		return loanData;
	}

	public void setLoanData(DerivedLoanData loanData) {
		this.loanData = loanData;
	}

	public MoneyData getArrearsTolerance() {
		return arrearsTolerance;
	}

	public void setArrearsTolerance(MoneyData arrearsTolerance) {
		this.arrearsTolerance = arrearsTolerance;
	}

	public boolean isWaiveAllowed() {
		return waiveAllowed;
	}

	public void setWaiveAllowed(boolean waiveAllowed) {
		this.waiveAllowed = waiveAllowed;
	}

	public boolean isMakeRepaymentAllowed() {
		return makeRepaymentAllowed;
	}

	public void setMakeRepaymentAllowed(boolean makeRepaymentAllowed) {
		this.makeRepaymentAllowed = makeRepaymentAllowed;
	}

	public LocalDate getLifeCycleStatusDate() {
		return lifeCycleStatusDate;
	}

	public void setLifeCycleStatusDate(LocalDate lifeCycleStatusDate) {
		this.lifeCycleStatusDate = lifeCycleStatusDate;
	}

	public boolean isRejectAllowed() {
		return rejectAllowed;
	}

	public void setRejectAllowed(boolean rejectAllowed) {
		this.rejectAllowed = rejectAllowed;
	}

	public boolean isWithdrawnByApplicantAllowed() {
		return withdrawnByApplicantAllowed;
	}

	public void setWithdrawnByApplicantAllowed(boolean withdrawnByApplicantAllowed) {
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
	}

	public boolean isUndoApprovalAllowed() {
		return undoApprovalAllowed;
	}

	public void setUndoApprovalAllowed(boolean undoApprovalAllowed) {
		this.undoApprovalAllowed = undoApprovalAllowed;
	}

	public boolean isDisbursalAllowed() {
		return disbursalAllowed;
	}

	public void setDisbursalAllowed(boolean disbursalAllowed) {
		this.disbursalAllowed = disbursalAllowed;
	}

	public LocalDate getExpectedFirstRepaymentOnDate() {
		return expectedFirstRepaymentOnDate;
	}

	public void setExpectedFirstRepaymentOnDate(
			LocalDate expectedFirstRepaymentOnDate) {
		this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}

	public void setExpectedMaturityDate(LocalDate expectedMaturityDate) {
		this.expectedMaturityDate = expectedMaturityDate;
	}
}