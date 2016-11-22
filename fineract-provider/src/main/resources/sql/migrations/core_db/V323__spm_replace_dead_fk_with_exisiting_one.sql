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

DROP PROCEDURE IF EXISTS remove_anonymous_fk;

DELIMITER $$

CREATE PROCEDURE remove_anonymous_fk (IN referencee VARCHAR(255), IN referenced VARCHAR(255))
  BEGIN
	  DECLARE fk2drop VARCHAR(255);

	  SELECT
	    CONSTRAINT_NAME
	  FROM
	    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
	  WHERE
	    TABLE_NAME = referencee
	    AND REFERENCED_TABLE_NAME = referenced
	  INTO fk2drop;

	  SET @alter_stmt = concat('ALTER TABLE ',referencee,' DROP FOREIGN KEY ',fk2drop);
    PREPARE pstmt FROM @alter_stmt;
    EXECUTE pstmt;
    DEALLOCATE PREPARE pstmt;
  END $$

DELIMITER ;

CALL remove_anonymous_fk('m_survey_scorecards', 'm_appusers');

ALTER TABLE `m_survey_scorecards` ADD FOREIGN KEY `m_appuser` (`user_id`);

DROP PROCEDURE IF EXISTS remove_anonymous_fk;
