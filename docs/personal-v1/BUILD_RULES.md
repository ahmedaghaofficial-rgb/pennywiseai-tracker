# Build Rules

1. Do not rebuild working core functionality.
2. New features should be isolated and removable where practical.
3. No database schema change without a migration and tests.
4. No parser change without sample-based tests.
5. No full UI redesign before sustained daily usage.
6. Do not turn this product into an ERP.
7. Design for future expansion without building unused complexity.
8. Every phase must end with an installable, usable build.
9. Any change that makes upstream updates harder needs a documented reason.
10. Priority order: Reliability > Automation > UX polish > New features.
11. Keep `main` stable; meaningful changes go through a branch and PR.
12. Prefer one focused feature or concern per branch.
