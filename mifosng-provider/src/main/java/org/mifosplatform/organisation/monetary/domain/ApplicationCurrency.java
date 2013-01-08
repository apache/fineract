package org.mifosplatform.organisation.monetary.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_currency")
public class ApplicationCurrency extends AbstractPersistable<Long> {

    @Column(name = "code", nullable = false, length = 3)
    private final String code;

    @Column(name = "decimal_places", nullable = false)
    private final Integer decimalPlaces;

    @Column(name = "name", nullable = false, length = 50)
    private final String name;

    @Column(name = "internationalized_name_code", nullable = false, length = 50)
    private final String nameCode;

    @Column(name = "display_symbol", nullable = true, length = 10)
    private final String displaySymbol;

    protected ApplicationCurrency() {
        this.code = null;
        this.name = null;
        this.decimalPlaces = null;
        this.nameCode = null;
        this.displaySymbol = null;
    }

    public ApplicationCurrency(final String code, final String name, final int decimalPlaces, final String nameCode,
            final String displaySymbol) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
        this.nameCode = nameCode;
        this.displaySymbol = displaySymbol;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public Integer getDecimalPlaces() {
        return this.decimalPlaces;
    }

    public String getNameCode() {
        return nameCode;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }
}