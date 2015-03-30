/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

public class PaymentTypeDomain {

    private Integer id;
    private String name;
    private String description;
    private Boolean isCashPayment;
    private Integer position;

    private PaymentTypeDomain(final Integer id, final String name, final String description, final Boolean isCashPayment,
            final Integer position) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isCashPayment = isCashPayment;
        this.position = position;

    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsCashPayment() {
        return this.isCashPayment;
    }

    public void setIsCashPayment(Boolean isCashPayment) {
        this.isCashPayment = isCashPayment;
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

}
