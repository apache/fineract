/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountNumberFormatRepository extends JpaRepository<AccountNumberFormat, Long>,
        JpaSpecificationExecutor<AccountNumberFormat> {

    public static final String FIND_ACCOUNT_NUMBER_FORMAT_FOR_ENTITY = "from  AccountNumberFormat anf where anf.accountTypeEnum = :accountTypeEnum";

    @Query(FIND_ACCOUNT_NUMBER_FORMAT_FOR_ENTITY)
    AccountNumberFormat findOneByAccountTypeEnum(@Param("accountTypeEnum") Integer accountTypeEnum);
}
