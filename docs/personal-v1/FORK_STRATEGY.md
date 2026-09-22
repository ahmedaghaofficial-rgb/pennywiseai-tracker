# Fork Strategy

## Source
Upstream project:
`sarim2000/pennywiseai-tracker`

This repository remains a GitHub fork so upstream changes can be reviewed and adopted later.

## Branch model
- `main`: stable version
- `setup/personal-v1`: initial product setup
- `feature/*`: one focused feature per branch
- `fix/*`: focused fixes

## Avoid
- Large direct changes to `main`
- Mixing unrelated features in one branch
- Large rewrites without a proven user need
- Reusing PennyWise branding in a distributed derivative

## Licensing
The codebase is AGPL-3.0.
Any distributed derivative must comply with that license.
PennyWise names, logos, and brand assets are not granted for reuse by the source-code license.
