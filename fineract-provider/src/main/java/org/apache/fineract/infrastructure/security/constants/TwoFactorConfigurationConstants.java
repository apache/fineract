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
package org.apache.fineract.infrastructure.security.constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TwoFactorConfigurationConstants {

    public static final String RESOURCE_NAME = "TWOFACTOR_CONFIGURATION";

    public static final String ENABLE_EMAIL_DELIVERY = "otp-delivery-email-enable";
    public static final String EMAIL_SUBJECT = "otp-delivery-email-subject";
    public static final String EMAIL_BODY = "otp-delivery-email-body";

    public static final String ENABLE_SMS_DELIVERY = "otp-delivery-sms-enable";
    public static final String SMS_PROVIDER_ID = "otp-delivery-sms-provider";
    public static final String SMS_MESSAGE_TEXT = "otp-delivery-sms-text";

    public static final String OTP_TOKEN_LIVE_TIME = "otp-token-live-time";
    public static final String OTP_TOKEN_LENGTH = "otp-token-length";

    public static final String ACCESS_TOKEN_LIVE_TIME = "access-token-live-time";
    public static final String ACCESS_TOKEN_LIVE_TIME_EXTENDED = "access-token-live-time-extended";

    public static final Set<String> REQUEST_DATA_PARAMETERS =
            new HashSet<>(Arrays.asList(ENABLE_EMAIL_DELIVERY, EMAIL_SUBJECT, EMAIL_BODY,
                    ENABLE_SMS_DELIVERY, SMS_PROVIDER_ID, SMS_MESSAGE_TEXT, OTP_TOKEN_LIVE_TIME,
                    OTP_TOKEN_LENGTH, ACCESS_TOKEN_LIVE_TIME, ACCESS_TOKEN_LIVE_TIME_EXTENDED));

    public static final List<String> STRING_PARAMETERS =
            Arrays.asList(EMAIL_SUBJECT, EMAIL_BODY, SMS_MESSAGE_TEXT);

    public static final List<String> BOOLEAN_PARAMETERS =
            Arrays.asList(ENABLE_EMAIL_DELIVERY, ENABLE_SMS_DELIVERY);

    public static final List<String> NUMBER_PARAMETERS =
            Arrays.asList(SMS_PROVIDER_ID, OTP_TOKEN_LIVE_TIME, OTP_TOKEN_LENGTH,
                    ACCESS_TOKEN_LIVE_TIME, ACCESS_TOKEN_LIVE_TIME_EXTENDED);

}
