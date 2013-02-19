/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.serialization.CalculateSavingScheduleQueryFromApiJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalculateSavingScheduleImpl implements CalculateSavingSchedule {

    private final PlatformSecurityContext context;
    private final CalculateSavingScheduleQueryFromApiJsonHelper fromApiJsonDeserializer;
    private final SavingScheduleAssembler savingScheduleAssembler;

    @Autowired
    public CalculateSavingScheduleImpl(final PlatformSecurityContext context, 
            final CalculateSavingScheduleQueryFromApiJsonHelper fromApiJsonDeserializer,
            final SavingScheduleAssembler savingScheduleAssembler) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.savingScheduleAssembler = savingScheduleAssembler;
    }

    @Override
    public SavingScheduleData calculateSavingSchedule(final JsonQuery query) {
    	
        context.authenticatedUser();
        
        this.fromApiJsonDeserializer.validate(query.json());
        
        return this.savingScheduleAssembler.fromJson(query.parsedJson());
    
    }

}
