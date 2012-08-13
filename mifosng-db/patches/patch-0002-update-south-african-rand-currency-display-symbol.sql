UPDATE `ref_currency`
SET
`display_symbol` = 'R'
WHERE id=161;


UPDATE `org_organisation_currency`
SET
`display_symbol` = 'R'
WHERE code like 'ZAR';