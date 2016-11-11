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
package org.apache.fineract.infrastructure.campaigns.sms.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public final class CampaignTriggerWithSubTypes {

    public enum ActualCampaignTriggerType {
        INVALID(0, "campaignTriggerType.invalid"), //
        LOAN(1, "campaignTriggerType.loan"), //
        SAVING(2, "campaignTriggerType.saving"), //
        CLIENT(3, "campaignTriggerType.client");

        private Integer value;
        private String code;

        private ActualCampaignTriggerType(Integer value, String code) {
            this.value = value;
            this.code = code;
        }

        public static ActualCampaignTriggerType fromInt(final Integer typeValue) {
            ActualCampaignTriggerType type = ActualCampaignTriggerType.INVALID;
            switch (typeValue) {
                case 1:
                    type = LOAN;
                break;
                case 2:
                    type = SAVING;
                break;
                case 3:
                    type = CLIENT;
                break;
            }
            return type;
        }

        public Integer getValue() {
            return this.value;
        }

        public String getCode() {
            return this.code;
        }

        public static EnumOptionData toEnumOptionData(final ActualCampaignTriggerType triggerType) {
            final EnumOptionData optionData = new EnumOptionData(new Long(triggerType.getValue()), triggerType.getCode(), triggerType.name());
            return optionData;
        }

        public static EnumOptionData toEnumOptionData(final Integer triggerTypeValue) {
            ActualCampaignTriggerType actualCampaignTriggerType = ActualCampaignTriggerType.fromInt(triggerTypeValue);
            final EnumOptionData optionData = new EnumOptionData(new Long(actualCampaignTriggerType.getValue()),
                    actualCampaignTriggerType.getCode(), actualCampaignTriggerType.name());
            return optionData;
        }

        public boolean isInvalid() {
            return this.value.equals(ActualCampaignTriggerType.INVALID.getValue());
        }
    }

    public enum CampaignTriggerSubType {
        INVALID(0, ActualCampaignTriggerType.INVALID, "campaignTriggerSubType.invalid"), //

        DISBURSE(101, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.disburse"), //
        REPAYMENT(102, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.repayment"), //
        UNDO_DISBURSAL(103, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.undodisbursal"), //
        WRITE_OFF(104, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.writeoff"), //
        ADJUST(105, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.adjust"), //
        UNDO_WRITE_OFF(106, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.undowriteoff"), //
        FORECLOSURE(107, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.foreclosure"), //
        APPROVED(108, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.approved"), //
        WAIVE_INTEREST(109, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.waive.interest"), //
        CLOSE(110, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.close"), //
        CLOSE_AS_RESCHEDULE(111, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.close.as.rescheduled"), //
        ADD_CHARGE(112, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.add.charge"), //
        UPDATE_CHARGE(113, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.update.charge"), //
        WAIVE_CHARGE(114, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.waive.charge"), //
        DELETE_CHARGE(115, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.delete.charge"), //
        CHARGE_PAYMENT(116, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.charge.payment"), //
        INITIATE_TRANSFER(117, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.initiate.transfer"), //
        ACCEPT_TRANSFER(118, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.accept.transfer"), //
        WITHDRAW_TRANSFER(119, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.withdraw.transfer"), //
        REJECT_TRANSFER(120, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reject.transfer"), //
        REASSIGN_OFFICER(121, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reassign.officer"), //
        REMOVE_OFFICER(122, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reassign.officer"), //
        APPLY_OVERDUE_CHARGE(123, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reassign.officer"), //
        INTEREST_RECALCULATION(124, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reassign.officer"), //
        REFUND(125, ActualCampaignTriggerType.LOAN, "campaignTriggerSubType.reassign.officer"), //

        SAVINGS_DEPOSIT(201, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.deposit"), //
        SAVINGS_WITHDRAWAL(202, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.withdrawal"), //
        SAVINGS_ACTIVATE(203, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.activate"), //
        SAVINGS_ADJUST_TRANSACTION(204, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.adjust"), //
        SAVINGS_APPLY_ANNUAL_FEE(205, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.apply.annual.fee"), //
        SAVINGS_CALCULATE_INTEREST(206, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.calclulate.interest"), //
        SAVINGS_CLOSE(207, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.close"), //
        SAVINGS_POST_INTEREST(208, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.post"), //
        SAVINGS_REJECT(209, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.reject"), //
        SAVINGS_UNDO(210, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.undo"), //
        SAVINGS_ADD_CHARGE(211, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.add.charge"), //
        SAVINGS_WAIVE_CHARGE(212, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.waive.charge"), //
        SAVINGS_PAY_CHARGE(213, ActualCampaignTriggerType.SAVING, "campaignTriggerSubType.savings.pay.charge"), //

        CLIENTS_ACTIVATE(301, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.activate"), //
        CLIENTS_CLOSE(302, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.close"), //
        CLIENTS_ACCEPT_TRANSFER(303, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.accept.transfer"), //
        CLIENTS_ASSIGN_STAFF(304, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.assign.staff"), //
        CLIENTS_CREATE(305, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.create"), //
        CLIENTS_DELETE(306, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.delete"), //
        CLIENTS_PROPOSE_TRANSFER(307, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.propose.transfer"), //
        CLIENTS_REACTIVATE(308, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.reactivate"), //
        CLIENTS_REJECT(309, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.reject"), //
        CLIENTS_REJECT_TRANSFER(310, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.reject.transfer"), //
        CLIENTS_WITHDRAW(311, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.withdraw"), //
        CLIENTS_WITHDRAW_TRANSFER(312, ActualCampaignTriggerType.CLIENT, "campaignTriggerSubType.withdraw.transfer");

        private Integer id;
        private ActualCampaignTriggerType type;
        private String code;

        private CampaignTriggerSubType(Integer id, ActualCampaignTriggerType type, String code) {
            this.id = id;
            this.type = type;
            this.code = code;
        }

        public static CampaignTriggerSubType fromInt(final Integer subTypeValue) {
            CampaignTriggerSubType subType = CampaignTriggerSubType.INVALID;
            switch (subTypeValue) {
                case 101:
                    subType = DISBURSE;
                break;
                case 102:
                    subType = REPAYMENT;
                break;
                case 103:
                    subType = ADJUST;
                break;
                case 104:
                    subType = UNDO_DISBURSAL;
                break;
                case 105:
                    subType = WRITE_OFF;
                break;
                case 106:
                    subType = UNDO_WRITE_OFF;
                break;

                case 201:
                    subType = SAVINGS_DEPOSIT;
                break;
                case 202:
                    subType = SAVINGS_WITHDRAWAL;
                break;
                case 203:
                    subType = SAVINGS_ACTIVATE;
                break;
                case 204:
                    subType = SAVINGS_ADJUST_TRANSACTION;
                break;
                case 205:
                    subType = SAVINGS_APPLY_ANNUAL_FEE;
                break;
                case 206:
                    subType = SAVINGS_CALCULATE_INTEREST;
                break;
                case 207:
                    subType = SAVINGS_CLOSE;
                break;
                case 208:
                    subType = SAVINGS_POST_INTEREST;
                break;
                case 209:
                    subType = SAVINGS_REJECT;
                break;
                case 210:
                    subType = SAVINGS_UNDO;
                break;
                case 211:
                    subType = SAVINGS_ADD_CHARGE;
                break;
                case 212:
                    subType = SAVINGS_WAIVE_CHARGE;
                break;
                case 213:
                    subType = SAVINGS_PAY_CHARGE;
                break;

                case 301:
                    subType = CLIENTS_ACTIVATE;
                break;
                case 302:
                    subType = CLIENTS_CLOSE;
                break;
                case 303:
                    subType = CLIENTS_ACCEPT_TRANSFER;
                break;
                case 304:
                    subType = CLIENTS_ASSIGN_STAFF;
                break;
                case 305:
                    subType = CLIENTS_CREATE;
                break;
                case 306:
                    subType = CLIENTS_DELETE;
                break;
                case 307:
                    subType = CLIENTS_PROPOSE_TRANSFER;
                break;
                case 308:
                    subType = CLIENTS_REACTIVATE;
                break;
                case 309:
                    subType = CLIENTS_REJECT;
                break;
                case 310:
                    subType = CLIENTS_REJECT_TRANSFER;
                break;
                case 311:
                    subType = CLIENTS_WITHDRAW;
                break;
                case 312:
                    subType = CLIENTS_WITHDRAW_TRANSFER;
                break;
            }
            return subType;
        }

        public Integer getId() {
            return this.id;
        }

        public ActualCampaignTriggerType getType() {
            return this.type;
        }

        public String getCode() {
            return this.code;
        }

        public static EnumOptionData toEnumOptionData(final Integer triggerSubType) {
            CampaignTriggerSubType subTypeEnum = CampaignTriggerSubType.fromInt(triggerSubType);
            final EnumOptionData optionData = new EnumOptionData(new Long(subTypeEnum.getId()), subTypeEnum.getCode(), subTypeEnum.name());
            return optionData;
        }
    }

    public static List<EnumOptionData> addTypeSubTypeMapping(ActualCampaignTriggerType type) {
        List<EnumOptionData> subTypeList = new ArrayList<>();
        EnumOptionData optionData = null;
        for (CampaignTriggerSubType subType : CampaignTriggerSubType.values()) {
            if (subType.getType().equals(type)) {
                optionData = new EnumOptionData(subType.getId().longValue(), subType.getCode(), subType.name());
                subTypeList.add(optionData);
            }
        }
        return subTypeList;
    }

    public static Collection<TriggerTypeWithSubTypesData> getTriggerTypeAndSubTypes() {
        final Collection<TriggerTypeWithSubTypesData> typesList = new ArrayList<>();
        EnumOptionData actualTriggerType = null;
        for (ActualCampaignTriggerType triggerType : ActualCampaignTriggerType.values()) {
            if (triggerType.isInvalid()) {
                continue;
            }
            List<EnumOptionData> subTypeList = addTypeSubTypeMapping(triggerType);
            actualTriggerType = ActualCampaignTriggerType.toEnumOptionData(triggerType);
            TriggerTypeWithSubTypesData triggerTypeWithSubTypesData = new TriggerTypeWithSubTypesData(actualTriggerType, subTypeList);
            typesList.add(triggerTypeWithSubTypesData);
        }
        return typesList;
    }

}
