/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.HashMap;

import com.google.gson.Gson;

public class CenterDomain {

    public static class Builder {

        private int id;
        private HashMap status;
        private boolean active;
        private String name;
        private String externalId;
        private int staffId;
        private int officeId;
        private String officeName;
        private String hierarchy;

        private Builder(final int id, final int statusid, final String statuscode, final String statusvalue, final boolean active,
                final String name, final String externalId, final int staffId, final int officeID, final String officeName,
                final String hierarchy) {
            this.id = id;
            this.status = new HashMap();
            this.status.put("id", statusid);
            this.status.put("code", statuscode);
            this.status.put("value", statusvalue);
            this.active = active;
            this.name = name;
            this.externalId = externalId;
            this.staffId = staffId;
            this.officeId = officeID;
            this.officeName = officeName;
            this.hierarchy = hierarchy;
        }

        public CenterDomain build() {
            return new CenterDomain(this.id, (int) this.status.get("id"), (String) this.status.get("code"),
                    (String) this.status.get("value"), this.active, this.name, this.externalId, this.staffId, this.officeId,
                    this.officeName, this.hierarchy);
        }
    }

    private int id;
    private HashMap status;
    private boolean active;
    private String name;
    private String externalId;
    private int staffId;
    private int officeId;
    private String officeName;
    private String hierarchy;

    CenterDomain() {
        super();
    }

    private CenterDomain(final int id, final int statusid, final String statuscode, final String statusvalue, final boolean active,
            final String name, final String externalId, final int staffId, final int officeID, final String officeName,
            final String hierarchy) {
        this.id = id;
        this.status = new HashMap();
        this.status.put("id", statusid);
        this.status.put("code", statuscode);
        this.status.put("value", statusvalue);
        this.active = active;
        this.name = name;
        this.externalId = externalId;
        this.staffId = staffId;
        this.officeId = officeID;
        this.officeName = officeName;
        this.hierarchy = hierarchy;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static CurrencyDomain fromJSON(final String jsonData) {
        return new Gson().fromJson(jsonData, CurrencyDomain.class);
    }

    public static Builder create(final int id, final int statusid, final String statuscode, final String statusvalue, final boolean active,
            final String name, final String externalId, final int staffId, final int officeID, final String officeName,
            final String hierarchy) {
        return new Builder(id, statusid, statuscode, statusvalue, active, name, externalId, staffId, officeID, officeName, hierarchy);
    }

    public String getExternalId() {
        return this.externalId;
    }

    public int getStaffId() {
        return this.staffId;
    }

    public int getId() {
        return this.id;
    }

    public HashMap getStatus() {
        return this.status;
    }

    public boolean isActive() {
        return this.active;
    }

    public String getName() {
        return this.name;
    }

    public int getOfficeId() {
        return this.officeId;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        if (this.id >= 0) hash += this.id;
        if (this.status != null) {
            if ((int) this.status.get("id") >= 0) hash += (int) this.status.get("id");
            if ((String) this.status.get("code") != null) hash += this.status.get("code").hashCode();
            if ((String) this.status.get("value") != null) hash += this.status.get("value").hashCode();
        }
        if (this.name != null) hash += this.name.hashCode();
        if (this.officeId >= 0) hash += this.officeId;
        if (this.officeName != null) hash += this.officeName.hashCode();
        if (this.hierarchy != null) hash += this.hierarchy.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) { return true; }

        if (!(obj instanceof CenterDomain)) return false;

        CenterDomain cd = (CenterDomain) obj;

        if (this.hashCode() == cd.hashCode()) return true;
        return false;
    }
}
