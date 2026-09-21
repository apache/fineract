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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupSaveCollectionSheetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotNull(message = "{org.apache.fineract.portfolio.group.calendar-id.not-null}")
    private Long calendarId;
    @NotNull(message = "{org.apache.fineract.portfolio.group.transaction-date.not-null}")
    private String transactionDate;
    private String actualDisbursementDate;
    @Length(max = 1000, message = "{org.apache.fineract.portfolio.group.note.max}")
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
    private Boolean isTransactionDateOnNonMeetingDate;
    private String dateFormat;
    private String locale;
}
