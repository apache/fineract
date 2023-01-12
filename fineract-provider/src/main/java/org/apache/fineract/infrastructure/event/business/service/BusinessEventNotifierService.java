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
package org.apache.fineract.infrastructure.event.business.service;

import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;

/**
 * Implemented class is responsible for notifying the business event to registered listeners.
 */
public interface BusinessEventNotifierService {

    /**
     * Method should be called to notify listeners after Business event execution for any pre-processing of event
     */
    void notifyPreBusinessEvent(BusinessEvent<?> businessEvent);

    /**
     * Method should be called to notify listeners after Business event execution for any post-processing of event
     */
    void notifyPostBusinessEvent(BusinessEvent<?> businessEvent);

    /**
     * Method is to register a class as listener for pre-processing of any Business event
     */
    <T extends BusinessEvent<?>> void addPreBusinessEventListener(Class<T> eventType, BusinessEventListener<T> listener);

    /**
     * Method is to register a class as listener for post-processing of any Business event
     */
    <T extends BusinessEvent<?>> void addPostBusinessEventListener(Class<T> eventType, BusinessEventListener<T> listener);

    void startExternalEventRecording();

    void stopExternalEventRecording();

    void resetEventRecording();

}
