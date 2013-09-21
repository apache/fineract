package org.mifosplatform.template.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByEntityAndType(TemplateEntity entity, TemplateType type);
}