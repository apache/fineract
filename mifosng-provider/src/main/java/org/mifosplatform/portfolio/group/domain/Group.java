/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.domain.Client;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_group")
public class Group extends AbstractPersistable<Long> {

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Group> children = new LinkedList<Group>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Group parent;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    private GroupLevel groupLevel;

    @Column(name = "hierarchy", length = 100)
    private String hierarchy;

    @Column(name = "name", length = 100, unique = true)
    private String name;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private Set<Client> clientMembers;

    public Group() {
        this.name = null;
        this.externalId = null;
        this.clientMembers = new HashSet<Client>();
    }

    public static Group newGroup(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel,
            final String name, final String externalId, final Set<Client> clientMembers, final Set<Group> childGroups) {
        return new Group(office, staff, parent, groupLevel, name, externalId, clientMembers, childGroups);
    }

    public Group(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel, final String name,
            final String externalId, final Set<Client> clientMembers, final Set<Group> childGroups) {
        this.office = office;
        this.staff = staff;
        this.groupLevel = groupLevel;
        this.parent = parent;

        if (parent != null) {
            this.parent.addChild(this);
        }

        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        if (clientMembers != null) {
            this.clientMembers = clientMembers;
        }
        if (childGroups != null) {
            this.children.addAll(childGroups);
        }
    }

    private void addChild(final Group group) {
        this.children.add(group);
    }

    public Long getOfficeId() {
        return this.office.getId();
    }

    public Staff getStaff() {
        return this.staff;
    }

    public void setStaff(final Staff staff) {
        this.staff = staff;
    }

    public Long getStaffId() {
        return this.staff.getId();
    }

    public GroupLevel getGroupLevel() {
        return this.groupLevel;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getName() {
        return this.name;
    }

    public Set<Client> getClientMembers() {
        return this.clientMembers;
    }

    public Group getParent() {
        return this.parent;
    }

    public void setParent(final Group parent) {
        this.parent = parent;
    }

    public void setClientMembers(final Set<Client> clientMembers) {
        this.clientMembers = clientMembers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public void setLevelId(final GroupLevel groupLevel) {
        this.groupLevel = groupLevel;
    }

    public void assigStaff(final Staff newStaff) {
        this.staff = newStaff;
    }

    public void unassigStaff() {
        this.staff = null;
    }

    public void addClientMember(final Client member) {
        this.clientMembers.add(member);
    }

    public boolean hasClientAsMember(final Client client) {
        return this.clientMembers.contains(client);
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on group so it wont appear
     * in query/report results.
     * 
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.externalId = getId() + "_" + this.externalId;
        this.name = getId() + "_" + this.name;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void generateHierarchy() {

        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = "." + getId() + ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public boolean isOfficeIdentifiedBy(final Long officeId) {
        return this.office.identifiedBy(officeId);
    }

    public Long officeId() {
        return this.office.getId();
    }
}