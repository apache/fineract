package org.mifosplatform.portfolio.group.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_group")
public class Group extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

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

    public static Group newGroup(Office office, String name, String externalId, Set<Client> clientMembers) {
        return new Group(office, name, externalId, clientMembers);
    }

    public Group(Office office, String name, String externalId, Set<Client> clientMembers) {
        this.office = office;
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
    }

    public void update(final GroupCommand command, final Office groupOffice, final Set<Client> clientMembers) {
        if (command.isExternalIdChanged()) {
            this.externalId = command.getExternalId();
        }

        if (command.isOfficeIdChanged()) {
            this.office = groupOffice;
        }

        if (command.isNameChanged()) {
            this.name = command.getName();
        }

        if (clientMembers != null && command.isClientMembersChanged()) {
            this.clientMembers = clientMembers;
        }
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
        this.externalId = this.getId() + "_" + this.externalId;
        this.name = this.getId() + "_" + this.name;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
