SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `m_survey_scorecards`;
CREATE TABLE `m_survey_scorecards` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `survey_id` BIGINT(20) NOT NULL,
  `question_id` BIGINT(20) NOT NULL,
  `response_id` BIGINT(20) NOT NULL,
  `user_id` BIGINT(20) NOT NULL,
  `client_id` BIGINT(20) NOT NULL,
  `created_on` DATETIME NULL DEFAULT NULL,
  `a_value` INT(4) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`survey_id`) REFERENCES `m_surveys` (`id`),
  FOREIGN KEY (`question_id`) REFERENCES `m_survey_questions` (`id`),
  FOREIGN KEY (`response_id`) REFERENCES `m_survey_responses` (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `m_appusers` (`id`),
  FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
);
SET FOREIGN_KEY_CHECKS = 1;
