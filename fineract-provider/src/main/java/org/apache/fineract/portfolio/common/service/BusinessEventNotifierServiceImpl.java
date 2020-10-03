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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.springframework.stereotype.Service;

@Service
public class BusinessEventNotifierServiceImpl implements BusinessEventNotifierService {

    private final Map<BusinessEvents, List<BusinessEventListener>> preListeners = new HashMap<>(5);
    private final Map<BusinessEvents, List<BusinessEventListener>> postListeners = new HashMap<>(5);

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.common.service.BusinessEventNotifierService #notifyBusinessEventToBeExecuted
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants .BusinessEvents,
     * org.springframework.data.jpa.domain.AbstractPersistable)
     */
    @Override
    public void notifyBusinessEventToBeExecuted(BusinessEvents businessEvent, Map<BusinessEntity, Object> businessEventEntity) {
        List<BusinessEventListener> businessEventListeners = this.preListeners.get(businessEvent);
        if (businessEventListeners != null) {
            for (BusinessEventListener eventListener : businessEventListeners) {
                eventListener.businessEventToBeExecuted(businessEventEntity);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.common.service.BusinessEventNotifierService #notifyBusinessEventWasExecuted
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants .BusinessEvents,
     * org.springframework.data.jpa.domain.AbstractPersistable)
     */
    @Override
    public void notifyBusinessEventWasExecuted(BusinessEvents businessEvent, Map<BusinessEntity, Object> businessEventEntity) {
        List<BusinessEventListener> businessEventListeners = this.postListeners.get(businessEvent);
        if (businessEventListeners != null) {
            for (BusinessEventListener eventListener : businessEventListeners) {
                eventListener.businessEventWasExecuted(businessEventEntity);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.common.service.BusinessEventNotifierService #addBusinessEventPreListeners
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants .BusinessEvents,
     * org.apache.fineract.portfolio.common.service.BusinessEventListener)
     */
    @Override
    public void addBusinessEventPreListeners(BusinessEvents businessEvent, BusinessEventListener businessEventListener) {
        addBusinessEventListeners(businessEvent, businessEventListener, preListeners);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.fineract.portfolio.common.service.BusinessEventNotifierService #addBusinessEventPostListeners
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants .BusinessEvents,
     * org.apache.fineract.portfolio.common.service.BusinessEventListener)
     */
    @Override
    public void addBusinessEventPostListeners(BusinessEvents businessEvent, BusinessEventListener businessEventListener) {
        addBusinessEventListeners(businessEvent, businessEventListener, postListeners);
    }

    private void addBusinessEventListeners(BusinessEvents businessEvent, BusinessEventListener businessEventListener,
            final Map<BusinessEvents, List<BusinessEventListener>> businessEventListenerMap) {
        List<BusinessEventListener> businessEventListeners = businessEventListenerMap.get(businessEvent);
        if (businessEventListeners == null) {
            businessEventListeners = new ArrayList<>();
            businessEventListenerMap.put(businessEvent, businessEventListeners);
        }
        businessEventListeners.add(businessEventListener);
    }

}
