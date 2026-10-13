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
package org.apache.fineract.portfolio.collectionsheet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldNameConstants
public class CollectionSheetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long officeId;
    private Long staffId;
    private String dateFormat;
    private String locale;
    private String actualDisbursementDate;
    private String transactionDate;
    private String note;
    private DisbursementTransactionsRequest bulkDisbursementTransactions;
    // The save command reads the repayment and savings lists at the top level of the request, as documented.
    private List<RepaymentTransactionRequest> bulkRepaymentTransactions;
    private List<SavingDueTransactionRequest> bulkSavingsDueTransactions;

}
