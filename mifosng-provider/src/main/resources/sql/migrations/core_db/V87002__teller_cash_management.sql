CREATE TABLE `m_tellers` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`office_id` BIGINT(20) NOT NULL,
	`debit_account_id` BIGINT(20),
	`credit_account_id` BIGINT(20),
	`name` VARCHAR(50) NOT NULL,
	`description` VARCHAR(100),
	`valid_from` DATE,
	`valid_to` DATE,
	`state` SMALLINT(5),
	PRIMARY KEY (`id`),
	UNIQUE INDEX `m_tellers_name_unq` (`name`),
	INDEX `IK_m_tellers_m_office` (`office_id`),
	CONSTRAINT `FK_m_tellers_m_office` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`),
	CONSTRAINT `FK_m_tellers_gl_account_debit_account_id` FOREIGN KEY (`debit_account_id`) REFERENCES `acc_gl_account` (`id`),
	CONSTRAINT `FK_m_tellers_gl_account_credit_account_id` FOREIGN KEY (`credit_account_id`) REFERENCES `acc_gl_account` (`id`)
	);

CREATE TABLE `m_cashiers` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`staff_id` BIGINT(20),
	`teller_id` BIGINT(20),
	`description` VARCHAR(100),
	`start_date` DATE,
	`end_date` DATE,
	`start_time` varchar(10),
	`end_time` varchar(10),
	`full_day` TINYINT,
	PRIMARY KEY (`id`),
	INDEX `IK_m_cashiers_m_staff` (`staff_id`),
	INDEX `IK_m_cashiers_m_teller` (`teller_id`),
	CONSTRAINT `FK_m_cashiers_m_staff` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
	CONSTRAINT `FK_m_cashiers_m_teller` FOREIGN KEY (`teller_id`) REFERENCES `m_tellers` (`id`)
	); 
		
CREATE TABLE `m_cashier_transactions` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`cashier_id` BIGINT(20) NOT NULL,
	`txn_type` SMALLINT(5)  NOT NULL,
	`txn_amount` DECIMAL (19,6)  NOT NULL,
	`txn_date` DATE  NOT NULL,
	`created_date` DATETIME  NOT NULL,
	`entity_type` VARCHAR(50),
	`entity_id` BIGINT(20),
	`txn_note` VARCHAR(200),
	PRIMARY KEY (`id`),
	INDEX `IK_m_teller_transactions_m_cashier` (`cashier_id`),
	CONSTRAINT `FK_m_teller_transactions_m_cashiers` FOREIGN KEY (`cashier_id`) REFERENCES `m_cashiers` (`id`)
	);

	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'CREATE_TELLER', 'TELLER', 'CREATE', 1
	);
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'UPDATE_TELLER', 'TELLER', 'CREATE', 1
	);
	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'ALLOCATECASHIER_TELLER', 'TELLER', 'ALLOCATE', 1
	);
	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'UPDATECASHIERALLOCATION_TELLER', 'TELLER', 'UPDATECASHIERALLOCATION', 1
	);
	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'DELETECASHIERALLOCATION_TELLER', 'TELLER', 'DELETECASHIERALLOCATION', 1
	);
	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'ALLOCATECASHTOCASHIER_TELLER', 'TELLER', 'ALLOCATECASHTOCASHIER', 1
	);
	
	INSERT INTO m_permission (
		grouping, code, entity_name, action_name, can_maker_checker
	) values (
		'cash_mgmt', 'SETTLECASHFROMCASHIER_TELLER', 'TELLER', 'SETTLECASHFROMCASHIER', 1
	);
	
	INSERT INTO r_enum_value (
		enum_name, enum_id, enum_message_property, enum_value, enum_type
	) values (
		'teller_status', 300, 'Active', 'Active',0
	);
	INSERT INTO r_enum_value (
		enum_name, enum_id, enum_message_property, enum_value, enum_type
	) values (
		'teller_status', 400, 'Inactive', 'Inactive',0
	);
	INSERT INTO r_enum_value (
		enum_name, enum_id, enum_message_property, enum_value, enum_type
	) values (
		'teller_status', 600, 'Closed', 'Closed',0
	);