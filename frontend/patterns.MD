# 🌐 Frontend Architecture & Design Patterns

*A complete guide (React-focused) — GitHub Markdown*

---

## 📌 Table of Contents

1. [Why frontend patterns matter](#why-frontend-patterns-matter)
2. [Classical UI patterns](#classical-ui-patterns)

   * MVC
   * MVP
   * MVVM
3. [Modern frontend patterns](#modern-frontend-patterns)

   * Component-Driven Architecture
   * Flux / Redux
   * Unidirectional Data Flow
   * Reactive State (Hooks)
4. [Large-scale patterns](#large-scale-patterns)

   * Microfrontends
   * Atomic Design
   * Feature-Based Architecture
5. [How these patterns map to React](#how-these-patterns-map-to-react)
6. [Which pattern should you use?](#which-pattern-should-you-use)
7. [Quick reference table](#quick-reference-table)

---

# ⭐ Why frontend patterns matter

Frontend code becomes unmaintainable quickly when:

* Components become massive
* No separation between UI & logic
* State is scattered everywhere
* Architecture grows randomly

Patterns help you build:

* **Scalable** apps
* **Predictable** behavior
* **Reusable** components
* **Easy-to-test** logic
* **Standardized** structure

---

# 🎯 Classical UI Patterns

These originated before SPAs existed, but they still influence modern frontend.

---

## 1️⃣ MVC — Model–View–Controller

### **Concept**

```
Model       → Data + business logic  
View        → UI that displays data  
Controller  → Handles user input, decides actions
```

### **Usage in frontend**

Earlier JS frameworks (Backbone, AngularJS 1.x) applied MVC directly.

### **Does React use MVC?**

Not strictly.
React is **only the View**, while Model & Controller are implemented by:

* State management (Redux, MobX, Zustand)
* Custom Hooks
* API services

### **When MVC is useful**

* Apps where responsibilities must be very cleanly separated
* Legacy ecosystems (Backbone, jQuery era)

---

## 2️⃣ MVP — Model–View–Presenter

### **Concept**

```
View  ↔ Presenter ↔ Model
```

### Key idea

* **Presenter** contains all logic
* **View** is passive (just renders)

### In frontend

Used by:

* Android UI architecture
* Some legacy JS apps

### In React?

Rarely used.
React components already combine View + logic cleanly.

---

## 3️⃣ MVVM — Model–View–ViewModel (Recommended for modern UI)

### **Concept**

```
Model       → API + state logic  
View        → Components  
ViewModel   → Reactive data binding (state, hooks)
```

### Why it fits React perfectly

* Components = **View**
* Hooks = **ViewModel**
* API services = **Model**

### Example mapping in React

```
/components/UserList.jsx → View  
/hooks/useUsers.js       → ViewModel  
/services/user.api.js    → Model  
```

### Benefits

* Clear separation
* Reusable logic via hooks
* Easy to test

---

# 🚀 Modern Frontend Patterns (React era)

---

## 4️⃣ Component-Driven Architecture (CDA)

### Philosophy

“Build UIs from small reusable components upward.”

### Tools

* Storybook
* Design Systems
* UI libraries

### Benefits

* Scalable
* Reusable
* Independent UI testing

---

## 5️⃣ Flux Architecture (Unidirectional Data Flow)

```
Action → Dispatcher → Store → View → (back to Action)
```

React originally adopted Flux through:

### Redux

* Single state tree
* Pure reducers
* Predictable updates

### Recoil / Zustand

Modern alternatives that still follow **unidirectional flow**.

### When useful

* Large apps
* Complex shared state

---

## 6️⃣ Hooks + Reactive State Pattern (React-native pattern)

This is the **new default** in React apps.

### Characteristics

* Logic extracted into custom hooks
* Components subscribe to state reactively
* Derived values using memo & refs

### Example

```js
function useCart() {
  const [items, setItems] = useState([]);
  const total = useMemo(() => items.reduce(...), [items]);
  return { items, total, addItem, removeItem };
}
```

---

# 🏗 Large-Scale Frontend Architecture Patterns

---

## 7️⃣ Feature-Based Architecture (Most popular in real apps)

Instead of grouping by file type, group by **feature**.

```
src/
  features/
    users/
      UserList.jsx
      UserCard.jsx
      useUsers.js
      user.api.js
```

### Benefits

* Clear boundaries
* Easy to scale
* Follows MVVM naturally

---

## 8️⃣ Atomic Design (Design System Pattern)

Five layers:

```
Atoms → Molecules → Organisms → Templates → Pages
```

### Example

* Atom = Button
* Molecule = SearchBox
* Organism = Navbar

Used heavily in design-system-driven teams.

---

## 9️⃣ Microfrontends (Enterprise-level)

Split frontend into independent apps:

```
app-shell/
user-app/
orders-app/
payments-app/
```

### Tools

* Module Federation
* single-spa
* Web Components

### When to use

* Large organizations
* Independent deployments per team

---

# 🔄 How These Patterns Map to React

| Pattern                    | React Equivalent                                        |
| -------------------------- | ------------------------------------------------------- |
| MVC                        | Components (View) + Hooks/Services (Controller/Model)   |
| MVP                        | Presenter logic sometimes moved to container components |
| **MVVM**                   | **View = JSX, ViewModel = Hooks, Model = APIs**         |
| Flux/Redux                 | Global unidirectional state                             |
| Component Architecture     | React’s core philosophy                                 |
| Atomic Design              | Component library structure                             |
| Feature-based architecture | Modern scalable React folder structure                  |
| Microfrontends             | Module Federation, single-spa                           |

---

# ✔ Which pattern should YOU use?

### For **small/medium React apps**

* MVVM (hooks as ViewModel)
* Component-driven architecture
* Feature-based folders

### For **large React apps**

* MVVM
* Flux/Redux or Zustand
* Design system (Atomic Design)
* Microfrontends (if multiple teams)

---

# 📝 Quick Reference Table

| Pattern                        | Frontend? | React?               | Use Case                       |
| ------------------------------ | --------- | -------------------- | ------------------------------ |
| **MVC**                        | Yes       | Partial              | Apps needing strict separation |
| **MVP**                        | Yes       | Rare                 | Thin-view, test-heavy apps     |
| **MVVM**                       | Yes       | ⭐ Best fit           | Modern SPAs                    |
| **Flux/Redux**                 | Yes       | Yes                  | Complex global state           |
| **Component Architecture**     | Yes       | ⭐ Core               | All React apps                 |
| **Atomic Design**              | Yes       | Yes                  | Design systems                 |
| **Feature-Based Architecture** | Yes       | ⭐ Highly recommended | Scalable apps                  |
| **Microfrontends**             | Yes       | Yes                  | Enterprise-level apps          |

---



# Front-end design patterns, standards & verification — **Full guide (React-focused)**

> A single-file GitHub-style markdown doc that explains common front-end design patterns, shows practical React examples, and lists tools & checks to ensure your front-end is standardized, scalable and maintainable.

---

## Table of contents

1. [Principles & goals](#principles--goals)
2. [High-level design patterns (front-end)](#high-level-design-patterns-front-end)
3. [React-specific patterns (with examples)](#react-specific-patterns-with-examples)
4. [Scalability patterns and architectures](#scalability-patterns-and-architectures)
5. [Standards, style & developer workflow (how to enforce)](#standards-style--developer-workflow-how-to-enforce)
6. [Verification, testing & observability tools](#verification-testing--observability-tools)
7. [Checklist you can use in PRs / code reviews](#checklist-you-can-use-in-prs--code-reviews)
8. [Recommended starter config (opinionated)](#recommended-starter-config-opinionated)
9. [Further reading / references](#further-reading--references)

---

## Principles & goals

Before picking patterns or tools, agree team goals. The usual ones:

* **Maintainability** — easy to understand & modify.
* **Composability** — small reusable components.
* **Testability** — components can be unit/ integration tested in isolation.
* **Performance** — lazy loading, code splitting, small bundles.
* **Observability/Operability** — runtime errors, metrics, logs.
* **Accessibility (a11y)** — keyboard focus, screen-reader friendly, semantic markup.

Design patterns and tools exist to help reach these goals; pick the ones that map to the project's constraints (team size, release cadence, legacy code).

---

## High-level design patterns (front-end)

These are cross-framework concepts that help structure UI code:

* **Component-driven development (CDD)** — build UI from small components upward; each component is a unit of functionality and documentation/test. (Storybook is the canonical tool here). ([Storybook][1])
* **Container / Presentational (Smart / Dumb)** — containers fetch/compose data; presentational components receive props and render UI. Useful for separation of concerns. ([Medium][2])
* **Feature / Module scoping** — group files by feature rather than type (`feature/Component`, `feature/hooks`, `feature/styles`) for large apps.
* **Domain-driven components & design systems** — shared design primitives (buttons, inputs) kept in a design system repo (or package) to ensure consistency. Storybook + design tokens help. ([Storybook][3])
* **State management patterns** — local state (hooks), global state (Redux Toolkit, Zustand, Recoil), server state (React Query / TanStack Query). Choose based on data lifetime & complexity.
* **Microfrontends** — split big UI into smaller independently deployable frontends (single-spa, Module Federation). Useful at org scale. ([Single SPA][4])

---

## React-specific patterns (with examples)

Below are the most useful React patterns, when to use them and minimal examples.

> **Important**: patterns are *tools*, not rules. Avoid premature abstraction.

### 1) **Function Components + Hooks (recommended default)**

Use hooks for state, effects and logic composition.

```jsx
// Counter.jsx
import React, { useState, useEffect } from 'react';

export default function Counter({ start = 0 }) {
  const [n, setN] = useState(start);
  useEffect(() => {
    const id = setInterval(() => setN(x => x + 1), 1000);
    return () => clearInterval(id);
  }, []);
  return <div>Count: {n}</div>;
}
```

### 2) **Container / Presentational**

Use when you want separation of data concerns and pure UI.

```jsx
// UserCard.presentational.jsx
export function UserCard({ name, email, onClick }) {
  return <div onClick={onClick}><h3>{name}</h3><p>{email}</p></div>;
}

// UserCard.container.jsx
import { UserCard } from './UserCard.presentational';
import { useUser } from './useUser';
export default function UserCardContainer({ userId }) {
  const { user, loading } = useUser(userId);
  if (loading) return <div>loading...</div>;
  return <UserCard name={user.name} email={user.email} onClick={() => console.log(userId)} />;
}
```

### 3) **Compound Components**

Group related components — useful for building small libraries (like `<Form>` with `<Form.Field>` children).

```jsx
// Tabs.js (simplified)
export function Tabs({ children }) {
  // internal state and context to share with Tabs.* children
}
Tabs.Tab = function Tab({ children }) { /* uses parent context */ };
Tabs.Panel = function Panel({ children }) { /* shows active panel */ };
```

### 4) **Provider / Context pattern**

For sharing cross-cutting concerns (theme, auth). Keep Contexts small and stable.

```jsx
// ThemeContext.js
import React from 'react';
export const ThemeContext = React.createContext({ color: 'blue' });
```

### 5) **Custom Hooks (logic extraction)**

When multiple components share the same stateful logic.

```js
// usePagination.js
import { useState } from 'react';
export function usePagination(initial = 1) {
  const [page, setPage] = useState(initial);
  return { page, next: () => setPage(p => p + 1), prev: () => setPage(p => Math.max(1, p-1)) };
}
```

### 6) **Higher-Order Components (HOC)** *(use sparingly)*

HOCs wrap components to add behavior (legacy pattern, often replaced by hooks).

```jsx
function withLogging(Component) {
  return function Wrapped(props) {
    useEffect(() => { console.log('mount', props); }, []);
    return <Component {...props} />;
  }
}
```

### 7) **Render Props** *(legacy; sometimes handy)*

Pass a function prop to control rendering.

```jsx
<DataLoader render={(data) => <ul>{data.map(...)} </ul>} />
```

### 8) **Presentation / performance patterns**

* **Memoization** (`React.memo`, `useMemo`, `useCallback`) for expensive calculations.
* **Virtualization** (react-window, react-virtualized) for large lists.
* **Lazy loading & Suspense** for route and component splitting.

---

## Scalability patterns and architectures

* **Code splitting / dynamic imports** — split bundles per route or component to reduce initial payload.
* **Server-side rendering / hybrid rendering** — Next.js, Remix for SEO and faster perceived load.
* **Edge/CDN + caching strategy** — public assets on CDN; use HTTP caching and stale-while-revalidate patterns.
* **Microfrontends** — split by team/feature, using approaches like **single-spa** or Webpack **Module Federation**. Use when teams need independent deployment and isolation. ([Single SPA][4])
* **Design System + Component Library** — centralized UI primitives, versioned and distributed as packages (monorepo or package registry). Storybook helps document & test. ([Storybook][3])

---

## Standards, style & developer workflow (how to enforce)

Adopt automated tooling and guardrails. Recommended layers:

1. **Code style & linting**

   * `ESLint` + `eslint-plugin-react` / `eslint-plugin-jsx-a11y` — enforce best practices and catch bugs.
   * `Prettier` — code formatting. Integrate with ESLint (Prettier rules as ESLint plugin) so CI passes only when code is formatted. ([LogRocket Blog][5])

2. **CSS/style rules**

   * `Stylelint` for CSS. Consider CSS Modules, BEM, or utility-first (Tailwind) depending on team preference.
   * Design tokens & shared tokens for colors/spacing.

3. **Type safety**

   * **TypeScript** for most non-trivial codebases — catches many classes of bugs at compile time.

4. **Component-driven development**

   * Use **Storybook** to develop, test, and document components in isolation. Storybook becomes living documentation. ([Storybook][1])

5. **Testing**

   * Unit: Jest + React Testing Library (focus on behavior, not implementation).
   * Integration / E2E: Cypress or Playwright.
   * Visual regression: Chromatic (Storybook) or Percy.

6. **PR / CI workflows**

   * Running linting, type checks, tests, storybook build, bundle-size check and Lighthouse CI in CI pipelines (GitHub Actions / GitLab CI).
   * Enforce protected branches & required checks.

7. **Accessibility**

   * `eslint-plugin-jsx-a11y` + axe-core automated checks in CI + manual/audit checks with Lighthouse or axe browser extension.

---

## Verification, testing & observability tools

Below are the practical tools you should use to verify standardization, performance, accessibility and scalability.

### Quality & Style

* **ESLint** — linting & best-practice rules. ([LogRocket Blog][5])
* **Prettier** — code formatting. ([Better Stack][6])
* **Stylelint** — CSS linting.

### Component dev & docs

* **Storybook** — component dev, docs and visual tests. Great for CDD and design systems. ([Storybook][1])

### Testing

* **Jest + React Testing Library** — unit and component tests.
* **Cypress / Playwright** — E2E testing.
* **Chromatic / Percy** — visual regression testing integrated with Storybook.

### Performance & bundle

* **Lighthouse** — audits for performance, accessibility, SEO and PWA. Use in CI for regression. ([Chrome for Developers][7])
* **Webpack Bundle Analyzer / source-map-explorer** — inspect bundle composition and size.
* **bundle size budgets** — fail CI if bundles grow above thresholds.

### Accessibility (a11y)

* **axe-core / axe-webpack-plugin** — automated accessibility checks.
* **Pa11y** — accessibility testing tool.

### Observability & errors

* **Sentry / LogRocket** — runtime error tracking and session replay.
* **Application Performance Monitoring (APM)** — Datadog, New Relic for frontend metrics.

### Security & dependencies

* **Snyk / npm audit / Dependabot / Renovate** — dependency vulnerability scanning and automated updates.

### Scalability & architecture

* **single-spa / Module Federation** — microfrontend orchestration and shared dependency strategies. ([Single SPA][8])

---

## Checklist you can use in PRs / code reviews

(Use this checklist as a checklist file in each repo)

* [ ] Does the code have a clear single responsibility?
* [ ] Is the new UI component documented in Storybook with edge states?
* [ ] Unit tests exist for business logic and behavior (React Testing Library).
* [ ] E2E tests added if the change affects flows.
* [ ] Lint & TypeScript checks pass in CI.
* [ ] Prettier formatting applied.
* [ ] Accessibility: `aria-*` attributes / semantic tags present; axe checks pass.
* [ ] Performance: bundle size checked; no heavy libs added without justification.
* [ ] No secrets or credentials in code.
* [ ] Observability: logged meaningful errors; Sentry events created where needed.
* [ ] Release notes or changelog updated (for public components / API changes).

---

## Recommended starter config (opinionated)

A quick list of dependencies & minimal `.github/workflows/ci.yml` steps.

**Dev dependencies**

```
eslint prettier eslint-config-prettier eslint-plugin-react eslint-plugin-jsx-a11y typescript jest @testing-library/react cypress storybook webpack-bundle-analyzer source-map-explorer axe-core stylelint
```

**CI steps**

1. `npm ci`
2. `npm run lint` (ESLint + Stylelint)
3. `npm run type-check`
4. `npm test` (unit)
5. `npm run build-storybook` (optionally publish)
6. `npm run bundle-analyze` (check size)
7. `lighthouse-ci` run against a test deployment (or use Lighthouse in CI). ([Chrome for Developers][7])

---

## Example: Small directory layout for scale

```
/src
  /features
    /auth
      AuthPage.jsx
      LoginForm.jsx
      useAuth.js
      auth.css
      auth.stories.jsx
  /components
    /ui
      Button.jsx
      Button.stories.jsx
  /hooks
    useDebounce.js
  /services
    api.js
  index.tsx
```

Group by feature; keep `components/ui` for primitives; `stories` close to components.

---

## Practical tips & anti-patterns

**Do**

* Prefer small, focused components.
* Use TypeScript for public APIs.
* Automate checks in CI.
* Use Storybook early; treat it as documentation.

**Don't**

* Over-abstract: avoid creating abstractions with only one consumer.
* Use global mutable state for everything.
* Ship large monolithic bundles without code-splitting.
* Skip accessibility — it's cheaper to design accessibly from the start.

---

## Further reading / references

* React design patterns articles & guides (patterns list & when to use). ([Trio][9])
* Storybook (Component-Driven Development & docs). ([Storybook][1])
* Microfrontends and single-spa / Module Federation overview. ([Single SPA][4])
* Lighthouse — automated audits for performance & accessibility. ([Chrome for Developers][7])
* ESLint + Prettier integration guides (style & consistency). ([LogRocket Blog][5])

---

## Quick start checklist (copy-paste to README)

```md
### Frontend code quality (must haves)
- [ ] ESLint configured + CI lint check
- [ ] Prettier configured + format on save
- [ ] TypeScript enabled (if project uses TS)
- [ ] Storybook running with stories for all components
- [ ] Jest + React Testing Library tests for components
- [ ] Lighthouse perf & a11y checks integrated in CI
- [ ] Bundle analyzer configured
- [ ] Sentry (or equivalent) integrated for runtime errors
```

---

