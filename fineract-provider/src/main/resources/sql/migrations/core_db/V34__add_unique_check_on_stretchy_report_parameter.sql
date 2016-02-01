ALTER TABLE stretchy_report_parameter
  ADD UNIQUE INDEX `report_parameter_unique` (`report_id` ASC, `parameter_id` ASC) ;
