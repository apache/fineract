/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.portfolio.rate.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.useradministration.domain.AppUser;

import javax.persistence.*;
import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */

@Entity
@Table(name = "m_rate", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "name")})
public class Rate extends AbstractAuditableCustom<AppUser, Long> {

  @Column(name = "name", length = 250, unique = true)
  private String name;

  @Column(name = "percentage", scale = 10, precision = 2, nullable = false)
  private BigDecimal percentage;

  @Column(name = "product_apply", length = 100)
  private String productApply;

  @Column(name = "active", nullable = false)
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "approve_user", nullable = true)
  private AppUser approveUser;


  public Rate() {
  }


  public Rate(String name, BigDecimal percentage, String productApply, boolean active,
      AppUser approveUser) {
    this.name = name;
    this.percentage = percentage;
    this.productApply = productApply;
    this.active = active;
    this.approveUser = approveUser;
  }

  public Rate(String name, BigDecimal percentage, String productApply, boolean active) {
    this.name = name;
    this.percentage = percentage;
    this.productApply = productApply;
    this.active = active;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getPercentage() {
    return percentage;
  }

  public void setPercentage(BigDecimal percentage) {
    this.percentage = percentage;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public AppUser getApproveUser() {
    return approveUser;
  }

  public void setApproveUser(AppUser approveUser) {
    this.approveUser = approveUser;
  }

  public String getProductApply() {
    return productApply;
  }

  public void setProductApply(String productApply) {
    this.productApply = productApply;
  }

  @Override
  public String toString() {
    return "Rate{" +
        "name='" + name + '\'' +
        ", percentage=" + percentage +
        ", productApply='" + productApply + '\'' +
        ", active=" + active +
        ", approveUser=" + approveUser +
        '}';
  }

  public static Rate from(String name, BigDecimal percentage, String productApply, Boolean active) {
    return new Rate(name, percentage, productApply, active);
  }

  public static Rate fromJson(final JsonCommand command, AppUser user) {

    final String name = command.stringValueOfParameterNamed("name");

    final BigDecimal percentage = command.bigDecimalValueOfParameterNamed("percentage");

    final String productApply = command.stringValueOfParameterNamed("productApply");

    final boolean active = command.booleanPrimitiveValueOfParameterNamed("active");

    return new Rate(name, percentage, productApply, active, user);
  }

  public Map<String, Object> update(final JsonCommand command) {

    final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

    final String nameParamName = "name";
    if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
      final String newValue = command.stringValueOfParameterNamed(nameParamName);
      actualChanges.put(nameParamName, newValue);
      this.name = StringUtils.defaultIfEmpty(newValue, null);
    }

    final String percentageParamName = "percentage";
    if (command.isChangeInBigDecimalParameterNamed(percentageParamName, this.percentage)) {
      final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(percentageParamName);
      actualChanges.put(percentageParamName, newValue);
      this.percentage = newValue;
    }

    final String productApplyParamName = "productApply";
    if (command.isChangeInStringParameterNamed(productApplyParamName, this.productApply)) {
      final String newValue = command.stringValueOfParameterNamed(productApplyParamName);
      actualChanges.put(productApplyParamName, newValue);
      this.productApply = StringUtils.defaultIfEmpty(newValue, null);
    }

    final String activeParamName = "active";
    if (command.isChangeInBooleanParameterNamed(activeParamName, this.active)) {
      final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(activeParamName);
      actualChanges.put(activeParamName, newValue);
      this.active = newValue;
    }

    final String approveUserParamName = "approveUserId";
    if (command.isChangeInLongParameterNamed(approveUserParamName, getApproveUserId())) {
      final Long newValue = command.longValueOfParameterNamed(approveUserParamName);
      actualChanges.put(approveUserParamName, newValue);
    }

    return actualChanges;
  }

  private Long getApproveUserId() {
    Long approveUserId = null;
    if (this.approveUser != null) {
      approveUserId = this.approveUser.getId();
    }
    return approveUserId;
  }

  public void assembleFrom(String name, BigDecimal percentage, String productApply, boolean active){
    this.name = name;
    this.percentage = percentage;
    this.productApply = productApply;
    this.active = active;
  }
}
