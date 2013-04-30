/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.organisation.office.data.OfficeData;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class AccountingRuleData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final Long accountToDebitId;
    @SuppressWarnings("unused")
    private final Long accountToCreditId;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final boolean systemDefined;

    // template
    @SuppressWarnings("unused")
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    @SuppressWarnings("unused")
    private List<GLAccountData> allowedAccounts = new ArrayList<GLAccountData>();

    public AccountingRuleData(Long id, Long accountToDebitId, Long accountToCreditId) {
        this(id, null, null, accountToDebitId, accountToCreditId, null, null, true, null, null);
    }

    public AccountingRuleData(Long id, Long officeId, String officeName, Long accountToDebitId, Long accountToCreditId, String name,
            String description, boolean systemDefined) {
        this(id, officeId, officeName, accountToDebitId, accountToCreditId, name, description, systemDefined, null, null);
    }

    public AccountingRuleData(Long id, Long officeId, String officeName, Long accountToDebitId, Long accountToCreditId, String name,
            String description, boolean systemDefined, List<OfficeData> allowedOffices, List<GLAccountData> allowedAccounts) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.accountToDebitId = accountToDebitId;
        this.accountToCreditId = accountToCreditId;
        this.name = name;
        this.description = description;
        this.systemDefined = systemDefined;
        this.allowedOffices = allowedOffices;
        this.allowedAccounts = allowedAccounts;
    }

    public void setAllowedOffices(List<OfficeData> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }

    public void setAllowedAccounts(List<GLAccountData> allowedAccounts) {
        this.allowedAccounts = allowedAccounts;
    }

}