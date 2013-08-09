/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long>, JpaSpecificationExecutor<SavingsAccount> {

    @Query("from SavingsAccount s_acc where s_acc.client.id = :clientId")
    List<SavingsAccount> findSavingAccountByClientId(@Param("clientId") Long clientId);

    @Query("from SavingsAccount sa where sa.client.id = :clientId and sa.group.id = :groupId")
    List<SavingsAccount> findByClientIdAndGroupId(@Param("clientId") Long clientId, @Param("groupId") Long groupId);
}