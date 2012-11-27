package org.mifosplatform.infrastructure.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Permission findOneByCode(String code);
}
