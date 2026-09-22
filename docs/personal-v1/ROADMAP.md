# Personal v1 Roadmap

## Phase 0 — Clean Base
Goal: preserve PennyWise behavior while establishing our own safe development path.

- Work only through branches and PRs
- Keep the fork relationship with upstream
- Do not change database schema
- Do not change parsers
- Do not redesign the UI
- Do not add features yet
- Validate tests and APK build

Definition of done:
- Existing tests pass
- APK builds
- Core behavior remains unchanged
- Upstream updates remain easy to consume

## Phase 1 — Daily-use Core
Use the existing app for:
- Personal / Business
- Accounts
- Manual transactions
- SMS / notifications
- Categories / tags
- Search
- Backup / restore

Success:
The app can replace spreadsheets for day-to-day money tracking.

## Phase 2 — My Egypt Sources
Add only sources actually needed in real use:
- Banks
- Wallets
- Relevant notification sources

Success:
Most transactions arrive automatically.

## Phase 3 — Automation
Improve:
- Smart rules
- Auto categorization
- Auto profile assignment
- Deduplication reliability

Success:
80–90% of transactions require no manual organization.

## Phase 4 — Better Capture
Only after real usage proves the need:
- Screenshot attachment
- Receipt attachment
- OCR
- User-defined parsing rules

## Phase 5 — Stability
Must prove:
- No duplicate transactions
- Backup and restore work
- Updates preserve data
- Moving to a new phone is safe
- Offline usage is reliable
- Financial data is protected

## Phase 6 — Personal Production
Use it as the primary personal finance app.

Classify every problem as:
1. Must Fix
2. Friction
3. Nice to Have

Build in that order.
