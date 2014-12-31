/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.service;

import java.util.Map;

import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;

/**
 * The interface to be implemented by classes that want to be informed when a
 * Business Event executes. example: on completion of loan approval event need
 * to block guarantor funds
 * 
 */
public interface BusinessEventListner {

    /**
     * Implement this method for notifications before executing Business Event
     */
    public void businessEventToBeExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity);

    /**
     * Implement this method for notifications after executing Business Event
     */
    public void businessEventWasExecuted(Map<BUSINESS_ENTITY, Object> businessEventEntity);

}
