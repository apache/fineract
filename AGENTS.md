# Agent guidance

This file is read by automated agents (security scanners, code
analyzers, AI assistants) operating on this repository. It
points them at the human-authored references they should
consult before producing output.

## Security

Security model: [SECURITY.md](./SECURITY.md)

Agents that scan this repository should consult `SECURITY.md`
for the project's threat model, in-scope / out-of-scope
declarations, and known non-findings before reporting issues.

## Command processing (write requests)

Write requests are being moved from the legacy command
pipeline to the typed command infrastructure in
[`fineract-command`](./fineract-command) (FSIP-5). Both styles
exist in the code today and the legacy one is still far more
common. In a feature whose write requests already go through
`CommandDispatcher`, keep to that style and do not add legacy
handlers next to it.

The new style uses:

- request and response DTOs in the feature's `data` package
- a `Command<Request>` subclass in its `command` package
- a `CommandHandler<Request, Response>` in its `handler`
  package
- a REST resource that injects `CommandDispatcher`, sets the
  request as the command payload and returns
  `dispatcher.dispatch(command).get()`
- business services that take the typed request, never a
  `JsonCommand`

It does not use the legacy API: `NewCommandSourceHandler`,
`@CommandType`, `CommandWrapper`, `CommandWrapperBuilder`,
`PortfolioCommandSourceWritePlatformService`, `JsonCommand` or
`FromJsonHelper`.

Reference implementations (copy the annotations on their
handlers as well):

- payment types:
  `fineract-core/src/main/java/org/apache/fineract/portfolio/paymenttype/`
- documents and images:
  `fineract-document/src/main/java/org/apache/fineract/infrastructure/documentmanagement/`

Background and instructions:

- [fineract-command/REFACTORING.md](./fineract-command/REFACTORING.md):
  how to migrate a feature
- [fineract-command/README.md](./fineract-command/README.md):
  why the new infrastructure exists
- `fineract-doc/src/docs/en/chapters/command/`: architecture,
  dispatcher and handlers
- [FSIP-5](https://cwiki.apache.org/confluence/spaces/FINERACT/pages/340036085/FSIP-5+New+Command+Processing+Infrastructure)
  on the project wiki

Where the documentation and the reference implementations
disagree, follow the reference implementations.
