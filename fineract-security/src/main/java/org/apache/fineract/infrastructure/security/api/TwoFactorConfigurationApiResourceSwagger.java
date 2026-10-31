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
package org.apache.fineract.infrastructure.security.api;

import io.swagger.v3.oas.annotations.media.Schema;

public final class TwoFactorConfigurationApiResourceSwagger {

    private TwoFactorConfigurationApiResourceSwagger() {

    }

    @Schema(description = "GetTwoFactorConfigurationResponse")
    public static final class GetTwoFactorConfigurationResponse {

        private GetTwoFactorConfigurationResponse() {

        }

        @Schema(name = "otp-delivery-email-enable", example = "true")
        public Boolean otpDeliveryEmailEnable;
        @Schema(name = "otp-delivery-email-subject", example = "Fineract Two-Factor Authentication Token")
        public String otpDeliveryEmailSubject;
        @Schema(name = "otp-delivery-email-body", example = "Hello {{username}}.\nYour OTP login token is {{token}}.")
        public String otpDeliveryEmailBody;
        @Schema(name = "otp-delivery-sms-enable", example = "false")
        public Boolean otpDeliverySmsEnable;
        @Schema(name = "otp-delivery-sms-provider", example = "1")
        public Integer otpDeliverySmsProvider;
        @Schema(name = "otp-delivery-sms-text", example = "Your authentication token for Fineract is {{token}}.")
        public String otpDeliverySmsText;
        @Schema(name = "otp-token-live-time", description = "Seconds", example = "300")
        public Integer otpTokenLiveTime;
        @Schema(name = "otp-token-length", example = "5")
        public Integer otpTokenLength;
        @Schema(name = "access-token-live-time", description = "Seconds", example = "86400")
        public Integer accessTokenLiveTime;
        @Schema(name = "access-token-live-time-extended", description = "Seconds", example = "604800")
        public Integer accessTokenLiveTimeExtended;
    }

    @Schema(description = "PutTwoFactorConfigurationRequest")
    public static final class PutTwoFactorConfigurationRequest {

        private PutTwoFactorConfigurationRequest() {

        }

        @Schema(name = "otp-delivery-email-enable", example = "true")
        public Boolean otpDeliveryEmailEnable;
        @Schema(name = "otp-delivery-email-subject", example = "Fineract Two-Factor Authentication Token")
        public String otpDeliveryEmailSubject;
        @Schema(name = "otp-delivery-email-body", example = "Hello {{username}}.\nYour OTP login token is {{token}}.")
        public String otpDeliveryEmailBody;
        @Schema(name = "otp-delivery-sms-enable", example = "false")
        public Boolean otpDeliverySmsEnable;
        @Schema(name = "otp-delivery-sms-provider", example = "1")
        public Integer otpDeliverySmsProvider;
        @Schema(name = "otp-delivery-sms-text", example = "Your authentication token for Fineract is {{token}}.")
        public String otpDeliverySmsText;
        @Schema(name = "otp-token-live-time", description = "Seconds", example = "300")
        public Integer otpTokenLiveTime;
        @Schema(name = "otp-token-length", example = "5")
        public Integer otpTokenLength;
        @Schema(name = "access-token-live-time", description = "Seconds", example = "86400")
        public Integer accessTokenLiveTime;
        @Schema(name = "access-token-live-time-extended", description = "Seconds", example = "604800")
        public Integer accessTokenLiveTimeExtended;
    }

    @Schema(description = "PutTwoFactorConfigurationResponse")
    public static final class PutTwoFactorConfigurationResponse {

        private PutTwoFactorConfigurationResponse() {

        }

        @Schema(description = "The settings whose value changed")
        public PutTwoFactorConfigurationRequest changes;
    }
}
