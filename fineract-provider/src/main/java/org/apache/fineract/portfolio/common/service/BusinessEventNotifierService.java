/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.common.service;

import java.util.Map;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;

/**
 * Implemented class is responsible for notifying the business event to registered listeners.
 *
 */
public interface BusinessEventNotifierService {

    /**
     * Method should be called to notify listeners before Business event execution for any pre-processing of event
     */
    void notifyBusinessEventToBeExecuted(BusinessEvents businessEvent, Map<BusinessEntity, Object> businessEventEntity);

    /**
     * Method should be called to notify listeners after Business event execution for any post-processing of event
     */
    void notifyBusinessEventWasExecuted(BusinessEvents businessEvent, Map<BusinessEntity, Object> businessEventEntity);

    /**
     * Method is to register a class as listener for pre-processing of any Business event
     */
    void addBusinessEventPreListeners(BusinessEvents businessEvent, BusinessEventListener businessEventListener);

    /**
     * Method is to register a class as listener for post-processing of any Business event
     */
    void addBusinessEventPostListeners(BusinessEvents businessEvent, BusinessEventListener businessEventListener);

}
