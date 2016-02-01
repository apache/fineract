/***Work around for issues on non Unicode safe connections**/
UPDATE `m_currency` SET `display_symbol`=CHAR(226, 130, 185) WHERE  `id`=64;

/***Update organization currencies if applicable***/
update m_organisation_currency set display_symbol =CHAR(226, 130, 185) where code = "INR";