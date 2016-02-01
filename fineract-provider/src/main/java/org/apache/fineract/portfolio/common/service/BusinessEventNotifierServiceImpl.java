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

import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.springframework.stereotype.Service;

@Service
public class BusinessEventNotifierServiceImpl implements BusinessEventNotifierService {

    private final Map<BUSINESS_EVENTS, List<BusinessEventListner>> preListners = new HashMap<>(5);
    private final Map<BUSINESS_EVENTS, List<BusinessEventListner>> postListners = new HashMap<>(5);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.fineract.portfolio.common.service.BusinessEventNotifierService
     * #notifyBusinessEventToBeExecuted
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.springframework.data.jpa.domain.AbstractPersistable)
     */
    @Override
    public void notifyBusinessEventToBeExecuted(BUSINESS_EVENTS businessEvent, Map<BUSINESS_ENTITY, Object> businessEventEntity) {
        List<BusinessEventListner> businessEventListners = this.preListners.get(businessEvent);
        if (businessEventListners != null) {
            for (BusinessEventListner eventListner : businessEventListners) {
                eventListner.businessEventToBeExecuted(businessEventEntity);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.fineract.portfolio.common.service.BusinessEventNotifierService
     * #notifyBusinessEventWasExecuted
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.springframework.data.jpa.domain.AbstractPersistable)
     */
    @Override
    public void notifyBusinessEventWasExecuted(BUSINESS_EVENTS businessEvent, Map<BUSINESS_ENTITY, Object> businessEventEntity) {
        List<BusinessEventListner> businessEventListners = this.postListners.get(businessEvent);
        if (businessEventListners != null) {
            for (BusinessEventListner eventListner : businessEventListners) {
                eventListner.businessEventWasExecuted(businessEventEntity);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.fineract.portfolio.common.service.BusinessEventNotifierService
     * #addBusinessEventPreListners
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.apache.fineract.portfolio.common.service.BusinessEventListner)
     */
    @Override
    public void addBusinessEventPreListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner) {
        addBusinessEventListners(businessEvent, businessEventListner, preListners);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.fineract.portfolio.common.service.BusinessEventNotifierService
     * #addBusinessEventPostListners
     * (org.apache.fineract.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.apache.fineract.portfolio.common.service.BusinessEventListner)
     */
    @Override
    public void addBusinessEventPostListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner) {
        addBusinessEventListners(businessEvent, businessEventListner, postListners);
    }

    private void addBusinessEventListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner,
            final Map<BUSINESS_EVENTS, List<BusinessEventListner>> businessEventListnerMap) {
        List<BusinessEventListner> businessEventListners = businessEventListnerMap.get(businessEvent);
        if (businessEventListners == null) {
            businessEventListners = new ArrayList<>();
            businessEventListnerMap.put(businessEvent, businessEventListners);
        }
        businessEventListners.add(businessEventListner);
    }

}
