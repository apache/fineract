/*
Note: its possible that this script wont apply on windowns through the command line client. The UTF-8 value £ isnt inserted.
In this case you can open up an sql tool like MySql workbench and apply this script in total.
*/
UPDATE `ref_currency` SET `display_symbol` = 'BND' where `id`=18;
UPDATE `ref_currency` SET `display_symbol` = 'GHC' where `id`=51;
UPDATE `ref_currency` SET `display_symbol` = 'KSh' where `id`=71;
UPDATE `ref_currency` SET `display_symbol` = 'DT' where `id`=140;
UPDATE `ref_currency` SET `display_symbol` = '$' where `id`=148;
UPDATE `mifosngprovider`.`ref_currency` SET `display_symbol`='L£' WHERE `id`='81';