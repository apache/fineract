-- example of dropping fund column/index/foreign key 
-- alter table `mifosngprovider`.`portfolio_loan` 
-- DROP FOREIGN KEY `FK7C885877240145`,
-- DROP INDEX `FK7C885877240145`,
-- drop column `fund_id`;

-- associate fund table with loans making fund optional at database level
alter table `mifosngprovider`.`portfolio_loan`
add column `fund_id` bigint(20) DEFAULT NULL,
add KEY `FK7C885877240145` (`fund_id`),
add CONSTRAINT `FK7C885877240145` FOREIGN KEY (`fund_id`) REFERENCES `org_fund` (`id`);