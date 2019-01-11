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
package org.apache.fineract.organisation.teller.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.domain.CashierTxnType;
import org.apache.fineract.organisation.teller.domain.TellerStatus;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Created by sanyam on 20/8/17.
 */
final class TellerApiResourceSwagger {
    private TellerApiResourceSwagger(){

    }

    @ApiModel(value = "GetTellersResponse")
    public static final class GetTellersResponse {
        private GetTellersResponse() {

        }
        @ApiModelProperty(example = "3")
        public Long id;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "0")
        public Long debitAccountId;
        @ApiModelProperty(example = "0")
        public Long creditAccountId;
        @ApiModelProperty(example = "Teller3")
        public String name;
        @ApiModelProperty(example = "[2015,2,1]")
        public LocalDate startDate;
        @ApiModelProperty(example = "ACTIVE")
        public TellerStatus status;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
    }

    @ApiModel(value = "PostTellersRequest")
    public static final class PostTellersRequest {
        private PostTellersRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Teller3")
        public String name;
        @ApiModelProperty(example = "cash handling")
        public String description;
        @ApiModelProperty(example = "ACTIVE")
        public TellerStatus status;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 February 2015")
        public LocalDate startDate;

    }

    @ApiModel(value = "PostTellersResponse")
    public static final class PostTellersResponse {
        private PostTellersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "5")
        public Long resourceId;
    }

    @ApiModel(value = "PutTellersRequest")
    public static final class PutTellersRequest {
        private PutTellersRequest() {

        }
        @ApiModelProperty(example = "Teller3")
        public String name;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "teller cash handling")
        public String description;
        @ApiModelProperty(example = "ACTIVE")
        public TellerStatus status;
        @ApiModelProperty(example = "28 February 2015")
        public LocalDate endDate;
        @ApiModelProperty(example = "01 February 2015")
        public LocalDate startDate;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;

    }

    @ApiModel(value = "PutTellersResponse")
    public static final class PutTellersResponse {
        private PutTellersResponse() {

        }
        final class PutTellersResponseChanges {
            private PutTellersResponseChanges() {}
            @ApiModelProperty(example = "teller cash handling")
            public String description;
            @ApiModelProperty(example = "28 February 2015")
            public LocalDate endDate;
            @ApiModelProperty(example = "01 February 2015")
            public LocalDate startDate;
            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd-MM-yyyy")
            public String dateFormat;
        }
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "5")
        public Long resourceId;
        public PutTellersResponseChanges changes;

    }

    @ApiModel(value = "GetTellersTellerIdCashiersResponse")
    public static final class GetTellersTellerIdCashiersResponse {
        private GetTellersTellerIdCashiersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long tellerId;
        @ApiModelProperty(example = "Teller1")
        public String tellerName;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        public Collection<CashierData> cashiers;

    }

    @ApiModel(value = "PostTellersTellerIdCashiersRequest")
    public static final class PostTellersTellerIdCashiersRequest {
        private PostTellersTellerIdCashiersRequest() {

        }
        @ApiModelProperty(example = "28 February 2015")
        public LocalDate endDate;
        @ApiModelProperty(example = "teller cash handling")
        public String description;
        @ApiModelProperty(example = "true")
        public Boolean isFullDay;
        @ApiModelProperty(example = "3")
        public Long staffId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 February 2015")
        public LocalDate startDate;

    }

    @ApiModel(value = "PostTellersTellerIdCashiersResponse")
    public static final class PostTellersTellerIdCashiersResponse {
        private PostTellersTellerIdCashiersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        @ApiModelProperty(example = "2")
        public Long subResourceId;

    }

    @ApiModel(value = "GetTellersTellerIdCashiersCashierIdResponse")
    public static final class GetTellersTellerIdCashiersCashierIdResponse {
        private GetTellersTellerIdCashiersCashierIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "1")
        public Long tellerId;
        @ApiModelProperty(example = "1")
        public Long staffId;
        @ApiModelProperty(example = "")
        public String description;
        @ApiModelProperty(example = "Feb 20, 2015 12:00:00 AM")
        public LocalDate startDate;
        @ApiModelProperty(example = "Feb 27, 2015 12:00:00 AM")
        public LocalDate endDate;
        @ApiModelProperty(example = "true")
        public Boolean isFullDay;
        @ApiModelProperty(example = "")
        public String startTime;
        @ApiModelProperty(example = "")
        public String endTime;
        @ApiModelProperty(example = "Teller1")
        public String tellerName;
        @ApiModelProperty(example = "Staff1, Test")
        public String staffName;

    }

    @ApiModel(value = "PutTellersTellerIdCashiersCashierIdRequest")
    public static final class PutTellersTellerIdCashiersCashierIdRequest {
        private PutTellersTellerIdCashiersCashierIdRequest() {

        }
        @ApiModelProperty(example = "25 February 2015")
        public LocalDate endDate;
        @ApiModelProperty(example = "Cashier updated.")
        public String description;
        @ApiModelProperty(example = "true")
        public Boolean isFullDay;
        @ApiModelProperty(example = "1")
        public Long staffId;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 February 2015")
        public LocalDate startDate;

    }

    @ApiModel(value = "PutTellersTellerIdCashiersCashierIdResponse")
    public static final class PutTellersTellerIdCashiersCashierIdResponse {
        private PutTellersTellerIdCashiersCashierIdResponse() {

        }
        final class PutTellersTellerIdCashiersCashierIdResponseChanges {
            private PutTellersTellerIdCashiersCashierIdResponseChanges() {}
            @ApiModelProperty(example = "25 February 2015")
            public LocalDate endDate;
            @ApiModelProperty(example = "Cashier updated.")
            public String description;
            @ApiModelProperty(example = "en")
            public String locale;
            @ApiModelProperty(example = "dd-MM-yyyy")
            public String dateFormat;
        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        @ApiModelProperty(example = "2")
        public Long subResourceId;
        public PutTellersTellerIdCashiersCashierIdResponseChanges changes;
    }

    @ApiModel(value = "DeleteTellersTellerIdCashiersCashierIdResponse")
    public static final class DeleteTellersTellerIdCashiersCashierIdResponse {
        private DeleteTellersTellerIdCashiersCashierIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "GetTellersTellerIdCashiersTemplateResponse")
    public static final class GetTellersTellerIdCashiersTemplateResponse {
        private GetTellersTellerIdCashiersTemplateResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long tellerId;
        @ApiModelProperty(example = "Teller1")
        public String tellerName;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        public Collection<StaffData> staffOptions;
    }

    @ApiModel(value = "GetTellersTellerIdCashiersCashiersIdTransactionsResponse")
    public static final class GetTellersTellerIdCashiersCashiersIdTransactionsResponse {
        private GetTellersTellerIdCashiersCashiersIdTransactionsResponse() {

        }
        @ApiModelProperty(example = "8")
        public Long id;
        @ApiModelProperty(example = "15")
        public Long cashierId;
        public CashierTxnType txnType;
        @ApiModelProperty(example = "1000")
        public BigDecimal txnAmount;
        @ApiModelProperty(example = "Feb 25, 2015 12:00:00 AM")
        public Date txnDate;
        @ApiModelProperty(example = "2")
        public Long entityId;
        @ApiModelProperty(example = "loans")
        public String entityType;
        @ApiModelProperty(example = "Disbursement, Loan:2-000000002,Client:1-Test 1")
        public String txnNote;
        @ApiModelProperty(example = "Feb 25, 2015 12:00:00 AM")
        public Date createdDate;
        @ApiModelProperty(example = "1")
        public Long officeId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "0")
        public Long tellerId;
        @ApiModelProperty(example = "B, Ramesh")
        public String cashierName;
    }

    @ApiModel(value = "PostTellersTellerIdCashiersCashierIdAllocateRequest")
    public static final class PostTellersTellerIdCashiersCashierIdAllocateRequest {
        private PostTellersTellerIdCashiersCashierIdAllocateRequest() {

        }
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "5000")
        public BigDecimal txnAmount;
        @ApiModelProperty(example = "allocating cash")
        public String txnNote;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "01 February 2015")
        public Date txnDate;
    }

    @ApiModel(value = "PostTellersTellerIdCashiersCashierIdAllocateResponse")
    public static final class PostTellersTellerIdCashiersCashierIdAllocateResponse {
        private PostTellersTellerIdCashiersCashierIdAllocateResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        @ApiModelProperty(example = "4")
        public Long subResourceId;
    }

    @ApiModel(value = "PostTellersTellerIdCashiersCashierIdSettleRequest")
    public static final class PostTellersTellerIdCashiersCashierIdSettleRequest {
        private PostTellersTellerIdCashiersCashierIdSettleRequest() {

        }
        @ApiModelProperty(example = "USD")
        public String currencyCode;
        @ApiModelProperty(example = "2000")
        public BigDecimal txnAmount;
        @ApiModelProperty(example = "cash settlement")
        public String txnNote;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd-MM-yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "20 February 2015")
        public Date txnDate;

    }

    @ApiModel(value = "PostTellersTellerIdCashiersCashierIdSettleResponse")
    public static final class PostTellersTellerIdCashiersCashierIdSettleResponse {
        private PostTellersTellerIdCashiersCashierIdSettleResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
        @ApiModelProperty(example = "5")
        public Long subResourceId;
    }

    @ApiModel(value = "GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse")
    public static final class GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse {
        private GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse() {

        }
        @ApiModelProperty(example = "7000.000000")
        public BigDecimal sumCashAllocation;
        @ApiModelProperty(example = "0")
        public BigDecimal sumInwardCash;
        @ApiModelProperty(example = "0")
        public BigDecimal sumOutwardCash;
        @ApiModelProperty(example = "50.000000")
        public BigDecimal sumCashSettlement;
        @ApiModelProperty(example = "6950.000000")
        public BigDecimal netCash;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "1")
        public long tellerId;
        @ApiModelProperty(example = "Teller1")
        public String tellerName;
        @ApiModelProperty(example = "1")
        public long cashierId;
        @ApiModelProperty(example = "Staff1, Test")
        public String cashierName;
        public Page<CashierTransactionData> cashierTransactions;

    }

    @ApiModel(value = "GetTellersTellerIdCashiersCashiersIdTransactionsTemplateResponse")
    public static final class GetTellersTellerIdCashiersCashiersIdTransactionsTemplateResponse {
        private GetTellersTellerIdCashiersCashiersIdTransactionsTemplateResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long cashierId;
        @ApiModelProperty(example = "Head Office")
        public String officeName;
        @ApiModelProperty(example = "0")
        public Long tellerId;
        @ApiModelProperty(example = "Teller1")
        public String tellerName;
        @ApiModelProperty(example = "Staff1, Test")
        public String cashierName;
        public CashierData cashierData;
        @ApiModelProperty(example = "Feb 20, 2015 12:00:00 AM")
        public LocalDate startDate;
        @ApiModelProperty(example = "Feb 27, 2015 12:00:00 AM")
        public LocalDate endDate;
        public Collection<CurrencyData> currencyOptions;
    }

}
