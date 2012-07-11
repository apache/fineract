package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Command used for transfering money between two branches.
 */
@XmlRootElement
public class BranchMoneyTransferCommand {

	private Long id;
	private Long fromOfficeId;
	private Long toOfficeId;
	
	private String dateFormat;
	private String transactionDate;
	private LocalDate transactionLocalDate;
	
	private String locale;
	private String transactionAmount;
	private BigDecimal transactionAmountValue;
	
	protected BranchMoneyTransferCommand() {
		//
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFromOfficeId() {
		return fromOfficeId;
	}

	public void setFromOfficeId(Long fromOfficeId) {
		this.fromOfficeId = fromOfficeId;
	}

	public Long getToOfficeId() {
		return toOfficeId;
	}

	public void setToOfficeId(Long toOfficeId) {
		this.toOfficeId = toOfficeId;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public LocalDate getTransactionLocalDate() {
		return transactionLocalDate;
	}

	public void setTransactionLocalDate(LocalDate transactionLocalDate) {
		this.transactionLocalDate = transactionLocalDate;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(String transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public BigDecimal getTransactionAmountValue() {
		return transactionAmountValue;
	}

	public void setTransactionAmountValue(BigDecimal transactionAmountValue) {
		this.transactionAmountValue = transactionAmountValue;
	}
}