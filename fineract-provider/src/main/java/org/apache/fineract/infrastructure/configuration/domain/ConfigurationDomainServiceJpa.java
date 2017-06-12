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
package org.apache.fineract.infrastructure.configuration.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.cache.domain.PlatformCache;
import org.apache.fineract.infrastructure.cache.domain.PlatformCacheRepository;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.exception.PermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationDomainServiceJpa implements ConfigurationDomainService {

    private final PermissionRepository permissionRepository;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final PlatformCacheRepository cacheTypeRepository;
    private static Map<String, GlobalConfigurationPropertyData> configurations = new HashMap<>();

    @Autowired
    public ConfigurationDomainServiceJpa(final PermissionRepository permissionRepository,
            final GlobalConfigurationRepositoryWrapper globalConfigurationRepository, final PlatformCacheRepository cacheTypeRepository) {
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
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(makerCheckerConfigurationProperty);

        return thisTask.hasMakerCheckerEnabled() && property.isEnabled();
    }

    @Override
    public boolean isAmazonS3Enabled() {
        return getGlobalConfigurationPropertyData("amazon-S3").isEnabled();
    }

    @Override
    public boolean isRescheduleFutureRepaymentsEnabled() {
        final String rescheduleRepaymentsConfigurationProperty = "reschedule-future-repayments";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(rescheduleRepaymentsConfigurationProperty);
        return property.isEnabled();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.fineract.infrastructure.configuration.domain.
     * ConfigurationDomainService#isHolidaysEnabled()
     */
    @Override
    public boolean isRescheduleRepaymentsOnHolidaysEnabled() {
        final String holidaysConfigurationProperty = "reschedule-repayments-on-holidays";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(holidaysConfigurationProperty);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnHolidayEnabled() {
        final String allowTransactionsOnHolidayProperty = "allow-transactions-on-holiday";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(allowTransactionsOnHolidayProperty);
        return property.isEnabled();
    }

    @Override
    public boolean allowTransactionsOnNonWorkingDayEnabled() {
        final String propertyName = "allow-transactions-on-non_workingday";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isConstraintApproachEnabledForDatatables() {
        final String propertyName = "constraint_approach_for_datatables";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
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
        final String propertyName = "penalty-wait-period";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public Long retrieveGraceOnPenaltyPostingPeriod() {
        final String propertyName = "grace-on-penalty-posting";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public boolean isPasswordForcedResetEnable() {
        final String propertyName = "force-password-reset-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Long retrievePasswordLiveTime() {
        final String propertyName = "force-password-reset-days";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public Long retrieveOpeningBalancesContraAccount() {
        final String propertyName = "office-opening-balances-contra-account";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
    }

    @Override
    public boolean isSavingsInterestPostingAtCurrentPeriodEnd() {
        final String propertyName = "savings-interest-posting-current-period-end";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Integer retrieveFinancialYearBeginningMonth() {
        final String propertyName = "financial-year-beginning-month";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) return property.getValue().intValue();
        return 1;
    }

    @Override
    public Integer retrieveMinAllowedClientsInGroup() {
        final String propertyName = "min-clients-in-group";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) { return property.getValue().intValue(); }
        return null;
    }

    @Override
    public Integer retrieveMaxAllowedClientsInGroup() {
        final String propertyName = "max-clients-in-group";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) { return property.getValue().intValue(); }
        return null;
    }

    @Override
    public boolean isMeetingMandatoryForJLGLoans() {
        final String propertyName = "meetings-mandatory-for-jlg-loans";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public int getRoundingMode() {
        final String propertyName = "rounding-mode";
        int defaultValue = 6; // 6 Stands for HALF-EVEN
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        if (property.isEnabled()) {
            int value = property.getValue().intValue();
            if (value < 0 || value > 6) {
                return defaultValue;
            }
            return value;
        }
        return defaultValue;
    }

    @Override
    public boolean isBackdatePenaltiesEnabled() {
        final String propertyName = "backdate-penalties-enabled";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isOrganisationstartDateEnabled() {
        final String propertyName = "organisation-start-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Date retrieveOrganisationStartDate() {
        final String propertyName = "organisation-start-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getDateValue();
    }

	@Override
	public boolean isPaymnetypeApplicableforDisbursementCharge() {
		final String propertyName = "paymenttype-applicable-for-disbursement-charges";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
	}
	
    @Override
    public boolean isSkippingMeetingOnFirstDayOfMonthEnabled() {
        return getGlobalConfigurationPropertyData("skip-repayment-on-first-day-of-month").isEnabled();
    }

    @Override
    public Long retreivePeroidInNumberOfDaysForSkipMeetingDate() {
        final String propertyName = "skip-repayment-on-first-day-of-month";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();

    }

    @Override
    public boolean isInterestChargedFromDateSameAsDisbursementDate() {
        final String propertyName = "interest-charged-from-date-same-as-disbursal-date";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }
    
    @Override
    public boolean isChangeEmiIfRepaymentDateSameAsDisbursementDateEnabled() {
        final String propertyName = "change-emi-if-repaymentdate-same-as-disbursementdate";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

	@Override
	public boolean isDailyTPTLimitEnabled() {
        final String propertyName = "daily-tpt-limit";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
	}

	@Override
	public Long getDailyTPTLimit() {
        final String propertyName = "daily-tpt-limit";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.getValue();
	}

    @Override
    public void removeGlobalConfigurationPropertyDataFromCache(final String propertyName) {
        String identifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        String key = identifier + "_" + propertyName;
        configurations.remove(key);
    }

    @Override
    public boolean isSMSOTPDeliveryEnabled() {
        final String propertyName = "use-sms-for-2fa";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public boolean isEmailOTPDeliveryEnabled() {
        final String propertyName = "use-email-for-2fa";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        return property.isEnabled();
    }

    @Override
    public Integer retrieveOTPCharacterLength() {
        final String propertyName = "otp-character-length";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        int defaultValue = 6;
        int value = property.getValue().intValue();
        if(value < 1)
            return defaultValue;
        return value;
    }

    @Override
    public Integer retrieveOTPLiveTime() {
        final String propertyName = "otp-validity-period";
        final GlobalConfigurationPropertyData property = getGlobalConfigurationPropertyData(propertyName);
        int defaultValue = 300;
        int value = property.getValue().intValue();
        if(value < 1) {
            return defaultValue;
        }
        return value;
    }

    private GlobalConfigurationPropertyData getGlobalConfigurationPropertyData(final String propertyName) {
        String identifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        String key = identifier + "_" + propertyName;
        if (!configurations.containsKey(key)) {
            GlobalConfigurationProperty configuration = this.globalConfigurationRepository.findOneByNameWithNotFoundDetection(propertyName);
            configurations.put(key, configuration.toData());
        }
        return configurations.get(key);
    }
}