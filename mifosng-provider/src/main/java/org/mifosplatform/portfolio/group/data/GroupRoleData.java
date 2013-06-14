package org.mifosplatform.portfolio.group.data;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

public class GroupRoleData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final CodeValueData role;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final String clientName;

    public static final GroupRoleData template() {
        return new GroupRoleData(null, null, null, null);
    }

    public GroupRoleData(final Long id, final CodeValueData role, final Long clientId, final String clientName) {
        this.id = id;
        this.role = role;
        this.clientId = clientId;
        this.clientName = clientName;
    }

}
