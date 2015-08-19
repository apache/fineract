/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.domain;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoneyHelper {
    
    private static int roundingModeIntValue = -1;
    private static ConfigurationDomainService staticConfigurationDomainService;
    
    @Autowired
    private ConfigurationDomainService configurationDomainService;

    @PostConstruct
    public void someFunction () {
        staticConfigurationDomainService = configurationDomainService;
    }

    
    public static int getRoundingMode() {
        if (roundingModeIntValue == -1) {
            roundingModeIntValue = staticConfigurationDomainService.getRoundingMode();
        }
        return roundingModeIntValue;
    }

}
