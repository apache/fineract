/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_appuser_previous_password")
public class AppUserPreviousPassword extends AbstractPersistable<Long> {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "removal_date")
    @Temporal(TemporalType.DATE)
    private Date removalDate;

    @Column(name = "password", nullable = false)
    private String password;

    protected AppUserPreviousPassword() {

    }

    public AppUserPreviousPassword(final AppUser user) {
        this.userId = user.getId();
        this.password = user.getPassword().trim();
        this.removalDate = DateUtils.getDateOfTenant();
    }

    public String getPassword() {
        return this.password;
    }

}