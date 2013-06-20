package org.mifosplatform.portfolio.group.service;

import java.util.Collection;

import org.mifosplatform.portfolio.group.data.GroupRoleData;

public interface GroupRolesReadPlatformService {

    Collection<GroupRoleData> retrieveGroupRoles(Long groupId);

    GroupRoleData retrieveGroupRole(Long groupId, Long roleId);
}
