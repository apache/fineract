--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

-- two tables added: ReportCategoryList and FullReportList (FINERACT-1306)
INSERT INTO stretchy_report (report_name, report_type, report_category, report_sql, description, core_report, use_report)VALUES ("ReportCategoryList", 'Table', '(NULL)', '(NULL)', '(NULL)', 1, 1);
INSERT INTO stretchy_report (report_name, report_type, report_category, report_sql, description, core_report, use_report)VALUES ("FullReportList", 'Table', '(NULL)', '(NULL)', '(NULL)', 1, 1);
