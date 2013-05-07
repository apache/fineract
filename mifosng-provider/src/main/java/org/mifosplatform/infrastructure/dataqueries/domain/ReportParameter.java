/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "stretchy_parameter")
public class ReportParameter extends AbstractPersistable<Long> {

    protected ReportParameter() {
        //
    }

    public boolean hasIdOf(final Long id) {
        return getId().equals(id);
    }
}