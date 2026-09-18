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
package org.apache.fineract.portfolio.charge.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRepository extends JpaRepository<Charge, Long>, JpaSpecificationExecutor<Charge> {

    String WC_APPLICABLE_CHARGES = "select c from Charge c where c.deleted = false and c.active = true"
            + " and c.penalty = :penalty and c.chargeAppliesTo = :chargeAppliesTo";

    String WC_APPLICABLE_CHARGES_FOR_CURRENCY = WC_APPLICABLE_CHARGES + " and c.currencyCode = :currencyCode";

    String WC_PRODUCT_CHARGES = "select c from WorkingCapitalLoanProduct p join p.charges c where p.id = :productId"
            + " and c.deleted = false and c.active = true";

    String WC_LOAN_ACCOUNT_APPLICABLE_CHARGES = "select c from WorkingCapitalLoan l join l.loanProduct p join p.charges c"
            + " where l.id = :loanId and c.deleted = false and c.active = true"
            + " and c.chargeAppliesTo = :chargeAppliesTo and c.chargeTimeType in :chargeTimeTypes"
            + " and c.currencyCode = l.loanProductRelatedDetails.currency.code";

    @Query("select lc.id from WorkingCapitalLoanCharge lc where lc.charge.id = :chargeId and lc.active = true")
    Optional<Long> isAnyWorkingCapitalLoansAssociateWithThisCharge(@Param("chargeId") Long chargeId);

    /**
     * Ids of the working capital loan products offering the charge. A charge can be offered by several products, so
     * this returns every match rather than a single optional id.
     */
    @Query("select p.id from WorkingCapitalLoanProduct p join p.charges c where c.id = :chargeId")
    List<Long> findWorkingCapitalLoanProductIdsAssociatedWithCharge(@Param("chargeId") Long chargeId);

    @Query(WC_APPLICABLE_CHARGES + " order by c.name")
    List<Charge> findWorkingCapitalLoanApplicableCharges(@Param("penalty") boolean penalty,
            @Param("chargeAppliesTo") Integer chargeAppliesTo);

    /**
     * Same as {@link #findWorkingCapitalLoanApplicableCharges}, narrowed to the charges the user's office may see when
     * office specific products are enabled.
     */
    @Query(WC_APPLICABLE_CHARGES + " and c.id in :chargeIds order by c.name")
    List<Charge> findWorkingCapitalLoanApplicableChargesForOffice(@Param("penalty") boolean penalty,
            @Param("chargeAppliesTo") Integer chargeAppliesTo, @Param("chargeIds") Collection<Long> chargeIds);

    /**
     * Same as {@link #findWorkingCapitalLoanApplicableCharges}, narrowed to a single currency. Used by the loan
     * application template, where the offered charges must match the currency of the selected product.
     */
    @Query(WC_APPLICABLE_CHARGES_FOR_CURRENCY + " order by c.name")
    List<Charge> findWorkingCapitalLoanApplicableChargesForCurrency(@Param("penalty") boolean penalty,
            @Param("chargeAppliesTo") Integer chargeAppliesTo, @Param("currencyCode") String currencyCode);

    /**
     * Same as {@link #findWorkingCapitalLoanApplicableChargesForCurrency}, narrowed to the charges the user's office
     * may see when office specific products are enabled.
     */
    @Query(WC_APPLICABLE_CHARGES_FOR_CURRENCY + " and c.id in :chargeIds order by c.name")
    List<Charge> findWorkingCapitalLoanApplicableChargesForCurrencyAndOffice(@Param("penalty") boolean penalty,
            @Param("chargeAppliesTo") Integer chargeAppliesTo, @Param("currencyCode") String currencyCode,
            @Param("chargeIds") Collection<Long> chargeIds);

    @Query(WC_PRODUCT_CHARGES)
    List<Charge> findWorkingCapitalLoanProductCharges(@Param("productId") Long productId);

    /**
     * Same as {@link #findWorkingCapitalLoanProductCharges}, narrowed to the charges the user's office may see when
     * office specific products are enabled.
     */
    @Query(WC_PRODUCT_CHARGES + " and c.id in :chargeIds")
    List<Charge> findWorkingCapitalLoanProductChargesForOffice(@Param("productId") Long productId,
            @Param("chargeIds") Collection<Long> chargeIds);

    /**
     * Charges the loan's own product catalogues, in the loan currency. Only what the product sells is offered, so the
     * account charge template cannot suggest a charge the product does not carry. This is deliberately stricter than
     * {@link #findWorkingCapitalLoanApplicableChargesForCurrency}, which the loan application template uses to offer
     * the whole Working Capital catalogue for the product currency.
     */
    @Query(WC_LOAN_ACCOUNT_APPLICABLE_CHARGES + " order by c.name")
    List<Charge> findWorkingCapitalLoanAccountApplicableCharges(@Param("loanId") Long loanId,
            @Param("chargeAppliesTo") Integer chargeAppliesTo, @Param("chargeTimeTypes") Collection<Integer> chargeTimeTypes);

    /**
     * Same as {@link #findWorkingCapitalLoanAccountApplicableCharges}, narrowed to the charges the user's office may
     * see when office specific products are enabled.
     */
    @Query(WC_LOAN_ACCOUNT_APPLICABLE_CHARGES + " and c.id in :chargeIds order by c.name")
    List<Charge> findWorkingCapitalLoanAccountApplicableChargesForOffice(@Param("loanId") Long loanId,
            @Param("chargeAppliesTo") Integer chargeAppliesTo, @Param("chargeTimeTypes") Collection<Integer> chargeTimeTypes,
            @Param("chargeIds") Collection<Long> chargeIds);
}
