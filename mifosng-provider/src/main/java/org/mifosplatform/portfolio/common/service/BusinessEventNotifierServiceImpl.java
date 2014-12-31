/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.mifosplatform.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.springframework.stereotype.Service;

@Service
public class BusinessEventNotifierServiceImpl implements BusinessEventNotifierService {

    private final Map<BUSINESS_EVENTS, List<BusinessEventListner>> preListners = new HashMap<>(5);
    private final Map<BUSINESS_EVENTS, List<BusinessEventListner>> postListners = new HashMap<>(5);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mifosplatform.portfolio.common.service.BusinessEventNotifierService
     * #notifyBusinessEventToBeExecuted
     * (org.mifosplatform.portfolio.common.BusinessEventNotificationConstants
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
     * org.mifosplatform.portfolio.common.service.BusinessEventNotifierService
     * #notifyBusinessEventWasExecuted
     * (org.mifosplatform.portfolio.common.BusinessEventNotificationConstants
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
     * org.mifosplatform.portfolio.common.service.BusinessEventNotifierService
     * #addBusinessEventPreListners
     * (org.mifosplatform.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.mifosplatform.portfolio.common.service.BusinessEventListner)
     */
    @Override
    public void addBusinessEventPreListners(BUSINESS_EVENTS businessEvent, BusinessEventListner businessEventListner) {
        addBusinessEventListners(businessEvent, businessEventListner, preListners);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mifosplatform.portfolio.common.service.BusinessEventNotifierService
     * #addBusinessEventPostListners
     * (org.mifosplatform.portfolio.common.BusinessEventNotificationConstants
     * .BUSINESS_EVENTS,
     * org.mifosplatform.portfolio.common.service.BusinessEventListner)
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
