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

As discussed in https://issues.apache.org/jira/browse/FINERACT-1154, now that everything is final, please do the following to remove the release branch (and just keep the tag),
and make sure that everything on the release tag is merged to develop and that e.g. git describe works:

>> git checkout develop
>> git merge release/${project['fineract.release.version']}
>> git push origin develop
>> git branch -D release/${project['fineract.release.version']}
>> git push origin :release/${project['fineract.release.version']}

[INSTRUCTIONS:END]
