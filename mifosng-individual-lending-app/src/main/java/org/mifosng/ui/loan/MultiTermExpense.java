package org.mifosng.ui.loan;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MultiTermExpense implements Serializable {

	private String name;
	private String description;
	private Integer fullTermInMonths;
	private Number fullAmount;
	private Integer applicableTermInMonths;
	private Number applicableAmount;

	public MultiTermExpense() {
		//
	}
	
	public MultiTermExpense(String name, Integer fullTermInMonths, BigDecimal fullAmount, Integer applicableTermInMonths) {
		this.name = name;
		this.fullTermInMonths = fullTermInMonths;
		this.fullAmount = fullAmount;
		this.applicableTermInMonths = applicableTermInMonths;
		this.applicableAmount = fullAmount;
		BigDecimal fullAmountAsDecimal = BigDecimal.valueOf(this.fullAmount.longValue());
		if (!fullAmountAsDecimal.equals(BigDecimal.ZERO)) {
			this.applicableAmount = fullAmountAsDecimal.divide(BigDecimal.valueOf(applicableTermInMonths.longValue()), RoundingMode.HALF_EVEN);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getFullTermInMonths() {
		return fullTermInMonths;
	}

	public void setFullTermInMonths(Integer fullTermInMonths) {
		this.fullTermInMonths = fullTermInMonths;
	}

	public Number getFullAmount() {
		return fullAmount;
	}

	public void setFullAmount(Number fullAmount) {
		this.fullAmount = fullAmount;
	}

	public Integer getApplicableTermInMonths() {
		return applicableTermInMonths;
	}

	public void setApplicableTermInMonths(Integer applicableTermInMonths) {
		this.applicableTermInMonths = applicableTermInMonths;
	}

	public Number getApplicableAmount() {
		return applicableAmount;
	}

	public void setApplicableAmount(Number applicableAmount) {
		this.applicableAmount = applicableAmount;
	}
}