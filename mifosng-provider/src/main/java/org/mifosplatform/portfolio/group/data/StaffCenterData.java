package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

public class StaffCenterData {

    private final Long staffId;
    @SuppressWarnings("unused")
    private final String staffName;
    private final Collection<CenterData> meetingFallCenters;

    private StaffCenterData(final Long staffId, final String staffName, final Collection<CenterData> meetingFallCenters) {
        this.staffId = staffId;
        this.staffName = staffName;
        this.meetingFallCenters = meetingFallCenters;
    }

    public static StaffCenterData instance(final Long staffId, final String staffName, final Collection<CenterData> meetingFallCenters) {
        return new StaffCenterData(staffId, staffName, meetingFallCenters);
    }

    public Long getStaffId() {
        return staffId;
    }

    public Collection<CenterData> getMeetingFallCenters() {
        return meetingFallCenters;
    }
}
