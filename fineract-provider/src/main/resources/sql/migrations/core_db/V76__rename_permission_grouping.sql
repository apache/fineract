/* break-out center and group permissions from porfolio grouping */

update m_permission
set grouping = "portfolio_center"
where code like '%center%'
and grouping like 'portfolio';


update m_permission
set grouping = "portfolio_group"
where code like '%group%'
and grouping like 'portfolio';