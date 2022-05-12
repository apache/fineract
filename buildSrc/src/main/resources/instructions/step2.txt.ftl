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


[INSTRUCTIONS:START]

Before a release is done, make sure that any issues that are fixed have their fix version setup correctly.

"project = FINERACT and resolution = fixed and fixVersion is empty"

Move all unresolved JIRA issues which have this release as Fix Version to the next release

"project = FINERACT and fixVersion = ${project['fineract.release.version']} and status not in ( Resolved, Done, Accepted, Closed )"

You can also run the following query to make sure that the issues fixed for the to-be-released version look accurate:

"project = FINERACT and fixVersion = ${project['fineract.release.version']}"

Finally, check out the output of the JIRA release note tool to see which tickets are included in the release, in order to do a sanity check.

[INSTRUCTIONS:END]
