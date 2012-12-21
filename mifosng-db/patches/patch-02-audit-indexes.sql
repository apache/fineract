
ALTER TABLE m_portfolio_command_source
ADD INDEX `action_name` (`action_name` ASC) 
, ADD INDEX `entity_name` (`entity_name` ASC, `resource_id` ASC) 
, ADD INDEX `made_on_date` (`made_on_date` ASC) 
, ADD INDEX `checked_on_date` (`checked_on_date` ASC) 
, ADD INDEX `processing_result_enum` (`processing_result_enum` ASC) 
, ADD INDEX `api_operation` (`api_operation` ASC) 
, ADD INDEX `api_resource` (`api_resource` ASC, `resource_id` ASC) ;
