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
package org.apache.fineract.portfolio.group.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.springframework.stereotype.Component;

/**
 * The typed command pipeline has no maker-checker yet (it is a separate proposal, see {@code fineract-command}). Until
 * it has, a group or center command whose task has maker-checker enabled is rejected instead of being executed without
 * the configured approval. A checker super user is not subject to maker-checker, as in the legacy pipeline.
 */
@Component
@RequiredArgsConstructor
public class GroupMakerCheckerGuard {

    private final PlatformSecurityContext context;
    private final ConfigurationDomainService configurationDomainService;

    public void rejectIfMakerCheckerEnabled(final String action, final GroupTypes groupingType) {
        rejectIfMakerCheckerEnabled(action + (groupingType == GroupTypes.CENTER ? "_CENTER" : "_GROUP"));
    }

    public void rejectIfMakerCheckerEnabled(final String taskPermission) {
        if (configurationDomainService.isMakerCheckerEnabledForTask(taskPermission) && !context.authenticatedUser().isCheckerSuperUser()) {
            throw new GeneralPlatformDomainRuleException("error.msg.maker.checker.not.supported",
                    "Maker-checker is enabled for " + taskPermission + ", which is not supported for this request yet.", taskPermission);
        }
    }
}
