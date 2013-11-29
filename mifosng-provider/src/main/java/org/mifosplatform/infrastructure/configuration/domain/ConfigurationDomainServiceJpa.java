/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.cache.domain.CacheType;
import org.mifosplatform.infrastructure.cache.domain.PlatformCache;
import org.mifosplatform.infrastructure.cache.domain.PlatformCacheRepository;
import org.mifosplatform.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationDomainServiceJpa implements ConfigurationDomainService {

    private final PermissionRepository permissionRepository;
    private final GlobalConfigurationRepository globalConfigurationRepository;
    private final PlatformCacheRepository cacheTypeRepository;

    @Autowired
    public ConfigurationDomainServiceJpa(final PermissionRepository permissionRepository,
            final GlobalConfigurationRepository globalConfigurationRepository, final PlatformCacheRepository cacheTypeRepository) {
        this.permissionRepository = permissionRepository;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.cacheTypeRepository = cacheTypeRepository;
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
    public boolean isAmazonS3Enabled() {
        return this.globalConfigurationRepository.findOneByName("amazon-S3").isEnabled();
    }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() {
        final String rescheduleRepaymentsConfigurationProperty = "reschedule-future-repayments";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository
                .findOneByName(rescheduleRepaymentsConfigurationProperty);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(rescheduleRepaymentsConfigurationProperty); }
        return property.isEnabled();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mifosplatform.infrastructure.configuration.domain.
     * ConfigurationDomainService#isHolidaysEnabled()
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
        final String propertyName = "allow-transactions-on-non_workingday";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository
                .findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property.isEnabled();
    }

    @Override
    public boolean isConstraintApproachEnabledForDatatables() {
        final String propertyName = "constraint_approach_for_datatables";
        final GlobalConfigurationProperty property = this.globalConfigurationRepository.findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property.isEnabled();
    }

    @Override
    public boolean isEhcacheEnabled() {
        return this.cacheTypeRepository.findOne(Long.valueOf(1)).isEhcacheEnabled();
    }

    @Transactional
    @Override
    public void updateCache(final CacheType cacheType) {
        final PlatformCache cache = this.cacheTypeRepository.findOne(Long.valueOf(1));
        cache.update(cacheType);

        this.cacheTypeRepository.save(cache);
    }

    @Override
    public Long retrievePenaltyWaitPeriod() {
        final String propertyName  ="penalty-wait-period";
        final GlobalConfigurationProperty property= this.globalConfigurationRepository.findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property.getValue();
    }
}