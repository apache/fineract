/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;


/**
 * Immutable data object representing a ClientIncentiveAttributes used 
 * for determining interest rate applicable from Interest rate chart.
 */
public class ClientIncentiveAttributes {

    private final boolean isFemale;
    private final boolean isChild;
    private final boolean isSeniorCitizen;


    public static ClientIncentiveAttributes instance(final boolean isFemale, final boolean isChild, final boolean isSeniorCitizen) {
        return new ClientIncentiveAttributes(isFemale, isChild, isSeniorCitizen);
    }

    

    private ClientIncentiveAttributes(final boolean isFemale, final boolean isChild, final boolean isSeniorCitizen) {
        this.isFemale = isFemale;
        this.isChild = isChild;
        this.isSeniorCitizen = isSeniorCitizen; 
    }

    public boolean isFemale(){
        return this.isFemale;
    }
    
    public boolean isChild(){
        return this.isChild;
    }
    
    public boolean isSeniorCitizen(){
        return this.isSeniorCitizen;
    }
}