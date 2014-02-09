/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "m_appuser_previews_passwords")
public class AppUserPreviewPassword extends AbstractPersistable<Long>  {

    private final static Logger logger = LoggerFactory.getLogger(AppUserPreviewPassword.class);

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "removal_date")
    @Temporal(TemporalType.DATE)
    private Date removalDate;

    @Column(name = "password", nullable = false)
    private String password;


    protected AppUserPreviewPassword() {

    }

    public AppUserPreviewPassword(final AppUser user) {

       this.userId = user.getId();
       this.password = user.getPassword().trim();
       this.removalDate = DateUtils.getDateOfTenant();
    }

    public String getPassword()
    {
        return this.password;
    }



}