/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.report.service;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public interface ReportingProcessService {

    Response processRequest(String reportName, MultivaluedMap<String, String> queryParams);

}