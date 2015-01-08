/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.domain;

import org.mifosplatform.infrastructure.cache.domain.CacheType;

public interface ConfigurationDomainService {

    boolean isMakerCheckerEnabledForTask(String taskPermissionCode);

    boolean isAmazonS3Enabled();

    boolean isRescheduleFutureRepaymentsEnabled();

    boolean isRescheduleRepaymentsOnHolidaysEnabled();

    boolean allowTransactionsOnHolidayEnabled();

    boolean allowTransactionsOnNonWorkingDayEnabled();

    boolean isConstraintApproachEnabledForDatatables();

    boolean isEhcacheEnabled();

    void updateCache(CacheType cacheType);

    Long retrievePenaltyWaitPeriod();

    boolean isPasswordForcedResetEnable();

    Long retrievePasswordLiveTime();

    Long retrieveGraceOnPenaltyPostingPeriod();

    Long retrieveOpeningBalancesContraAccount();

    boolean isSavingsInterestPostingAtCurrentPeriodEnd();

    Integer retrieveFinancialYearBeginningMonth();

    public Integer retrieveMinAllowedClientsInGroup();

    public Integer retrieveMaxAllowedClientsInGroup();

    boolean isMeetingMandatoryForJLGLoans();

}