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
package org.apache.fineract.infrastructure.bulkimport.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.fineract.infrastructure.bulkimport.populator.OfficeSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.PersonnelSheetPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.WorkbookPopulator;
import org.apache.fineract.infrastructure.bulkimport.populator.client.ClientWorkbookPopulator;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BulkImportWorkbookPopulatorServiceImpl implements BulkImportWorkbookPopulatorService {

  private final PlatformSecurityContext context;
  private final OfficeReadPlatformService officeReadPlatformService;
  private final StaffReadPlatformService staffReadPlatformService;

  @Autowired
  public BulkImportWorkbookPopulatorServiceImpl(final PlatformSecurityContext context,
      final OfficeReadPlatformService officeReadPlatformService,
      final StaffReadPlatformService staffReadPlatformService) {
    this.officeReadPlatformService = officeReadPlatformService;
    this.staffReadPlatformService = staffReadPlatformService;
    this.context = context;
  }

  @Override
  public Response getTemplate(final String entityType, final Long officeId, final Long staffId) {

    WorkbookPopulator populator = null;
    final Workbook workbook = new HSSFWorkbook();
    if (entityType.trim().equalsIgnoreCase(ClientApiConstants.CLIENT_RESOURCE_NAME)) {
      populator = populateClientWorkbook(officeId, staffId);
    } else
      throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
          "Unable to find requested resource");
    populator.populate(workbook);
    return buildResponse(workbook, entityType);
  }

  private WorkbookPopulator populateClientWorkbook(final Long officeId, final Long staffId) {
    this.context.authenticatedUser().validateHasReadPermission("OFFICE");
    this.context.authenticatedUser().validateHasReadPermission("STAFF");
    List<OfficeData> offices = fetchOffices(officeId);
    List<StaffData> staff = fetchStaff(staffId);

    return new ClientWorkbookPopulator(new OfficeSheetPopulator(offices),
        new PersonnelSheetPopulator(staff, offices));
  }

  private Response buildResponse(final Workbook workbook, final String entity) {
    String filename = entity + DateUtils.getLocalDateOfTenant().toString() + ".xls";
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      workbook.write(baos);
    } catch (IOException e) {
      e.printStackTrace();
    }

    final ResponseBuilder response = Response.ok(baos.toByteArray());
    response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    response.header("Content-Type", "application/vnd.ms-excel");
    return response.build();
  }

  @SuppressWarnings("unchecked")
  private List<OfficeData> fetchOffices(final Long officeId) {
    List<OfficeData> offices = null;
    if (officeId == null) {
      Boolean includeAllOffices = Boolean.TRUE;
      offices = (List) this.officeReadPlatformService.retrieveAllOffices(includeAllOffices, null);
    } else {
      offices = new ArrayList<>();
      offices.add(this.officeReadPlatformService.retrieveOffice(officeId));
    }
    return offices;
  }

  @SuppressWarnings("unchecked")
  private List<StaffData> fetchStaff(final Long staffId) {
    List<StaffData> staff = null;
    if (staffId == null)
      staff =
          (List) this.staffReadPlatformService.retrieveAllStaff(null, null, Boolean.FALSE, null);
    else {
      staff = new ArrayList<>();
      staff.add(this.staffReadPlatformService.retrieveStaff(staffId));
    }
    return staff;
  }

}
