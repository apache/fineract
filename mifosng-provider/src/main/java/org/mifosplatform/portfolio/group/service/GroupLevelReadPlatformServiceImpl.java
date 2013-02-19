/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.group.domain.GroupLevel;
import org.mifosplatform.portfolio.group.domain.GroupLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupLevelReadPlatformServiceImpl implements GroupLevelReadPlatformService {

    private final PlatformSecurityContext context;
    private final GroupLevelRepository groupLevelRepository;

    @Autowired
    public GroupLevelReadPlatformServiceImpl(final PlatformSecurityContext context, final GroupLevelRepository groupRepository) {
        this.context = context;
        this.groupLevelRepository = groupRepository;
    }

    @Override
    public Collection<GroupLevel> retrieveAllLevels() {
        this.context.authenticatedUser();
        return this.groupLevelRepository.findAll();
    }
}
