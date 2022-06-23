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
package org.apache.fineract.portfolio.businessevent.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.portfolio.businessevent.BusinessEventListener;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({ "unchecked", "rawtypes" })
public class BusinessEventNotifierServiceImpl implements BusinessEventNotifierService {

    private final Map<Class, List<BusinessEventListener>> listeners = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService
     * #notifyBusinessEventWasExecuted (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BusinessEvents, org.springframework.data.jpa.domain.AbstractPersistable)
     */
    @Override
    public void notifyBusinessEvent(BusinessEvent<?> businessEvent) {
        List<BusinessEventListener> businessEventListeners = listeners.get(businessEvent.getClass());
        if (businessEventListeners != null) {
            for (BusinessEventListener eventListener : businessEventListeners) {
                eventListener.onBusinessEvent(businessEvent);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService
     * #addBusinessEventPostListeners (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BusinessEvents, org.apache.fineract.portfolio.businessevent.BusinessEventListener)
     */
    @Override
    public <T extends BusinessEvent<?>> void addBusinessEventListener(Class<T> eventType, BusinessEventListener<T> listener) {
        List<BusinessEventListener> businessEventListeners = listeners.get(eventType);
        if (businessEventListeners == null) {
            businessEventListeners = new ArrayList<>();
            listeners.put(eventType, businessEventListeners);
        }
        businessEventListeners.add(listener);
    }

}
