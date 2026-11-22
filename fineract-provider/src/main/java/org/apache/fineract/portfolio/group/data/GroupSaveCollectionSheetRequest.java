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

package org.apache.fineract.portfolio.group.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.group.domain.GroupTypes;

/**
 * Saves the collection sheet of a group or a center. The body is still validated and processed by the legacy
 * {@code CollectionSheetWritePlatformService#updateCollectionSheet(JsonCommand)}, which is shared with the individual
 * collection sheet; {@code groupId} and {@code groupingType} are transient so they stay out of that JSON.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupSaveCollectionSheetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private transient Long groupId;

    /** Group or center, taken from the endpoint that was called; never read from the request body. */
    @JsonIgnore
    @Builder.Default
    private transient GroupTypes groupingType = GroupTypes.GROUP;

    private Long calendarId;
    private String transactionDate;
    private String actualDisbursementDate;
    private String note;
    private List<CollectionSheetAttendanceItem> clientsAttendance;
    private List<CollectionSheetLoanTransactionItem> bulkRepaymentTransactions;
    private List<CollectionSheetLoanTransactionItem> bulkDisbursementTransactions;
    private List<CollectionSheetSavingsTransactionItem> bulkSavingsDueTransactions;
    private Integer paymentTypeId;
    private String accountNumber;
    private String checkNumber;
    private String routingCode;
    private String receiptNumber;
    private String bankNumber;

    @JsonProperty("isTransactionDateOnNonMeetingDate")
    @SerializedName("isTransactionDateOnNonMeetingDate")
    private Boolean transactionDateOnNonMeetingDate;

    private String locale;
    private String dateFormat;
}
