latam Demo
======

This demo database contains:

- DDL of latest schema
- Minimum reference data required for deployment of platform which is:
  -  Its mandatory to have one selected currency so we default to several of latin-america currencies
  -  Its mandatory to have one root or head office, so we have one created by default called a 'Latam HO'
  -  Permissions supported/needed by latest release of software are setup
  -  Its mandatory to have at least one role when creating new users so we have one role created by default called 'Super user' which has the special permission 'Full Authorisation'. Any user with this role can do anything in the system.
  -  Its required to have at least one application user setup so remaining setup can be done through ui so we have on application user created by default with username 'quipo' with a password of 'quipo'. Application users must be associated with an office and a role so this user is associated with 'Latam HO' and 'Super user' role allowing this user to do any operation in any office(branch).
- Configuration
  - No 'additional data' through the 'datatables' approach is setup
  - One 'code' is setup called 'Client Identifier' with default values of {'Passport number'} - (required for Client Identity Document functionalty)
  - Enable/Disable configuration has one entry named 'maker-checker' to allow people to enable disable this feature at global level. It is off or disabled by default.