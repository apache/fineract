/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.domain;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.sms.SmsApiConstants;
import org.mifosplatform.infrastructure.sms.exception.SmsNotFoundException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SmsMessageAssembler {

    private final SmsMessageRepository smsMessageRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SmsMessageAssembler(final SmsMessageRepository smsMessageRepository, final GroupRepositoryWrapper groupRepositoryWrapper,
            final ClientRepositoryWrapper clientRepository, final StaffRepositoryWrapper staffRepository,
            final FromJsonHelper fromApiJsonHelper) {
        this.smsMessageRepository = smsMessageRepository;
        this.groupRepository = groupRepositoryWrapper;
        this.clientRepository = clientRepository;
        this.staffRepository = staffRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public SmsMessage assembleFromJson(final JsonCommand command) {

        final JsonElement element = command.parsedJson();

        String mobileNo = null;

        Group group = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)) {
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.groupIdParamName, element);
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
        }

        Client client = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)) {
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.clientIdParamName, element);
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            mobileNo = client.mobileNo();
        }

        Staff staff = null;
        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.staffIdParamName, element);
            staff = this.staffRepository.findOneWithNotFoundDetection(staffId);
            mobileNo = staff.mobileNo();
        }

        final String message = this.fromApiJsonHelper.extractStringNamed(SmsApiConstants.messageParamName, element);

        return SmsMessage.pendingSms(group, client, staff, message, mobileNo);
    }

    public SmsMessage assembleFromResourceId(final Long resourceId) {
        final SmsMessage sms = this.smsMessageRepository.findOne(resourceId);
        if (sms == null) { throw new SmsNotFoundException(resourceId); }
        return sms;
    }
}