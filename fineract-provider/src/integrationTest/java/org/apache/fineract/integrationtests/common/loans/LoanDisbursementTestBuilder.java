/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.common.loans;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

public class LoanDisbursementTestBuilder {
	
	String dueDate = null;
	Float totalOriginalDueForPeriod = null;
	Float totalOutstandingForPeriod = null;
	Float interestOutstanding = null;
	Float principalOutstanding = null;
	Float principalLoanBalanceOutstanding = null;
	Float principalDue = null;
	Float principalOriginalDue = null;
	String fromDate = null; 
	
	public LoanDisbursementTestBuilder(String dueDate,Float totalOriginalDueForPeriod,Float totalOutstandingForPeriod,
			Float interestOutstanding, Float principalOutstanding, Float principalLoanBalanceOutstanding, Float principalDue,
			Float principalOriginalDue, String fromDate) {
		this.dueDate = dueDate;
		this.totalOriginalDueForPeriod = totalOriginalDueForPeriod;
		this.totalOutstandingForPeriod = totalOutstandingForPeriod;
		this.interestOutstanding = interestOutstanding;
		this.principalOutstanding = principalOutstanding;
		this.principalLoanBalanceOutstanding = principalLoanBalanceOutstanding;
		this.principalDue = principalDue;
		this.principalOriginalDue = principalOriginalDue;
		this.fromDate = fromDate; 
	}

	public String getDueDate() {
		return this.dueDate;
	}

	public Float getTotalOriginalDueForPeriod() {
		return this.totalOriginalDueForPeriod;
	}

	public Float getTotalOutstandingForPeriod() {
		return this.totalOutstandingForPeriod;
	}

	public Float getInterestOutstanding() {
		return this.interestOutstanding;
	}

	public Float getPrincipalOutstanding() {
		return this.principalOutstanding;
	}

	public Float getPrincipalLoanBalanceOutstanding() {
		return this.principalLoanBalanceOutstanding;
	}

	public Float getPrincipalDue() {
		return this.principalDue;
	}

	public Float getPrincipalOriginalDue() {
		return this.principalOriginalDue;
	}

	public String getFromDate() {
		return this.fromDate;
	}
	
	/*public HashMap<String, String> build(String dueDate,String totalOriginalDueForPeriod,String totalOutstandingForPeriod,
			String interestOutstanding, String principalOutstanding, String principalLoanBalanceOutstanding, String principalDue,
			String principalOriginalDue, String fromDate) {
		HashMap<String, String> expectedRepaymentSchedule = new HashMap<String, String>();
		expectedRepaymentSchedule.put("dueDate", dueDate);
		expectedRepaymentSchedule.put("totalOriginalDueForPeriod",
				totalOriginalDueForPeriod);
		expectedRepaymentSchedule.put("totalOutstandingForPeriod",
				totalOutstandingForPeriod);
		expectedRepaymentSchedule.put("interestOutstanding", interestOutstanding);
		expectedRepaymentSchedule.put("principalOutstanding",
				principalOutstanding);
		expectedRepaymentSchedule.put("principalLoanBalanceOutstanding",
				principalLoanBalanceOutstanding);
		expectedRepaymentSchedule.put("principalDue", principalDue);
		expectedRepaymentSchedule.put("principalOriginalDue",
				principalOriginalDue);
		expectedRepaymentSchedule.put("fromDate", fromDate);
		
		return expectedRepaymentSchedule;
	}*/
}
