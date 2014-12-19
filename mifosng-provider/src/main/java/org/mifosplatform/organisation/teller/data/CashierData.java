/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;

/**
 * Represents a cashier, providing access to the cashier's office, staff
 * information, teller, and more.
 *
 * @author Markus Geiss
<<<<<<< HEAD
 * @since 2.0.0
 * @see org.mifosplatform.organisation.teller.domain.Cashier
 * @since 2.0.0
 */
public final class CashierData implements Serializable {

    private final Long id;
    private final Long tellerId;
    private final Long officeId;
    private final Long staffId;
    private final String description;
    private final Date startDate;
    private final Date endDate;
    private final Boolean isFullDay;
    private final String startTime;
    private final String endTime;
    
    // Template fields
    private final String officeName;
    private final String tellerName;
    private final String staffName;
    private final Collection<StaffData> staffOptions;

    /*
     * Creates a new cashier.
     */
    private CashierData(final Long id, final Long officeId, String officeName, 
    		final Long staffId, final String staffName, final Long tellerId, final String tellerName,
    		final String description,
    		final Date startDate, final Date endDate, final Boolean isFullDay,
            final String startTime, final String endTime, Collection<StaffData> staffOptions) {
        this.id = id;
        this.officeId = officeId;
        this.staffId = staffId;
        this.tellerId = tellerId;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isFullDay = isFullDay;
        this.startTime = startTime;
        this.endTime = endTime;
        
        this.officeName = officeName;
        this.tellerName = tellerName;
        this.staffName = staffName;
        this.staffOptions = staffOptions;
        
    }

    /**
     * Creates a new cashier.
     * <p/>
     * <p>The valid from/to dates may be used to define a time period in which
     * the cashier is assignable to a teller.</p>
     * <p/>
     * <p>The start/end times may be used to define a time period in which
     * the cashier works part time.</p>
     *
     * @param id          the primary key of this cashier
     * @param officeId    the primary key of the related office
     * @param staffId     the primary key of the related staff
     * @param tellerId    the primary key of the related teller
     * @param description the description of this cashier
     * @param validFrom   the valid from date of this cashier
     * @param validTo     the valid to date of this cashier
     * @param partTime    the part time flag of this cashier
     * @param startTime   the start time of this cashier
     * @param endTime     the end time of this cashier
     * @return the cashier
     */
    public static CashierData instance(final Long id, final Long officeId, String officeName, 
    		final Long staffId, final String staffName, final Long tellerId, final String tellerName,
    		final String description, final Date startDate, final Date endDate,
    		final Boolean isFullDay, final String startTime, final String endTime) {
        return new CashierData(id, officeId, officeName, staffId, staffName, tellerId, tellerName, 
        		description, startDate, endDate, isFullDay, startTime, endTime, null);
    }
    
    /*
     * Creates a new cashier.
     */
    public static CashierData template (final Long officeId, final String officeName, 
    		final Long tellerId, final String tellerName, final Collection<StaffData> staffOptions) {
        return new CashierData(null, officeId, officeName, null, null, tellerId, tellerName, 
        		null, null, null, null, null, null, staffOptions);
    }

    /**
     * Returns the primary key of this cashier.
     *
     * @return the primary key of this cashier
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the primary key of this cashiers related office.
     *
     * @return the primary key of this cashiers related office
     */
    public Long getOfficeId() {
        return officeId;
    }

    /**
     * Returns the primary key of this cashiers related staff.
     *
     * @return the primary key of this cashiers related staff
     */
    public Long getStaffId() {
        return staffId;
    }

    /**
     * Returns the primary key of this cashiers related teller.
     *
     * @return the primary key of this cashiers related teller
     */
    public Long getTellerId() {
        return tellerId;
    }

    /**
     * Returns the description of this cashier.
     *
     * @return the description of this cashier
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the valid from date of this cashier.
     * <p/>
     * <p>The valid from/to dates may be used to define a time period in which
     * the cashier is assignable to a teller.</p>
     *
     * @return the valid from date of this cashier
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the valid to date of this cashier.
     * <p/>
     * <p>The valid from/to dates may be used to define a time period in which
     * the cashier is assignable to a teller.</p>
     *
     * @return the valid to date of this cashier
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Returns whether this cashier works part time or not.
     *
     * @return {@code true} if this cashier works part time; {@code false} otherwise
     */
    public Boolean isFullDay() {
        return isFullDay;
    }

    /**
     * Returns the start time of this cashier.
     * <p/>
     * <p>The start/end times may be used to define a time period in which
     * the cashier works part time.</p>
     *
     * @return the start time of this cashier
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this cashier.
     * <p/>
     * <p>The start/end times may be used to define a time period in which
     * the cashier works part time.</p>
     *
     * @return the start time of this cashier
     */
    public String getEndTime() {
        return endTime;
    }

	public String getOfficeName() {
		return officeName;
	}

	public String getTellerName() {
		return tellerName;
	}
	
	public String getStaffName() {
		return staffName;
	}
}
