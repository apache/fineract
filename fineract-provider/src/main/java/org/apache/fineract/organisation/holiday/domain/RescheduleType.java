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
package org.apache.fineract.organisation.holiday.domain;

public enum RescheduleType {

    INVALID(0, "rescheduletype.invalid"), RESCHEDULETOSPECIFICDATE(2, "rescheduletype.rescheduletospecificdate"), //
    RESCHEDULETONEXTREPAYMENTDATE(1, "rescheduletype.rescheduletonextrepaymentdate");

    private final Integer value;
    private final String code;

    private RescheduleType(Integer value, String code) {
            this.value = value;
            this.code = code;
    }

    public static RescheduleType fromInt(int rescheduleTypeValue) {
            RescheduleType enumerration = RescheduleType.INVALID;
            switch (rescheduleTypeValue) {
            case 1:
                    enumerration = RescheduleType.RESCHEDULETONEXTREPAYMENTDATE;
                    break;
            case 2:
                    enumerration = RescheduleType.RESCHEDULETOSPECIFICDATE;
                    break;
            }
            return enumerration;
    }

    public boolean isRescheduleToSpecificDate(){
            return this.value.equals(RescheduleType.RESCHEDULETOSPECIFICDATE.getValue());
    }
    
    public boolean isResheduleToNextRepaymentDate(){
            return this.value.equals(RescheduleType.RESCHEDULETONEXTREPAYMENTDATE.getValue());
    }

    
    public Integer getValue() {
            return this.value;
    }

    public String getCode() {
            return this.code;
    }
    
}