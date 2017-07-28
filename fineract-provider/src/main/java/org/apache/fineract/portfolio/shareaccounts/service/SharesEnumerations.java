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
package org.apache.fineract.portfolio.shareaccounts.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountStatusEnumData;
import org.apache.fineract.portfolio.shareaccounts.domain.PurchasedSharesStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountDividendStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.apache.fineract.portfolio.shareproducts.SharePeriodFrequencyType;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividendStatusType;

public class SharesEnumerations {

    public static ShareAccountStatusEnumData status(final Integer statusEnum) {
        return status(ShareAccountStatusType.fromInt(statusEnum));
    }

    public static ShareAccountStatusEnumData status(final ShareAccountStatusType type) {
        final boolean submittedAndPendingApproval = type.isSubmittedAndPendingApproval();
        final boolean isApproved = type.isApproved();
        final boolean isRejected = type.isRejected();
        final boolean isActive = type.isActive();
        final boolean isClosed = type.isClosed();

        ShareAccountStatusEnumData optionData = null;
        switch (type) {
            case INVALID:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.INVALID.getValue().longValue(),
                        ShareAccountStatusType.INVALID.getCode(), "Invalid", submittedAndPendingApproval, isApproved, isRejected, isActive,
                        isClosed);
            break;

            case SUBMITTED_AND_PENDING_APPROVAL:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue().longValue(),
                        ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getCode(), "Submitted and pending approval",
                        submittedAndPendingApproval, isApproved, isRejected, isActive, isClosed);
            break;
            case APPROVED:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.APPROVED.getValue().longValue(),
                        ShareAccountStatusType.APPROVED.getCode(), "Approved", submittedAndPendingApproval, isApproved, isRejected,
                        isActive, isClosed);
            break;

            case ACTIVE:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.ACTIVE.getValue().longValue(),
                        ShareAccountStatusType.ACTIVE.getCode(), "Active", submittedAndPendingApproval, isApproved, isRejected, isActive,
                        isClosed);
            break;
            case REJECTED:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.REJECTED.getValue().longValue(),
                        ShareAccountStatusType.REJECTED.getCode(), "Rejected", submittedAndPendingApproval, isApproved, isRejected,
                        isActive, isClosed);
            break;
            case CLOSED:
                optionData = new ShareAccountStatusEnumData(ShareAccountStatusType.CLOSED.getValue().longValue(),
                        ShareAccountStatusType.CLOSED.getCode(), "Closed", submittedAndPendingApproval, isApproved, isRejected, isActive,
                        isClosed);
            break;
        }

        return optionData;
    }

    public static EnumOptionData purchasedSharesEnum(PurchasedSharesStatusType type) {
        EnumOptionData data = new EnumOptionData(PurchasedSharesStatusType.INVALID.getValue().longValue(),
                PurchasedSharesStatusType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case APPLIED:
                data = new EnumOptionData(PurchasedSharesStatusType.APPLIED.getValue().longValue(),
                        PurchasedSharesStatusType.APPLIED.getCode(), "Pending Approval");
            break;
            case APPROVED:
                data = new EnumOptionData(PurchasedSharesStatusType.APPROVED.getValue().longValue(),
                        PurchasedSharesStatusType.APPROVED.getCode(), "Approved");
            break;
            case REJECTED:
                data = new EnumOptionData(PurchasedSharesStatusType.REJECTED.getValue().longValue(),
                        PurchasedSharesStatusType.REJECTED.getCode(), "Rejected");
            break;
            case PURCHASED:
                data = new EnumOptionData(PurchasedSharesStatusType.PURCHASED.getValue().longValue(),
                        PurchasedSharesStatusType.PURCHASED.getCode(), "Purchase");
            break;
            case REDEEMED:
                data = new EnumOptionData(PurchasedSharesStatusType.REDEEMED.getValue().longValue(),
                        PurchasedSharesStatusType.REDEEMED.getCode(), "Redeem");
            break;
            case CHARGE_PAYMENT:
                data = new EnumOptionData(PurchasedSharesStatusType.CHARGE_PAYMENT.getValue().longValue(),
                        PurchasedSharesStatusType.CHARGE_PAYMENT.getCode(), "Charge Payment");
            break;

        }
        return data;
    }

    public static EnumOptionData purchasedSharesEnum(final Integer enumValue) {
        return purchasedSharesEnum(PurchasedSharesStatusType.fromInt(enumValue));
    }

    public static EnumOptionData lockinPeriodFrequencyType(final int id) {
        return lockinPeriodFrequencyType(SharePeriodFrequencyType.fromInt(id));
    }

    public static EnumOptionData minimumActivePeriodFrequencyType(final int id) {
        return minimumActivePeriodFrequencyType(SharePeriodFrequencyType.fromInt(id));
    }
    
    public static EnumOptionData minimumActivePeriodFrequencyType(final SharePeriodFrequencyType type) {
        final String codePrefix = "shares.minimumactive." ;
        EnumOptionData optionData = new EnumOptionData(SharePeriodFrequencyType.INVALID.getValue().longValue(),
                SharePeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }
    
    public static EnumOptionData lockinPeriodFrequencyType(final SharePeriodFrequencyType type) {
        final String codePrefix = "shares.lockin." ;
        EnumOptionData optionData = new EnumOptionData(SharePeriodFrequencyType.INVALID.getValue().longValue(),
                SharePeriodFrequencyType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case DAYS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.DAYS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.DAYS.getCode(), "Days");
            break;
            case WEEKS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.WEEKS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.WEEKS.getCode(), "Weeks");
            break;
            case MONTHS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.MONTHS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.MONTHS.getCode(), "Months");
            break;
            case YEARS:
                optionData = new EnumOptionData(SharePeriodFrequencyType.YEARS.getValue().longValue(), codePrefix
                        + SharePeriodFrequencyType.YEARS.getCode(), "Years");
            break;
        }
        return optionData;
    }

    public static EnumOptionData ShareAccountDividendStatusEnum(ShareAccountDividendStatusType type) {
        EnumOptionData data = new EnumOptionData(ShareAccountDividendStatusType.INVALID.getValue().longValue(),
                ShareAccountDividendStatusType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case POSTED:
                data = new EnumOptionData(ShareAccountDividendStatusType.POSTED.getValue().longValue(),
                        ShareAccountDividendStatusType.POSTED.getCode(), "Dividend Posted");
            break;
            case INITIATED:
                data = new EnumOptionData(ShareAccountDividendStatusType.INITIATED.getValue().longValue(),
                        ShareAccountDividendStatusType.INITIATED.getCode(), "Dividend Initiated");
            break;

        }
        return data;
    }

    public static EnumOptionData ShareAccountDividendStatusEnum(final Integer enumValue) {
        return ShareAccountDividendStatusEnum(ShareAccountDividendStatusType.fromInt(enumValue));
    }

    public static EnumOptionData ShareProductDividendStatusEnum(ShareProductDividendStatusType type) {
        EnumOptionData data = new EnumOptionData(ShareAccountDividendStatusType.INVALID.getValue().longValue(),
                ShareAccountDividendStatusType.INVALID.getCode(), "Invalid");
        switch (type) {
            case INVALID:
            break;
            case APPROVED:
                data = new EnumOptionData(ShareProductDividendStatusType.APPROVED.getValue().longValue(),
                        ShareProductDividendStatusType.APPROVED.getCode(), "Dividend Approved");
            break;
            case INITIATED:
                data = new EnumOptionData(ShareProductDividendStatusType.INITIATED.getValue().longValue(),
                        ShareProductDividendStatusType.INITIATED.getCode(), "Dividend Initiated");
            break;

        }
        return data;
    }

    public static EnumOptionData ShareProductDividendStatusEnum(final Integer enumValue) {
        return ShareProductDividendStatusEnum(ShareProductDividendStatusType.fromInt(enumValue));
    }
}
