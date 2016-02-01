ALTER TABLE `m_product_loan`
	ADD COLUMN `short_name` VARCHAR(4) NULL DEFAULT NULL AFTER `id`;
update m_product_loan set short_name=concat(LEFT(name,2), RIGHT(id,2)) where short_name is null;
ALTER TABLE `m_product_loan`
	ALTER `short_name` DROP DEFAULT;
ALTER TABLE `m_product_loan`
	CHANGE COLUMN `short_name` `short_name` VARCHAR(4) NOT NULL AFTER `id`;
ALTER TABLE `m_product_loan`
	ADD UNIQUE INDEX `unq_short_name` (`short_name`);

ALTER TABLE `m_savings_product`
	ADD COLUMN `short_name` VARCHAR(4) NULL DEFAULT NULL AFTER `name`;
update m_savings_product set short_name=concat(LEFT(name,2), RIGHT(id,2)) where short_name is null;
ALTER TABLE `m_savings_product`
	ALTER `short_name` DROP DEFAULT;
ALTER TABLE `m_savings_product`
	CHANGE COLUMN `short_name` `short_name` VARCHAR(4) NOT NULL AFTER `name`,
	ADD UNIQUE INDEX `sp_unq_short_name` (`short_name`);
