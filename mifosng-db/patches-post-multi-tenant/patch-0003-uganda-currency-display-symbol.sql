-- update display symbol for Ugandain shilling.

UPDATE `ref_currency` SET `display_symbol` = 'USh' WHERE code='UGX';
UPDATE `org_organisation_currency` SET `display_symbol` = 'USh' WHERE code='UGX';
