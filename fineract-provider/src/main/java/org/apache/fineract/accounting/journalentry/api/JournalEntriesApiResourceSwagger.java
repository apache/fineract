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
package org.apache.fineract.accounting.journalentry.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.api.CurrenciesApiResourceSwagger.CurrencyItem;
import org.apache.fineract.portfolio.note.api.NotesApiResourceSwagger.GetResourceTypeResourceIdNotesResponse;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceSwagger.GetPaymentTypesResponse;

/**
 * Created by sanyam on 25/7/17.
 */
final class JournalEntriesApiResourceSwagger {

    private JournalEntriesApiResourceSwagger() {}

    @Schema(description = "PostJournalEntriesResponse")
    public static final class PostJournalEntriesResponse {

        private PostJournalEntriesResponse() {

        }

        @Schema(description = "1")
        public Long officeId;
        @Schema(description = "RS9MCISID4WK1ZM")
        public String transactionId;

    }

    @Schema(description = "PostJournalEntriesTransactionIdRequest")
    public static final class PostJournalEntriesTransactionIdRequest {

        private PostJournalEntriesTransactionIdRequest() {

        }

        @Schema(description = "1")
        public Long officeId;
    }

    @Schema(description = "PostJournalEntriesTransactionIdResponse")
    public static final class PostJournalEntriesTransactionIdResponse {

        private PostJournalEntriesTransactionIdResponse() {

        }

        @Schema(description = "1")
        public Long officeId;
    }

    static final class EnumOptionType {

        private EnumOptionType() {}

        @Schema(example = "2")
        public Long id;
        @Schema(example = "accountType.asset")
        public String code;
        @Schema(example = "ASSET")
        public String value;
    }

    static final class JournalEntryTransactionItem {

        private JournalEntryTransactionItem() {}

        static final class PaymentDetailData {

            private PaymentDetailData() {}

            @Schema(example = "62")
            public Long id;
            public GetPaymentTypesResponse paymentType;
            @Schema(example = "acc123")
            public String accountNumber;
            @Schema(example = "che123")
            public String checkNumber;
            @Schema(example = "rou123")
            public String routingCode;
            @Schema(example = "rec123")
            public String receiptNumber;
            @Schema(example = "ban123")
            public String bankNumber;
        }

        static final class TransactionDetails {

            private TransactionDetails() {}

            @Schema(example = "2")
            public Long transactionId;
            public EnumOptionType transactionType;
            public GetResourceTypeResourceIdNotesResponse noteData;
            public PaymentDetailData paymentDetails;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "L12")
        public String transactionId;
        @Schema(example = "1")
        public Long entityId;
        @Schema(example = "1")
        public Long officeId;
        @Schema(example = "Head Office")
        public String officeName;
        @Schema(example = "10")
        public Long glAccountId;
        @Schema(example = "Cash Account")
        public String glAccountName;
        @Schema(example = "0123-4567")
        public String glAccountCode;
        @Schema(example = "[2022, 07, 01]")
        public LocalDate transactionDate;
        @Schema(example = "[2022, 07, 01]")
        public LocalDate submittedOnDate;

        @Schema(example = "100.000000")
        public Double amount;
        @Schema(example = "false")
        public boolean reversed;
        @Schema(example = "false")
        public boolean manualEntry;
        @Schema(example = "Manual entry")
        public String comments;
        @Schema(example = "QWERTY")
        public String referenceNumber;
        @Schema(example = "1234.56")
        public BigDecimal officeRunningBalance;
        @Schema(example = "1234.56")
        public BigDecimal organizationRunningBalance;
        @Schema(example = "false")
        public boolean runningBalanceComputed;
        @Schema(example = "1")
        public Long createdByUserId;
        @Schema(example = "mifos")
        public String createdByUserName;
        @Schema(example = "[2022, 07, 01]")
        public LocalDate createdDate;

        public CurrencyItem currency;
        public EnumOptionType glAccountType;
        public EnumOptionType entryType;
        public EnumOptionType entityType;
        public TransactionDetails transactionDetails;
    }

    @Schema(description = "GetJournalEntriesTransactionIdResponse")
    public static final class GetJournalEntriesTransactionIdResponse {

        private GetJournalEntriesTransactionIdResponse() {}

        @Schema(example = "2")
        public Long totalFilteredRecords;
        public List<JournalEntryTransactionItem> pageItems;
    }

}
