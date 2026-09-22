# First Execution Brief

## Objective
Establish a verified baseline before adding or removing functionality.

## Allowed in this phase
- Documentation
- Branch / PR workflow
- Build verification
- Test verification
- Identifying PennyWise-specific commercial/branding components for later isolation

## Not allowed yet
- New features
- Database changes
- Parser changes
- UI redesign
- Product-logic changes
- Aggressive deletion of billing/licensing code before dependency impact is understood

## Definition of Done
- CI tests pass
- APK build succeeds
- App behavior remains identical to upstream
- No user data path is changed
- A clear next-step list exists for creating the personal build
