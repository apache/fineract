/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationDomainServiceJpa implements ConfigurationDomainService {

    private final PermissionRepository permissionRepository;
    private final GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    public ConfigurationDomainServiceJpa(final PermissionRepository permissionRepository,
            final GlobalConfigurationRepository globalConfigurationRepository) {
        this.permissionRepository = permissionRepository;
        this.globalConfigurationRepository = globalConfigurationRepository;
    }

    @Override
    public boolean isMakerCheckerEnabledForTask(final String taskPermissionCode) {
        if (StringUtils.isBlank(taskPermissionCode)) { throw new PermissionNotFoundException(taskPermissionCode); }

        final Permission thisTask = this.permissionRepository.findOneByCode(taskPermissionCode);
        if (thisTask == null) { throw new PermissionNotFoundException(taskPermissionCode); }

        final String makerCheckerConfigurationProperty = "maker-checker";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(makerCheckerConfigurationProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(makerCheckerConfigurationProperty); }

        return thisTask.hasMakerCheckerEnabled() && property.isEnabled();
    }

    @Override
    public boolean isAmazonS3Enabled(){
        return this.globalConfigurationRepository.findOneByName("amazon-S3").isEnabled();
    }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() {
        final String rescheduleRepaymentsConfigurationProperty = "reschedule-future-repayments";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(rescheduleRepaymentsConfigurationProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(rescheduleRepaymentsConfigurationProperty); }
        return property.isEnabled();
    }

    /* (non-Javadoc)
     * @see org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService#isHolidaysEnabled()
     */
    @Override
    public boolean isRescheduleRepaymentsOnHolidaysEnabled() {
        final String holidaysConfigurationProperty = "reschedule-repayments-on-holidays";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(holidaysConfigurationProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(holidaysConfigurationProperty); }
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnHolidayEnabled() {
        final String allowTransactionsOnHolidayProperty = "allow-transactions-on-holiday";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(allowTransactionsOnHolidayProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(allowTransactionsOnHolidayProperty); }
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnNonWorkingDayEnabled() {
        final String allowTransactionsOnNonWorkingDayProperty = "allow-transactions-on-non_workingday";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(allowTransactionsOnNonWorkingDayProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(allowTransactionsOnNonWorkingDayProperty); }
        return property.isEnabled();
    }

}