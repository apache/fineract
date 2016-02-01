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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;

public class LoanRescheduleRequestEnumerations {
	
	public static EnumOptionData status(final LoanRescheduleRequestStatusEnumData status) {
		Long id = status.id();
        String code = status.code();
        String value = status.value();

        return new EnumOptionData(id, code, value);
	}

	public static LoanRescheduleRequestStatusEnumData status(final Integer statusId) {
		return status(LoanStatus.fromInt(statusId));
	}
	
	public static LoanRescheduleRequestStatusEnumData status(final LoanStatus status) {
		LoanRescheduleRequestStatusEnumData enumData = new LoanRescheduleRequestStatusEnumData(
				LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
				LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
		
		switch(status) {
			case SUBMITTED_AND_PENDING_APPROVAL:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
						LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval");
				break;
				
			case APPROVED:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.APPROVED.getValue().longValue(),
						LoanStatus.APPROVED.getCode(), "Approved");
				break;
				
			case REJECTED:
				enumData = new LoanRescheduleRequestStatusEnumData(
						LoanStatus.REJECTED.getValue().longValue(),
						LoanStatus.REJECTED.getCode(), "Rejected");
				break;
				
			default:
				break;
		}
		
		return enumData;
	}
}
