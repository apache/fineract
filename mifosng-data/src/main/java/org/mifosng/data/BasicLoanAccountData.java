package org.mifosng.data;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

@XmlRootElement(name = "loan")
public class BasicLoanAccountData {

	private boolean open = true;
	private boolean openWithRepaymentMade = true;
	private boolean closed = false;
	private boolean interestRebateOutstanding = false;
	private boolean pendingApproval = false;
	private boolean waitingForDisbursal = false;
	private String lifeCycleStatusText = "Active";
	
	private Long id = Long.valueOf(1);
	private String externalId = "1234567";
	private String loanProductName = "Standard loan 1.75%";

	private LocalDate submittedOnDate = new LocalDate();
	private String submittedOnNote = "";
	private LocalDate approvedOnDate = new LocalDate();
	private String approvedOnNote = "";

	private LocalDate expectedDisbursementDate  = new LocalDate();
	private LocalDate actualDisbursementDate  = new LocalDate();
	private String disbursedOnNote = "";
	private boolean undoDisbursalAllowed = true;

	private LocalDate closedOnDate  = new LocalDate();
	private String closedOnNote = "";

	private LocalDate maturityDate = new LocalDate();

	private MoneyData principal = MoneyData.of(new CurrencyData("XOF", "CFA", 0, "CFA", "currency.CFA"), BigDecimal.valueOf(Double.valueOf("12345.89")));
	private BigDecimal interestRatePerYear = BigDecimal.valueOf(Double.valueOf("21.0"));
	private BigDecimal interestRatePerPeriod = BigDecimal.valueOf(Double.valueOf("1.75"));
	private Integer interestPeriodFrequencyType = Integer.valueOf(2); 
	private String interestPeriodFrequencyText = "Month(s)";
	private Integer interestMethodType = Integer.valueOf(1);
	private String interestMethodText = "Declining Balance";
	private Integer amortizationMethodValue = Integer.valueOf(1);
	private String amortizationMethodText = "Equal Installments";
	private Integer numberOfRepayments = 12;

	private Integer repaymentFrequencyNumber = 1;
	private Integer repaymentFrequencyTypeEnumOrdinal = 2;
	private String repaymentFrequencyTypeText = "Month(s)";

	private MoneyData interestRebateOwed = MoneyData.of(new CurrencyData("XOF", "CFA", 0, "CFA", "currency.CFA"), BigDecimal.valueOf(Double.valueOf("0.0")));

//	private DerivedLoanData loanData;
//	private MoneyData arrearsTolerance;
	private boolean waiveAllowed = false;

	public BasicLoanAccountData() {
		//
	}

	public BasicLoanAccountData(final boolean closed, final boolean open,
			final boolean openWithRepaymentMade,
			final boolean interestRebateOutstanding,
			final boolean pendingApproval, final boolean waitingForDisbursal,
			final String lifeCycleStatusText, final Long id, String externalId,
			final String loanProductName, final LocalDate closedOnDate,
			final String closedOnNote, final LocalDate submittedOnDate,
			final String submittedOnNote, final LocalDate approvedOnDate,
			final String approvedOnNote,
			final LocalDate expectedDisbursementDate,
			final LocalDate actualDisbursementDate,
			final String disbursedOnNote, final LocalDate maturityDate,
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
		this.externalId = externalId;
		this.undoDisbursalAllowed = !openWithRepaymentMade;
		this.interestRebateOutstanding = interestRebateOutstanding;
		this.pendingApproval = pendingApproval;
		this.waitingForDisbursal = waitingForDisbursal;
		this.lifeCycleStatusText = lifeCycleStatusText;
		this.id = id;
		this.loanProductName = loanProductName;
		this.closedOnDate = closedOnDate;
		this.closedOnNote = closedOnNote;
		this.submittedOnDate = submittedOnDate;
		this.submittedOnNote = submittedOnNote;
		this.approvedOnDate = approvedOnDate;
		this.approvedOnNote = approvedOnNote;
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.actualDisbursementDate = actualDisbursementDate;
		this.disbursedOnNote = disbursedOnNote;
		this.maturityDate = maturityDate;
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
		this.waiveAllowed = waiveAllowed;
		
		this.interestRebateOwed = interestRebateOwed;
	}

	public int getMaxSubmittedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getSubmittedOnDate().toDateMidnight().toDateTime())
				.getDays();
	}

	public int getMaxApprovedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getApprovedOnDate().toDateMidnight().toDateTime())
				.getDays();
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
	
	public int getLoanTermInDays() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		return  Days.daysBetween(dateToUse.toDateMidnight().toDateTime(), this.getMaturityDate().toDateMidnight().toDateTime()).getDays();
	}
	
	public int getLoanTermInMonths() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		return  Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(), this.getMaturityDate().toDateMidnight().toDateTime()).getMonths();
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

	public String getSubmittedOnNote() {
		return this.submittedOnNote;
	}

	public void setSubmittedOnNote(final String submittedOnNote) {
		this.submittedOnNote = submittedOnNote;
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

	public String getApprovedOnNote() {
		return this.approvedOnNote;
	}

	public void setApprovedOnNote(final String approvedOnNote) {
		this.approvedOnNote = approvedOnNote;
	}

	public String getDisbursedOnNote() {
		return this.disbursedOnNote;
	}

	public void setDisbursedOnNote(final String disbursedOnNote) {
		this.disbursedOnNote = disbursedOnNote;
	}

	public String getClosedOnNote() {
		return this.closedOnNote;
	}

	public void setClosedOnNote(final String closedOnNote) {
		this.closedOnNote = closedOnNote;
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

	public LocalDate getMaturityDate() {
		return this.maturityDate;
	}

	public void setMaturityDate(final LocalDate maturityDate) {
		this.maturityDate = maturityDate;
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

	public boolean isWaiveAllowed() {
		return waiveAllowed;
	}

	public void setWaiveAllowed(boolean waiveAllowed) {
		this.waiveAllowed = waiveAllowed;
	}
}