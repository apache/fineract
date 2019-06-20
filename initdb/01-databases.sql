# create databases
CREATE DATABASE IF NOT EXISTS `mifosplatform-tenants`;
CREATE DATABASE IF NOT EXISTS `mifostenant-default`;

# create root user and grant rights
GRANT ALL ON *.* TO 'root'@'%';