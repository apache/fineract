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
package org.apache.fineract.infrastructure.campaigns.email.domain;

public enum EmailMessageStatusType {

    INVALID(0, "emailMessageStatusType.invalid"), //
    PENDING(100, "emailMessageStatusType.pending"), //
    SENT(200, "emailMessageStatusType.sent"), //
    DELIVERED(300, "emailMessageStatusType.delivered"), //
    FAILED(400, "emailMessageStatusType.failed");

    private final Integer value;
    private final String code;

    public static EmailMessageStatusType fromInt(final Integer statusValue) {

        EmailMessageStatusType enumeration = EmailMessageStatusType.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = EmailMessageStatusType.PENDING;
            break;
            case 200:
                enumeration = EmailMessageStatusType.SENT;
            break;
            case 300:
                enumeration = EmailMessageStatusType.DELIVERED;
            break;
            case 400:
                enumeration = EmailMessageStatusType.FAILED;
            break;
        }
        return enumeration;
    }

    EmailMessageStatusType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isPending(){
        return this.value.equals(EmailMessageStatusType.PENDING.getValue());
    }

    public boolean isSent(){
        return this.value.equals(EmailMessageStatusType.SENT.getValue());
    }
}