/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long>, JpaSpecificationExecutor<LoanProduct> {

    @Query("select loanProduct from LoanProduct loanProduct, IN(loanProduct.charges) charge where charge.id = :chargeId")
    List<LoanProduct> retrieveLoanProductsByChargeId(@Param("chargeId") Long chargeId);
}