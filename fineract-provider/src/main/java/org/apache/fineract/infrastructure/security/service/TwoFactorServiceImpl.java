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
package org.apache.fineract.infrastructure.security.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.apache.fineract.infrastructure.core.service.PlatformEmailService;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConstants;
import org.apache.fineract.infrastructure.security.data.OTPDeliveryMethod;
import org.apache.fineract.infrastructure.security.data.OTPRequest;
import org.apache.fineract.infrastructure.security.domain.OTPRequestRepository;
import org.apache.fineract.infrastructure.security.domain.TFAccessToken;
import org.apache.fineract.infrastructure.security.domain.TFAccessTokenRepository;
import org.apache.fineract.infrastructure.security.exception.AccessTokenInvalidIException;
import org.apache.fineract.infrastructure.security.exception.OTPDeliveryMethodInvalidException;
import org.apache.fineract.infrastructure.security.exception.OTPTokenInvalidException;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("twofactor")
public class TwoFactorServiceImpl implements TwoFactorService {



    private final AccessTokenGenerationService accessTokenGenerationService;
    private final PlatformEmailService emailService;
    private final SmsMessageScheduledJobService smsMessageScheduledJobService;

    private final OTPRequestRepository otpRequestRepository;
    private final TFAccessTokenRepository tfAccessTokenRepository;
    private final SmsMessageRepository smsMessageRepository;

    private final TwoFactorConfigurationService configurationService;

    @Autowired
    public TwoFactorServiceImpl(AccessTokenGenerationService accessTokenGenerationService,
            PlatformEmailService emailService,
            SmsMessageScheduledJobService smsMessageScheduledJobService,
            OTPRequestRepository otpRequestRepository,
            TFAccessTokenRepository tfAccessTokenRepository,
            SmsMessageRepository smsMessageRepository,
            TwoFactorConfigurationService configurationService) {
        this.accessTokenGenerationService = accessTokenGenerationService;
        this.emailService = emailService;
        this.smsMessageScheduledJobService = smsMessageScheduledJobService;
        this.otpRequestRepository = otpRequestRepository;
        this.tfAccessTokenRepository = tfAccessTokenRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.configurationService = configurationService;
    }


    @Override
    public List<OTPDeliveryMethod> getDeliveryMethodsForUser(final AppUser user) {
        List<OTPDeliveryMethod> deliveryMethods = new ArrayList<>();

        OTPDeliveryMethod smsMethod = getSMSDeliveryMethodForUser(user);
        if(smsMethod != null) {
            deliveryMethods.add(smsMethod);
        }
        OTPDeliveryMethod emailDelivery = getEmailDeliveryMethodForUser(user);
        if(emailDelivery != null) {
            deliveryMethods.add(emailDelivery);
        }

        return deliveryMethods;
    }

    @Override
    public OTPRequest createNewOTPToken(final AppUser user, final String deliveryMethodName,
                                        final boolean extendedAccessToken) {
        if(TwoFactorConstants.SMS_DELIVERY_METHOD_NAME.equalsIgnoreCase(deliveryMethodName)) {
            OTPDeliveryMethod smsDelivery = getSMSDeliveryMethodForUser(user);
            if(smsDelivery == null) {
                throw new OTPDeliveryMethodInvalidException();
            }
            final OTPRequest request = generateNewToken(smsDelivery, extendedAccessToken);
            final String smsText = configurationService.getFormattedSmsTextFor(user, request);
            SmsMessage smsMessage = SmsMessage.pendingSms(null, null, null, user.getStaff(), smsText,
                    user.getStaff().mobileNo(), null, false);
            this.smsMessageRepository.save(smsMessage);
            smsMessageScheduledJobService.sendTriggeredMessage(Collections.singleton(smsMessage),
                    configurationService.getSMSProviderId());
            otpRequestRepository.addOTPRequest(user, request);
            return request;
        } else if(TwoFactorConstants.EMAIL_DELIVERY_METHOD_NAME.equalsIgnoreCase(deliveryMethodName)) {
            OTPDeliveryMethod emailDelivery = getEmailDeliveryMethodForUser(user);
            if(emailDelivery == null) {
                throw new OTPDeliveryMethodInvalidException();
            }
            final OTPRequest request = generateNewToken(emailDelivery, extendedAccessToken);
            final String emailSubject = configurationService.getFormattedEmailSubjectFor(user, request);
            final String emailBody = configurationService.getFormattedEmailBodyFor(user, request);
            final EmailDetail emailData = new EmailDetail(emailSubject, emailBody, user.getEmail(),
                    user.getFirstname() + " " + user.getLastname());
            emailService.sendDefinedEmail(emailData);
            otpRequestRepository.addOTPRequest(user, request);
            return request;
        }

        throw new OTPDeliveryMethodInvalidException();
    }

    @Override
    @CachePut(value = "userTFAccessToken",
            key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil)" +
                    ".getTenant().getTenantIdentifier().concat(#user.username).concat(#result.token + 'tok')")
    public TFAccessToken createAccessTokenFromOTP(final AppUser user, final String otpToken) {

        OTPRequest otpRequest = otpRequestRepository.getOTPRequestForUser(user);
        if(otpRequest == null || !otpRequest.isValid() || !otpRequest.getToken().equalsIgnoreCase(otpToken)) {
            throw new OTPTokenInvalidException();
        }

        otpRequestRepository.deleteOTPRequestForUser(user);

        String token = accessTokenGenerationService.generateRandomToken();
        int liveTime;
        if(otpRequest.getMetadata().isExtendedAccessToken()) {
            liveTime = configurationService.getAccessTokenExtendedLiveTime();
        } else {
            liveTime = configurationService.getAccessTokenLiveTime();
        }
        TFAccessToken accessToken = TFAccessToken.create(token, user, liveTime);
        tfAccessTokenRepository.save(accessToken);
        return accessToken;
    }

    @Override
    public void validateTwoFactorAccessToken(AppUser user, String token) {
        TFAccessToken accessToken = fetchAccessTokenForUser(user, token);

        if(accessToken == null || !accessToken.isValid()) {
            throw new AccessTokenInvalidIException();
        }
    }

    @Override
    @CacheEvict(value = "userTFAccessToken",
            key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil)" +
                    ".getTenant().getTenantIdentifier().concat(#user.username).concat(#result.token + 'tok')")
    public TFAccessToken invalidateAccessToken(final AppUser user, final JsonCommand command) {

        final String token = command.stringValueOfParameterNamed("token");
        final TFAccessToken accessToken = fetchAccessTokenForUser(user, token);

        if(accessToken == null || !accessToken.isValid()) {
            throw new AccessTokenInvalidIException();
        }

        accessToken.setEnabled(false);
        tfAccessTokenRepository.save(accessToken);

        return accessToken;
    }

    @Override
    @Cacheable(value = "userTFAccessToken",
            key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil)" +
                    ".getTenant().getTenantIdentifier().concat(#user.username).concat(#token + 'tok')")
    public TFAccessToken fetchAccessTokenForUser(final AppUser user, final String token) {
        return tfAccessTokenRepository.findByUserAndToken(user, token);
    }

    private OTPDeliveryMethod getSMSDeliveryMethodForUser(final AppUser user) {
        if(!configurationService.isSMSEnabled()) {
            return null;
        }

        if(configurationService.getSMSProviderId() == null) {
            return null;
        }

        if(user.getStaff() == null) {
            return null;
        }
        String mobileNo = user.getStaff().mobileNo();
        if(StringUtils.isBlank(mobileNo)) {
            return null;
        }

        return new OTPDeliveryMethod(TwoFactorConstants.SMS_DELIVERY_METHOD_NAME, mobileNo);
    }

    private OTPDeliveryMethod getEmailDeliveryMethodForUser(final AppUser user) {
        if(!configurationService.isEmailEnabled()) {
            return null;
        }

        return new OTPDeliveryMethod(TwoFactorConstants.EMAIL_DELIVERY_METHOD_NAME, user.getEmail());
    }

    private OTPRequest generateNewToken(final OTPDeliveryMethod deliveryMethod, final boolean extendedAccessToken) {
        int tokenLiveTime = configurationService.getOTPTokenLiveTime();
        int otpLength = configurationService.getOTPTokenLength();
        String token = new RandomOTPGenerator(otpLength).generate();
        return OTPRequest.create(token, tokenLiveTime, extendedAccessToken, deliveryMethod);
    }
}
