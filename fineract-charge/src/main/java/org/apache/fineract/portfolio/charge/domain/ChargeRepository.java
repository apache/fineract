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

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRepository extends JpaRepository<Charge, Long>, JpaSpecificationExecutor<Charge> {

    @Query("select lc.id from WorkingCapitalLoanCharge lc where lc.charge.id = :chargeId and lc.active = true")
    Optional<Long> isAnyWorkingCapitalLoansAssociateWithThisCharge(@Param("chargeId") Long chargeId);

    @Query("select case when count(lc) > 0 then true else false end from LoanCharge lc where lc.charge.id = :chargeId and lc.active = true")
    boolean isAnyActiveLoanChargeAssociatedWithCharge(@Param("chargeId") Long chargeId);

    @Query("select case when count(sc) > 0 then true else false end from SavingsAccountCharge sc where sc.charge.id = :chargeId and sc.status = true")
    boolean isAnyActiveSavingsAccountChargeAssociatedWithCharge(@Param("chargeId") Long chargeId);

    @Query("select case when count(lp) > 0 then true else false end from LoanProduct lp join lp.charges c where c.id = :chargeId")
    boolean isAnyLoanProductAssociatedWithCharge(@Param("chargeId") Long chargeId);

    @Query("select case when count(sp) > 0 then true else false end from SavingsProduct sp join sp.charges c where c.id = :chargeId")
    boolean isAnySavingsProductAssociatedWithCharge(@Param("chargeId") Long chargeId);
}
