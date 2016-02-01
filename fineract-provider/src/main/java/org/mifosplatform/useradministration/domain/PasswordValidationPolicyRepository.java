/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PasswordValidationPolicyRepository extends JpaRepository<PasswordValidationPolicy, Long>,
        JpaSpecificationExecutor<PasswordValidationPolicy> {

    @Query("from PasswordValidationPolicy PVP WHERE PVP.active = 1")
    public PasswordValidationPolicy findActivePasswordValidationPolicy();



}
