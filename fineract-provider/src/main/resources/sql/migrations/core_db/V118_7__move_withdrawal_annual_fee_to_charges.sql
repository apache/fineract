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

ALTER TABLE `m_savings_account_charge`
	CHANGE COLUMN `due_for_collection_as_of_date` `charge_due_date` DATE NULL DEFAULT NULL AFTER `charge_time_enum`;

ALTER TABLE `m_savings_account_charge`
	ADD COLUMN `fee_on_month` SMALLINT(5) NULL DEFAULT NULL AFTER `charge_due_date`,
	ADD COLUMN `fee_on_day` SMALLINT(5) NULL DEFAULT NULL AFTER `fee_on_month`,
	ADD COLUMN `is_active` TINYINT(1) NOT NULL DEFAULT '1' AFTER `waived`;


delimiter //
CREATE PROCEDURE migrate_withdrwal_fees()
begin
	declare no_more_rows boolean default false;
	declare v_currency_code  VARCHAR(3);
	declare v_withdrawal_fee_type_enum  SMALLINT(5);
	declare v_withdrawal_fee_amount_charge_def DECIMAL(19,6);
	declare v_account_id BIGINT(20);
	declare v_withdrawal_fee_amount DECIMAL(19,6);
	declare t_calculation_percentage DECIMAL(19,6);
	declare t_calculation_on_amount DECIMAL(19,6);
	declare t_withdrawal_fee_name VARCHAR(100);

	declare t_charge_id BIGINT(20);
	declare t_savings_charge_id BIGINT(20);

	-- savings transaction variables
	declare v_savings_transaction_id BIGINT(20);
	declare v_transaction_amount DECIMAL(19,6);


	declare cursor1 cursor for
      select sa.currency_code, sa.withdrawal_fee_type_enum, sa.withdrawal_fee_amount
      from m_savings_account sa where sa.withdrawal_fee_amount is not null and sa.withdrawal_fee_amount > 0 group by sa.currency_code, sa.withdrawal_fee_type_enum, sa.withdrawal_fee_amount;

	declare cursor2 cursor for
      select sa.id, sa.withdrawal_fee_amount from m_savings_account sa where sa.currency_code=v_currency_code and sa.withdrawal_fee_type_enum=v_withdrawal_fee_type_enum and sa.withdrawal_fee_amount is not null and sa.withdrawal_fee_amount > 0;

    declare cursor3 cursor for
      select sat.id, sat.amount from m_savings_account_transaction sat
      where sat.savings_account_id=v_account_id and sat.transaction_type_enum=4;

	declare continue handler for not found
      set no_more_rows := true;

	open cursor1;
    LOOP1: loop
	fetch cursor1 into v_currency_code, v_withdrawal_fee_type_enum, v_withdrawal_fee_amount_charge_def;
	if no_more_rows then
		close cursor1;
		leave LOOP1;
	end if;
	-- set withdrawal fee name
	if(v_withdrawal_fee_type_enum = 1) then
		set t_withdrawal_fee_name = CONCAT('Withdrawal fee-Flat-',v_currency_code);
	else
		set t_withdrawal_fee_name = CONCAT('Withdrawal fee-Percentage-',v_currency_code);
	end if;

	-- get charge id if already exists
	set t_charge_id = (select id from m_charge where name = t_withdrawal_fee_name);

	if t_charge_id is null then
		-- add withdrawal fee to charges
		INSERT INTO `m_charge` (`name`, `currency_code`, `charge_applies_to_enum`, `charge_time_enum`, `charge_calculation_enum`, `charge_payment_mode_enum`, `amount`, `is_penalty`, `is_active`, `is_deleted`) VALUES (t_withdrawal_fee_name , v_currency_code, 2, 5, v_withdrawal_fee_type_enum, 0, v_withdrawal_fee_amount_charge_def, 0, 1, 0);

		-- get inserted charge id
		set t_charge_id = last_insert_id();
	end if;

	open cursor2;
        LOOP2: loop
		fetch cursor2 into v_account_id, v_withdrawal_fee_amount;
		if no_more_rows then
		set no_more_rows := false;
		close cursor2;
		leave LOOP2;
            end if;

            if (v_withdrawal_fee_type_enum=1) then
		set t_calculation_percentage = NULL;
		set t_calculation_on_amount = NULL;
            else
	            set t_calculation_percentage = v_withdrawal_fee_amount;
	            set v_withdrawal_fee_amount = 0;
	            set t_calculation_on_amount = 0;
            end if;

            if not exists (select id from m_savings_account_charge sac where
		sac.savings_account_id=v_account_id and sac.charge_id=t_charge_id and sac.charge_time_enum=5) then

	            -- attach withdrawal charge to savings
	            INSERT INTO `m_savings_account_charge` (`savings_account_id`, `charge_id`, `is_penalty`, `charge_time_enum`, `charge_due_date`, `fee_on_month`, `fee_on_day`, `charge_calculation_enum`, `calculation_percentage`, `calculation_on_amount`, `amount`, `amount_paid_derived`, `amount_waived_derived`, `amount_writtenoff_derived`, `amount_outstanding_derived`, `is_paid_derived`, `waived`, `is_active`) VALUES (v_account_id, t_charge_id, 0, 5, NULL, NULL, NULL, v_withdrawal_fee_type_enum, t_calculation_percentage, t_calculation_on_amount, v_withdrawal_fee_amount, NULL, NULL, NULL, 0.000000, 0, 0, 1);

	            -- set savings account charge id
	            set t_savings_charge_id = last_insert_id();

	        else

			set t_savings_charge_id = (select id from m_savings_account_charge sac where sac.savings_account_id=v_account_id and sac.charge_id=t_charge_id and sac.charge_time_enum=5);

	        end if;


            open cursor3;
            LOOP3: loop
		fetch cursor3 into v_savings_transaction_id, v_transaction_amount;

			if no_more_rows then
			set no_more_rows := false;
			close cursor3;
			leave LOOP3;
	            end if;

	            if not exists (select id from m_savings_account_charge_paid_by sacp where
		sacp.savings_account_transaction_id=v_savings_transaction_id and sacp.savings_account_charge_id=t_savings_charge_id) then

		            -- insert a record into savings account charge paid by
		            INSERT INTO `m_savings_account_charge_paid_by` (`savings_account_transaction_id`, `savings_account_charge_id`, `amount`) VALUES(v_savings_transaction_id, t_savings_charge_id, v_transaction_amount);

		        end if;

	        end loop LOOP3;
        end loop LOOP2;
    end loop LOOP1;
end //

CREATE PROCEDURE migrate_annual_fees()
begin
	declare no_more_rows boolean default false;
	declare v_currency_code  VARCHAR(3);
	declare v_annual_fee_amount_charge_def DECIMAL(19,6);
	declare v_account_id BIGINT(20);
	declare v_annual_fee_amount DECIMAL(19,6);
	declare v_annual_fee_on_month SMALLINT(5);
	declare v_annual_fee_on_day SMALLINT(5);
	declare v_annual_fee_next_due_date DATE;
	declare t_annual_fee_name VARCHAR(100);
	declare t_charge_id BIGINT(20);
	declare t_savings_charge_id BIGINT(20);

	-- savings transaction variables
	declare v_savings_transaction_id BIGINT(20);
	declare v_transaction_amount DECIMAL(19,6);


	declare cursor1 cursor for
      select sa.currency_code, sa.annual_fee_amount
      from m_savings_account sa where sa.annual_fee_amount is not null and sa.annual_fee_on_month is not null and sa.annual_fee_on_day is not null group by sa.currency_code, sa.annual_fee_amount;

	declare cursor2 cursor for
      select sa.id, sa.annual_fee_amount, sa.annual_fee_on_month, sa.annual_fee_on_day, sa.annual_fee_next_due_date from m_savings_account sa where sa.currency_code=v_currency_code and sa.annual_fee_amount is not null and sa.annual_fee_on_month is not null and sa.annual_fee_on_day is not null;

    declare cursor3 cursor for
      select sat.id, sat.amount from m_savings_account_transaction sat
      where sat.savings_account_id=v_account_id and sat.transaction_type_enum=5;

	declare continue handler for not found
      set no_more_rows := true;

	open cursor1;
    LOOP1: loop
	fetch cursor1 into v_currency_code, v_annual_fee_amount_charge_def;

	if no_more_rows then
		close cursor1;
		leave LOOP1;
	end if;

	-- set annual fee name
	set t_annual_fee_name = CONCAT('Annual fee - ',v_currency_code);

	-- get charge id if already exists
	set t_charge_id = (select id from m_charge where name = t_annual_fee_name);

	if t_charge_id is null then

		-- add annual fee to charges
		INSERT INTO `m_charge` (`name`, `currency_code`, `charge_applies_to_enum`, `charge_time_enum`, `charge_calculation_enum`, `charge_payment_mode_enum`, `amount`, `is_penalty`, `is_active`, `is_deleted`) VALUES (t_annual_fee_name, v_currency_code, 2, 6, 1, 0, v_annual_fee_amount_charge_def, 0, 1, 0);

		-- get inserted charge id
		set t_charge_id = last_insert_id();

	end if;

	open cursor2;
        LOOP2: loop
		fetch cursor2 into v_account_id, v_annual_fee_amount, v_annual_fee_on_month, v_annual_fee_on_day, v_annual_fee_next_due_date;

		if no_more_rows then
		set no_more_rows := false;
		close cursor2;
		leave LOOP2;
            end if;

            if not exists (select id from m_savings_account_charge sac where
		sac.savings_account_id=v_account_id and sac.charge_id=t_charge_id and sac.charge_time_enum=6) then

	            -- attach annual charge to savings
	            INSERT INTO `m_savings_account_charge` (`savings_account_id`, `charge_id`, `is_penalty`, `charge_time_enum`, `charge_due_date`, `fee_on_month`, `fee_on_day`, `charge_calculation_enum`, `calculation_percentage`, `calculation_on_amount`, `amount`, `amount_paid_derived`, `amount_waived_derived`, `amount_writtenoff_derived`, `amount_outstanding_derived`, `is_paid_derived`, `waived`, `is_active`) VALUES (v_account_id, t_charge_id, 0, 6, v_annual_fee_next_due_date, v_annual_fee_on_month, v_annual_fee_on_day, 1, NULL, NULL, v_annual_fee_amount, NULL, NULL, NULL, v_annual_fee_amount, 0, 0, 1);

	            -- set savings account charge id
	            set t_savings_charge_id = last_insert_id();

	        else

			set t_savings_charge_id = (select id from m_savings_account_charge sac where sac.savings_account_id=v_account_id and sac.charge_id=t_charge_id and sac.charge_time_enum=6);

	        end if;

            open cursor3;
            LOOP3: loop
		fetch cursor3 into v_savings_transaction_id, v_transaction_amount;

			if no_more_rows then
			set no_more_rows := false;
			close cursor3;
			leave LOOP3;
	            end if;

	            if not exists (select id from m_savings_account_charge_paid_by sacp where
		sacp.savings_account_transaction_id=v_savings_transaction_id and sacp.savings_account_charge_id=t_savings_charge_id) then

		            -- insert a record into savings account charge paid by
		            INSERT INTO `m_savings_account_charge_paid_by` (`savings_account_transaction_id`, `savings_account_charge_id`, `amount`) VALUES(v_savings_transaction_id, t_savings_charge_id, v_transaction_amount);

		        end if;

	        end loop LOOP3;
        end loop LOOP2;
    end loop LOOP1;
end //

delimiter ;

call migrate_withdrwal_fees();
call migrate_annual_fees();

drop procedure if exists migrate_annual_fees;
drop procedure if exists migrate_withdrwal_fees;


ALTER TABLE `m_savings_account`
	DROP COLUMN `annual_fee_amount`,
	DROP COLUMN `annual_fee_on_month`,
	DROP COLUMN `annual_fee_on_day`,
	DROP COLUMN `annual_fee_next_due_date`,
	DROP COLUMN `withdrawal_fee_amount`,
	DROP COLUMN `withdrawal_fee_type_enum`;

ALTER TABLE `m_savings_product`
	DROP COLUMN `annual_fee_amount`,
	DROP COLUMN `annual_fee_on_month`,
	DROP COLUMN `annual_fee_on_day`;
