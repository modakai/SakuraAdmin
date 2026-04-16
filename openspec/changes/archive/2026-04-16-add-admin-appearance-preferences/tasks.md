## 1. Preference Model

- [x] 1.1 Define the admin appearance preference type, defaults, and option constants for color mode, theme color, radius, font, density, layout, navigation, sidebar state, motion, table stripes, breadcrumbs, and page title visibility.
- [x] 1.2 Consolidate or bridge existing `theme` and `sidebar-config` stores so admin appearance preferences have one authoritative state source.
- [x] 1.3 Add reset-to-default behavior that restores all preference fields and updates persisted local state.

## 2. Preference Application

- [x] 2.1 Implement a composable that applies preferences to `document.documentElement` classes and CSS variables.
- [x] 2.2 Support `system` color mode by reacting to browser or operating system color scheme changes.
- [x] 2.3 Apply font, density, reduced-motion, breadcrumbs, page title, and table stripe flags through stable classes or exported reactive state.
- [x] 2.4 Ensure the application logic is initialized only for management-side layouts or pages.

## 3. Configuration UI

- [x] 3.1 Replace the current appearance settings form dependency on server system config with direct local preference store reads and writes.
- [x] 3.2 Build grouped controls for theme, layout, navigation, and display-effect preferences using existing shadcn-vue UI components.
- [x] 3.3 Add a reset-to-default action with clear disabled/loading states where applicable.
- [x] 3.4 Keep the existing custom theme popover as a shortcut for high-frequency options and wire it to the same preference state source.
- [x] 3.5 Extend theme palettes with the shadcn-vue Colors Tailwind color families and localized labels.

## 4. Navigation And Scope

- [x] 4.1 Add a management navigation entry for the appearance preference configuration if the existing settings entry is not sufficient.
- [x] 4.2 Confirm user-side pages do not import or initialize admin appearance preference application logic.
- [x] 4.3 Remove or isolate obsolete appearance usage of `system-config.api.ts` without breaking unrelated callers.

## 5. Verification

- [x] 5.1 Add focused unit tests for default preferences, local persistence, update behavior, reset behavior, and DOM application logic.
- [x] 5.2 Add component tests for the appearance preference form and key controls.
- [x] 5.3 Run frontend type checking and the relevant Vitest suite.
- [ ] 5.4 Manually verify in browser that changing preferences applies immediately and survives refresh.
