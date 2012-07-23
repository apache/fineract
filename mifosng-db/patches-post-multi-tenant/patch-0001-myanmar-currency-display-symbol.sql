-- update display symbol for myanmar kyat.

UPDATE `ref_currency` SET `display_symbol` = 'K' WHERE code='MMK';
UPDATE `org_organisation_currency` SET `display_symbol` = 'K' WHERE code='MMK';
