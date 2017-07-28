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
package org.apache.fineract.portfolio.accountdetails.data;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountApplicationTimelineData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountStatusEnumData;

@SuppressWarnings("unused")
public class ShareAccountSummaryData {

	private final Long id;
	private final String accountNo;
	private final Long totalApprovedShares;
	private final Long totalPendingForApprovalShares;
	private final String externalId;
	private final Long productId;
	private final String productName;
	private final String shortProductName;
	private final ShareAccountStatusEnumData status;
	private final CurrencyData currency;
	private final ShareAccountApplicationTimelineData timeline;

	public ShareAccountSummaryData(final Long id, final String accountNo,
			final String externalId, final Long productId,
			final String productName, final String shortProductName,
			final ShareAccountStatusEnumData status,
			final CurrencyData currency, final Long approvedShares,
			final Long pendingForApprovalShares,
			final ShareAccountApplicationTimelineData timeline) {
		this.id = id;
		this.accountNo = accountNo;
		this.externalId = externalId;
		if(approvedShares == null) {
			this.totalApprovedShares = new Long(0) ;
		}else {
			this.totalApprovedShares = approvedShares;	
		}
		if(pendingForApprovalShares == null) {
			this.totalPendingForApprovalShares = new Long(0) ;
		}else {
			this.totalPendingForApprovalShares = pendingForApprovalShares;	
		}
		this.productId = productId;
		this.productName = productName;
		this.shortProductName = shortProductName;
		this.status = status;
		this.currency = currency;
		this.timeline = timeline;
	}

    
    public String getAccountNo() {
        return this.accountNo;
    }

}
