package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.client.data.ClientData;

public class GroupTransferData {

    @SuppressWarnings("unused")
    private final Long groupId;
    @SuppressWarnings("unused")
    private final Collection<ClientData> clientOptions;
    @SuppressWarnings("unused")
    private final Collection<GroupGeneralData> groupOptions;
    @SuppressWarnings("unused")
    private final Collection<StaffData> staffOptions;
    @SuppressWarnings("unused")
    private final boolean transferActiveLoans;
    @SuppressWarnings("unused")
    private final boolean inheritDestinationGroupLoanOfficer;

    private GroupTransferData(final Long groupId, final Collection<ClientData> clientOptions,
            final Collection<GroupGeneralData> groupOptions, final Collection<StaffData> staffOptions, final boolean transferActiveLoans,
            final boolean inheritDestinationGroupLoanOfficer) {
        this.groupId = groupId;
        this.clientOptions = clientOptions;
        this.groupOptions = groupOptions;
        this.staffOptions = staffOptions;
        this.transferActiveLoans = transferActiveLoans;
        this.inheritDestinationGroupLoanOfficer = inheritDestinationGroupLoanOfficer;
    }

    public static GroupTransferData template(final Long groupId, final Collection<ClientData> clientOptions,
            final Collection<GroupGeneralData> groupOptions, final Collection<StaffData> staffOptions, final boolean transferActiveLoans,
            final boolean inheritDestinationGroupLoanOfficer) {
        return new GroupTransferData(groupId, clientOptions, groupOptions, staffOptions, transferActiveLoans,
                inheritDestinationGroupLoanOfficer);
    }
}
