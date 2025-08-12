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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.validation.constraints.DateRange;
import org.apache.fineract.validation.constraints.LocalDate;
import org.apache.fineract.validation.constraints.Locale;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldNameConstants
@LocalDate(dateField = "actualDisbursementDate", formatField = "dateFormat", localeField = "locale", message = "{collection.sheet.actual.disbursement.date.local.date}")
@LocalDate(dateField = "transactionDate", formatField = "dateFormat", localeField = "locale", message = "{collection.sheet.transaction.date.local.date}")
@DateRange(maxYearsAgo = 10, dateField = "actualDisbursementDate", formatField = "dateFormat", localeField = "locale", message = "{collection.sheet.actual.disbursement.date.format}")
@DateRange(maxYearsAgo = 10, dateField = "transactionDate", formatField = "dateFormat", localeField = "locale", message = "{collection.sheet.transaction.date.format}")
public class CollectionSheetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @PositiveOrZero(message = "{collection.sheet.officeId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{collection.sheet.officeId.digits}")
    private Long officeId;

    @Pattern(regexp = "dd MMMM yyyy", message = "{collection.sheet.dateFormat.invalid}")
    private String dateFormat;

    @NotBlank(message = "{collection.sheet.locale.notBlank}")
    @Size(min = 2, max = 50, message = "{collection.sheet.locale.size}")
    @Locale
    private String locale;

    private String actualDisbursementDate;
    private String transactionDate;
    private String note;

    @Valid
    private DisbursementTransactionsRequest bulkDisbursementTransactions;
}
