package org.mifosng.platform.accounting.domain;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_acc_coa", uniqueConstraints={@UniqueConstraint(columnNames = {"gl_code"}, name="acc_gl_code")})
public class ChartOfAccounts extends AbstractPersistable<Long> {

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<ChartOfAccounts> children = new LinkedList<ChartOfAccounts>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChartOfAccounts       parent;

    @SuppressWarnings("unused")
	@Column(name = "name", nullable = false, length=45)
	private String name;
    
    @SuppressWarnings("unused")
    @Column(name = "gl_code", nullable = false, length=100)
   	private String glCode;
    
    @SuppressWarnings("unused")
    @Column(name = "is_disabled", nullable=false)
   	private boolean disabed = false;
    
    @SuppressWarnings("unused")
    @Column(name = "manual_entries_allowed", nullable=false)
   	private boolean manualEntriesAllowed = true;
    
    @SuppressWarnings("unused")
    @Column(name = "category", nullable = false, length=45)
   	private String category;
    
    @SuppressWarnings("unused")
    @Column(name = "ledger_type", nullable = false, length=45)
   	private String ledgerType;
    
    @SuppressWarnings("unused")
    @Column(name = "description", nullable = false, length=500)
   	private String description;
    
    public static ChartOfAccounts rootAccount(final String name) {
        return new ChartOfAccounts(null, name);
    }
    
    public static ChartOfAccounts createNew(final ChartOfAccounts parent, final String name) {
		return new ChartOfAccounts(parent, name);
	}

    protected ChartOfAccounts() {
    	//
    }

    private ChartOfAccounts(final ChartOfAccounts parent, final String name) {
        this.parent = parent;
        if (parent != null) {
            this.parent.addChild(this);
        } 
        
        if (StringUtils.isNotBlank(name)) {
        	this.name = name.trim();
        } else {
        	this.name = null;
        }
        this.glCode = "TEST-1";
        this.disabed = false;
        this.manualEntriesAllowed = true;
        this.category = "ASSETS";
        this.ledgerType = "HEADER";
        this.description = "Some description value..";
    }

	private void addChild(final ChartOfAccounts coa) {
        this.children.add(coa);
    }
}