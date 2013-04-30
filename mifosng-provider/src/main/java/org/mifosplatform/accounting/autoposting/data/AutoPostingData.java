/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.rule.data.AccountingRuleData;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;

/**
 * Immutable object representing a Auto posting rule
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class AutoPostingData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private OfficeData officeData;
    @SuppressWarnings("unused")
    private EnumOptionData productType;
    @SuppressWarnings("unused")
    private LoanProductData loanProductData;
    @SuppressWarnings("unused")
    private SavingsProductData savingProductData;
    @SuppressWarnings("unused")
    private ChargeData charge;
    @SuppressWarnings("unused")
    private CodeData event;
    @SuppressWarnings("unused")
    private CodeValueData eventAttribute;
    @SuppressWarnings("unused")
    private AccountingRuleData accountingRule;

    // template
    @SuppressWarnings("unused")
    private List<EnumOptionData> allowedProductTypes = new ArrayList<EnumOptionData>();
    @SuppressWarnings("unused")
    private List<OfficeData> allowedOffices = new ArrayList<OfficeData>();
    @SuppressWarnings("unused")
    private List<SavingsProductData> allowedSavingProducts = new ArrayList<SavingsProductData>();
    @SuppressWarnings("unused")
    private List<LoanProductData> allowedLoanProducts = new ArrayList<LoanProductData>();
    @SuppressWarnings("unused")
    private List<CodeValue> allowedEventAttributes = new ArrayList<CodeValue>();

    public AutoPostingData(Long id, String name, String description, OfficeData officeData, EnumOptionData productType,
            LoanProductData loanProductData, SavingsProductData savingsProductData, ChargeData charge, CodeData event,
            CodeValueData eventAttribute, AccountingRuleData accountingRule) {
        this(id, name, description, officeData, productType, loanProductData, savingsProductData, charge, event, eventAttribute,
                accountingRule, null, null, null, null, null);
    }

    private AutoPostingData(Long id, String name, String description, OfficeData officeData, EnumOptionData productType,
            LoanProductData loanProductData, SavingsProductData savingsProductData, ChargeData charge, CodeData event,
            CodeValueData eventAttribute, AccountingRuleData accountingRule, List<EnumOptionData> allowedProductTypes,
            List<OfficeData> allowedOffices, List<SavingsProductData> allowedSavingProducts, List<LoanProductData> allowedLoanProducts,
            List<CodeValue> allowedEventAttributes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.officeData = officeData;
        this.productType = productType;
        this.loanProductData = loanProductData;
        this.savingProductData = savingsProductData;
        this.charge = charge;
        this.event = event;
        this.eventAttribute = eventAttribute;
        this.accountingRule = accountingRule;
        this.allowedProductTypes = allowedProductTypes;
        this.allowedOffices = allowedOffices;
        this.allowedSavingProducts = allowedSavingProducts;
        this.allowedLoanProducts = allowedLoanProducts;
        this.allowedEventAttributes = allowedEventAttributes;
    }

    public void setAllowedProductTypes(List<EnumOptionData> allowedProductTypes) {
        this.allowedProductTypes = allowedProductTypes;
    }

    public void setAllowedOffices(List<OfficeData> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }

    public void setAllowedSavingProducts(List<SavingsProductData> allowedSavingProducts) {
        this.allowedSavingProducts = allowedSavingProducts;
    }

    public void setAllowedLoanProducts(List<LoanProductData> allowedLoanProducts) {
        this.allowedLoanProducts = allowedLoanProducts;
    }

    public void setAllowedEventAttributes(List<CodeValue> allowedEventAttributes) {
        this.allowedEventAttributes = allowedEventAttributes;
    }
}