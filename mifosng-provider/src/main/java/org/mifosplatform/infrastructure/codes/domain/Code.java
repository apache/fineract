package org.mifosplatform.infrastructure.codes.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_code", uniqueConstraints = { @UniqueConstraint(columnNames = { "code_name" }, name = "code_name") })
public class Code extends AbstractPersistable<Long> {

    @Column(name = "code_name", length = 100)
    private String name;

    @Column(name = "is_system_defined")
    private final boolean systemDefined;
    
    @SuppressWarnings("unused")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "code", orphanRemoval = true)
    private Set<CodeValue> values;

    public static Code createNew(final String codeName) {
        return new Code(codeName);
    }

    protected Code() {
        this.systemDefined = false;
    }

    private Code(final String name) {
        this.name = name;
        this.systemDefined = false;
    }
    
    public String name() {
        return this.name;
    }
    
    public boolean isSystemDefined() {
        return this.systemDefined;
    }

    public void update(final CodeCommand command) {
        
        if (systemDefined) {
            throw new SystemDefinedCodeCannotBeChangedException();
        }
        
        if (command.isNameChanged()) {
            this.name = StringUtils.defaultIfEmpty(command.getName(), null);
        }
    }
}