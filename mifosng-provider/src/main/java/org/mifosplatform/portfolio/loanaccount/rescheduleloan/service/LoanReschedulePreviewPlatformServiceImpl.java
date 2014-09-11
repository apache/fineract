/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.rescheduleloan.service;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.holiday.domain.HolidayRepository;
import org.mifosplatform.organisation.holiday.domain.HolidayStatusType;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.DefaultLoanReschedulerFactory;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleModel;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.mifosplatform.portfolio.loanaccount.rescheduleloan.exception.LoanRescheduleRequestNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanReschedulePreviewPlatformServiceImpl implements LoanReschedulePreviewPlatformService {
	
	private final LoanRescheduleRequestRepository loanRescheduleRequestRepository;
	private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
	private final ConfigurationDomainService configurationDomainService;
	private final HolidayRepository holidayRepository;
	private final WorkingDaysRepositoryWrapper workingDaysRepository;
	
	@Autowired
	public LoanReschedulePreviewPlatformServiceImpl(final LoanRescheduleRequestRepository loanRescheduleRequestRepository, 
			final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository, 
			final ConfigurationDomainService configurationDomainService, 
			final HolidayRepository holidayRepository, 
			final WorkingDaysRepositoryWrapper workingDaysRepository) {
		this.loanRescheduleRequestRepository = loanRescheduleRequestRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.configurationDomainService = configurationDomainService;
		this.holidayRepository = holidayRepository;
		this.workingDaysRepository = workingDaysRepository;
	}

	@Override
	public LoanRescheduleModel previewLoanReschedule(Long requestId) {
		final LoanRescheduleRequest loanRescheduleRequest = this.loanRescheduleRequestRepository.findOne(requestId);
		
		if(loanRescheduleRequest == null) {
			throw new LoanRescheduleRequestNotFoundException(requestId);
		}
		
		Loan loan = loanRescheduleRequest.getLoan();
		
		final boolean isHolidayEnabled = this.configurationDomainService.isRescheduleRepaymentsOnHolidaysEnabled();
        final List<Holiday> holidays = this.holidayRepository.findByOfficeIdAndGreaterThanDate(loan.getOfficeId(),
                loan.getDisbursementDate().toDate(), HolidayStatusType.ACTIVE.getValue());
        final WorkingDays workingDays = this.workingDaysRepository.findOne();
        final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
		final MonetaryCurrency currency = loanProductRelatedDetail.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        
        final InterestMethod interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod();
        final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
        final MathContext mathContext = new MathContext(8, roundingMode);
        
        LoanRescheduleModel loanRescheduleModel = new DefaultLoanReschedulerFactory().reschedule(mathContext, interestMethod, 
        		loanRescheduleRequest, applicationCurrency, isHolidayEnabled, holidays, workingDays);
		
		return loanRescheduleModel;
	}

}
