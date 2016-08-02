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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;

@SuppressWarnings("unused")
public class GroupLoanIndividualMonitoringData {
	
	private final Long id;
	private final Long loanId;
	private final BigDecimal totalLoanAmount;
	private final Long clientId;
	private final String clientName;
	private final String clientExternalID;
	private BigDecimal proposedAmount;
	private BigDecimal approvedAmount;
	private BigDecimal disbursedAmount;
	private CodeValueData loanPurpose;
	private Boolean isSelected;
	
	public GroupLoanIndividualMonitoringData(final Long id, final Long loanId, final BigDecimal totalLoanAmount, final Long clientId,
			final String clientName, final String clientExternalID, final BigDecimal proposedAmount, final BigDecimal approvedAmount,
			final BigDecimal disbursedAmount, final CodeValueData loanPurpose, final Boolean isSelected) {
		this.id = id;
		this.loanId = loanId;
		this.totalLoanAmount = totalLoanAmount;
		this.clientId = clientId;
		this.clientName = clientName;
		this.proposedAmount = proposedAmount;
		this.approvedAmount = approvedAmount;
		this.disbursedAmount = disbursedAmount;
		this.loanPurpose = loanPurpose;
		this.clientExternalID = clientExternalID;
		this.isSelected = isSelected;
	}
	
	public static GroupLoanIndividualMonitoringData instance(final Long id, final Long loanId, final BigDecimal totalLoanAmount, final Long clientId,
			final String clientName, final String clientExternalID, final BigDecimal proposedAmount, final BigDecimal approvedAmount,
			final BigDecimal disbursedAmount, final CodeValueData loanPurpose, final Boolean isSelected){
		
		return new GroupLoanIndividualMonitoringData(id, loanId, totalLoanAmount, clientId, clientName, clientExternalID, proposedAmount, approvedAmount, disbursedAmount, loanPurpose, isSelected);
	}
	
}
