/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.service;

import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.springframework.data.jpa.domain.AbstractPersistable;

public interface BusinessEventNotifierService {

    public void notifyBusinessEventToBeExecuted(BUSINESS_EVENTS businessEvent, AbstractPersistable<Long> businessEventEntity);

    public void notifyBusinessEventWasExecuted(BUSINESS_EVENTS businessEvent, AbstractPersistable<Long> businessEventEntity);

    public void addBusinessEventPreListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner);

    public void addBusinessEventPostListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner);

}
