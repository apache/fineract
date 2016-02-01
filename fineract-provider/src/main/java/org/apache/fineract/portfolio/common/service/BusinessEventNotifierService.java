/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.service;

import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import java.util.Map;

/**
 * Implemented class is responsible for notifying the business event to
 * registered listeners.
 * 
 */
public interface BusinessEventNotifierService {

    /**
     * Method should be called to notify listeners before Business event
     * execution for any pre-processing of event
     */
    public void notifyBusinessEventToBeExecuted(BUSINESS_EVENTS businessEvent, Map<BUSINESS_ENTITY, Object> businessEventEntity);

    /**
     * Method should be called to notify listeners after Business event
     * execution for any post-processing of event
     */
    public void notifyBusinessEventWasExecuted(BUSINESS_EVENTS businessEvent, Map<BUSINESS_ENTITY, Object> businessEventEntity);

    /**
     * Method is to register a class as listener for pre-processing of any
     * Business event
     */
    public void addBusinessEventPreListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner);

    /**
     * Method is to register a class as listener for post-processing of any
     * Business event
     */
    public void addBusinessEventPostListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner);

}
