package org.mifosplatform.portfolio.group.domain;

import org.mifosplatform.portfolio.group.exception.GroupRoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupRoleRepositoryWrapper {

    private final GroupRoleRepository repository;

    @Autowired
    public GroupRoleRepositoryWrapper(final GroupRoleRepository rRepository) {
        this.repository = rRepository;
    }

    public GroupRole findOneWithNotFoundDetection(final Long id) {
        final GroupRole entity = this.repository.findOne(id);
        if (entity == null) { throw new GroupRoleNotFoundException(id); }
        return entity;
    }

    public void save(final GroupRole entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final GroupRole entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final GroupRole entity) {
        this.repository.delete(entity);
    }

}
