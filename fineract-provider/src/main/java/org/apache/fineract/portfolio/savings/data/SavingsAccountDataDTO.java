/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.savings.domain.SavingsProduct;
import org.mifosplatform.useradministration.domain.AppUser;

public class SavingsAccountDataDTO {

    private final Client client;
    private final Group group;
    private final SavingsProduct savingsProduct;
    private final LocalDate applicationDate;
    private final AppUser appliedBy;
    private final DateTimeFormatter fmt;

    public SavingsAccountDataDTO(final Client client, final Group group, final SavingsProduct savingsProduct,
            final LocalDate applicationDate, final AppUser appliedBy, final DateTimeFormatter fmt) {
        this.client = client;
        this.group = group;
        this.savingsProduct = savingsProduct;
        this.applicationDate = applicationDate;
        this.appliedBy = appliedBy;
        this.fmt = fmt;
    }

    public Client getClient() {
        return this.client;
    }

    public Group getGroup() {
        return this.group;
    }

    public SavingsProduct getSavingsProduct() {
        return this.savingsProduct;
    }

    public LocalDate getApplicationDate() {
        return this.applicationDate;
    }

    public AppUser getAppliedBy() {
        return this.appliedBy;
    }

    public DateTimeFormatter getFmt() {
        return this.fmt;
    }
}
