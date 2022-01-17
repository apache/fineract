<#--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->
<h1>Release Artifacts</h1>
<p>
  <ac:structured-macro ac:macro-id="ac1cb3cb-84d4-45aa-820b-05cc42d39dd9" ac:name="jira" ac:schema-version="1">
    <ac:parameter ac:name="server">ASF JIRA</ac:parameter>
    <ac:parameter ac:name="columnIds">issuekey,summary,issuetype,created,updated,duedate,assignee,reporter,customfield_12311032,customfield_12311037,customfield_12311022,customfield_12311027,priority,status,resolution</ac:parameter>
    <ac:parameter ac:name="columns">key,summary,type,created,updated,due,assignee,reporter,Priority,Priority,Priority,Priority,priority,status,resolution</ac:parameter>
    <ac:parameter ac:name="maximumIssues">50</ac:parameter>
    <ac:parameter ac:name="jqlQuery">project = FINERACT AND fixVersion = ${project['fineract.release.version']} AND status not in (Open) </ac:parameter>
    <ac:parameter ac:name="serverId">5aa69414-a9e9-3523-82ec-879b028fb15b</ac:parameter>
  </ac:structured-macro>
</p>
<p>
  <br/>
</p>
