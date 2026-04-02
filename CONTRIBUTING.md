# Contributing to AI Support System

Thanks for your interest in contributing.

## How to Contribute

1. Fork the repository on GitHub.
2. Clone your fork locally.
3. Create a feature/fix branch:
   - `git checkout -b feature/your-feature-name`
   - or `git checkout -b fix/your-bug-fix`
4. Make your changes and keep commits focused.
5. Run relevant tests before opening PR:
   - See [TESTING.md](TESTING.md) for commands.
6. Push your branch and open a Pull Request to `main`.

## Contribution Guidelines

- Follow existing architecture patterns (event-driven flow, outbox, service boundaries).
- Keep changes scoped and avoid unrelated refactors in the same PR.
- Add/update tests for behavior changes.
- Update docs when behavior, APIs, or architecture changes:
  - `README.md`
  - `ARCHITECTURE.md`
  - `OVERVIEW.md`
  - Service-level README files where relevant

## Feature Checklist

If you add a new feature, verify:

- Endpoint/controller changes are documented and tested.
- Kafka producers/consumers are updated where needed.
- Database entities/migrations are handled.
- OpenAPI docs remain accurate.
- Integration points (gateway/routes/events) are validated.

## Pull Request Expectations

- Clear title and summary.
- Link issue/ticket if available.
- Mention testing performed.
- Keep PR review-friendly (small/medium scope preferred).

## Reporting Issues

For bugs or improvements, open a GitHub issue with:

- What happened
- Expected behavior
- Steps to reproduce
- Logs/screenshots if applicable
