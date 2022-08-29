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
package org.apache.fineract.accounting.rule.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.office.data.OfficeData;

/**
 * Immutable object representing a General Ledger Account
 *
 * Note: no getter/setters required as google-gson will produce json from fields of object.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AccountingRuleData {

    private Long id;
    private Long officeId;
    private String officeName;
    private String name;
    private String description;
    private boolean systemDefined;
    private boolean allowMultipleDebitEntries;
    private boolean allowMultipleCreditEntries;
    private List<AccountingTagRuleData> creditTags;
    private List<AccountingTagRuleData> debitTags;

    // template
    @SuppressWarnings("unused")
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    @SuppressWarnings("unused")
    private List<GLAccountData> allowedAccounts = new ArrayList<GLAccountData>();
    @SuppressWarnings("unused")
    private Collection<CodeValueData> allowedCreditTagOptions;
    @SuppressWarnings("unused")
    private Collection<CodeValueData> allowedDebitTagOptions;
    private List<GLAccountDataForLookup> creditAccounts;
    private List<GLAccountDataForLookup> debitAccounts;

}
