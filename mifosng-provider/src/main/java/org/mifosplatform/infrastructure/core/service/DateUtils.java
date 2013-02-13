/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;

public class DateUtils {

    public static LocalDate getLocalDateOfTenant() {

        LocalDate today = new LocalDate();

        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        if (tenant != null) {
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            if (zone != null) {
                today = new LocalDate(zone);
            }
        }

        return today;
    }

}