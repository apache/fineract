-- update display symbol for myanmar kyat.

UPDATE `ref_currency` SET `display_symbol` = 'K' WHERE id=92;

UPDATE `org_organisation_currency` SET `display_symbol` = 'K' WHERE id=92;
