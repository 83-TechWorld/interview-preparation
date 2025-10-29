Here’s a **comprehensive guide** covering *latest interview-friendly questions* in React (2025), **custom hooks with examples**, **performance tools & how to fix issues**, and **useful extensions** for React development.
Since your background includes React + TypeScript + full-stack work, I’ll include some more advanced items too.

---

## 1. Latest React Interview Questions & Sample Answers

Here are some up-to-date (2025-oriented) React questions with example answers and code where useful.
You can use these both for interview prep and for internal team knowledge sharing.

### Basic / Core Concepts

1. **What is React? What are its advantages?**

   * React is a JavaScript library for building user-interfaces, especially single-page apps. ([GeeksforGeeks][1])
   * Advantages include component-based architecture, declarative UI, virtual DOM for efficient updates, large ecosystem.
2. **What is JSX and how is it converted?**

   * JSX is a syntax extension: you write HTML-like code in JS. ([GeeksforGeeks][1])
   * Under the hood it is compiled (e.g., by Babel) into `React.createElement(...)` calls. ([GeeksforGeeks][1])
3. **Explain Functional Components vs Class Components.**

   * Class components extend `React.Component`, use `this.state`, lifecycle methods, etc.
   * Functional components are plain functions; with Hooks (from React 16.8+) they can have state & side-effects. ([DEV Community][2])
4. **What is the Virtual DOM and how does React use it?**

   * Virtual DOM: an in-memory representation of the real DOM; React builds a tree, diff’s it, updates only necessary parts. ([GeeksforGeeks][1])
5. **What are props vs state?**

   * *props* are passed from parent to child; read-only in child.
   * *state* is local to component (or via hooks), can change over time, triggers re-render.
6. **What are keys in lists and why are they important?**

   * Keys help React identify changed, added or removed items in lists so it can optimize updates. ([DEV Community][2])
   * Using array indices as keys is often an anti-pattern because it can lead to wrong UI behavior on dynamic lists. ([GitHub][3])

### Hooks & Intermediate Concepts

7. **What are Hooks in React and why were they introduced?**

   * Hooks are special functions (e.g., `useState`, `useEffect`, `useContext`, etc) that let you use state & other React features in functional components. ([GeeksforGeeks][1])
   * They simplify code by avoiding class complexity, make reuse easier, promote composition.
8. **Rules of Hooks — what are they?**

   * Only call Hooks at the top level of a React function (not inside loops/conditions) and only from React function components or custom hooks. ([DEV Community][2])
9. **What’s the difference between `useEffect` and `useLayoutEffect`?**

   * `useEffect` runs after rendering is committed to the screen.
   * `useLayoutEffect` runs synchronously after all DOM mutations but before the browser paints. Useful when you need to make DOM measurements or synchronously adjust layout. ([Medium][4])
10. **What is `useReducer` and when should you use it over `useState`?**

    * `useReducer` is like Redux in miniature: you provide a reducer function, initial state, dispatch actions. Useful when state logic is complex or when multiple state values are inter-dependent. ([GeeksforGeeks][1])
11. **What is memoization in React? Explain `useMemo` and `useCallback`.**

    * `useMemo` memorizes the result of a computation; it recomputes only when dependencies change. ([freecodecamp.org][5])
    * `useCallback` returns a memoized version of the callback function and only changes when its dependencies change (helpful when passing callbacks to children to avoid needless re-renders).
12. **What is `React.memo` and when to use it?**

    * `React.memo` is a higher-order component. It wraps a functional component and prevents re-rendering if props haven’t changed (shallow comparison). Good for performance when component receives same props many times. ([GeeksforGeeks][1])
13. **What is code‐splitting and lazy loading in React?**

    * Code splitting: break your bundle into smaller pieces so the user loads only what’s needed first, reducing initial size.
    * With React you can use `React.lazy()` and `Suspense` for dynamic imports. ([Medium][4])
14. **What is server-side rendering (SSR) vs client-side rendering (CSR) and what benefits/downsides do they have?**

    * CSR: the app is loaded entirely in the browser; initial HTML is lightweight but JS bundle must execute first.
    * SSR: the server pre-renders HTML that the client receives, improving initial load speed & SEO. Hydration needed on client.
    * Benefit: faster initial paint, better SEO. Downside: more complexity, maybe heavier server, can face hydration mismatches. ([GitHub][3])

### Advanced / Performance & Patterns

15. **How would you optimise performance in a React application?**
    Some strategies:

    * Use production build (not dev) when performance testing. ([legacy.reactjs.org][6])
    * Use `React.memo`, `useMemo`, `useCallback` to avoid unnecessary re-renders. ([freecodecamp.org][5])
    * Avoid heavy computation on every render; defer work, debounce/throttle inputs.
    * Code-split large modules / load components lazily.
    * Avoid expensive operations in render, move them to effects or off-screen workers if needed.
    * Keep state as local as possible. Avoid big global state that triggers many re-renders.
16. **What is “prop drilling” and how do you avoid it?**

    * Prop drilling = passing props through many layers just for deeply nested components.
    * Avoid by: using Context API, custom hooks, state management libraries, or composition pattern.
17. **What are custom hooks and when should they be used?**

    * Custom Hooks start with `use…`, let you encapsulate and reuse stateful logic (not UI) across components. ([react.dev][7])
    * Use them when you have logic that is used in multiple components (e.g., form state management, data fetching, toggle logic).
18. **What are React error boundaries and how do they work?**

    * Error boundaries catch exceptions thrown in child component trees, log them and show fallback UI. Only class components can be error boundaries (as of now).
19. **What is the difference between derived state vs computed state?**

    * Derived state = state that is calculated from props or other state and stored in the component state — often an anti-pattern.
    * Computed state = not stored; computed on render (or via hooks like `useMemo`) based on existing state/props. ([GeeksforGeeks][1])
20. **What are some signs of performance problems in React apps and how to debug them?**

    * Signs: laggy UI, high CPU usage in browser, large JS bundle, frequent re-renders, long TTI (Time to Interactive).
    * To debug: use React Developer Tools, performance profiler in browser, check bundle sizes (Webpack stats), use Lighthouse, etc.
21. **What is reconciliation in React?**

    * Reconciliation: React’s algorithm to compare new Virtual DOM tree with previous, figure out minimal updates needed. Helps optimization. ([DEV Community][2])
22. **What’s the new `useTransition` hook and how does it help performance?**

    * `useTransition` is part of React’s concurrent features — allows you to mark certain state updates as “non-urgent” so the UI can show immediate feedback (pending state) and avoid blocking user input. ([Medium][8])

---

## 2. Custom Hooks – Examples & Best Practices

Here are a few practical custom hook examples, along with some best practices (and pitfalls) you should watch out for.

### Example 1: `useFetch` – Hook for data fetching

```ts
import { useState, useEffect } from 'react';

interface FetchState<T> {
  data: T | null;
  loading: boolean;
  error: Error | null;
}

function useFetch<T>(url: string, options?: RequestInit): FetchState<T> {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    let didCancel = false;

    async function fetchData() {
      setLoading(true);
      try {
        const response = await fetch(url, options);
        if (!response.ok) {
          throw new Error(`Error: ${response.status}`);
        }
        const json: T = await response.json();
        if (!didCancel) {
          setData(json);
          setLoading(false);
        }
      } catch (err) {
        if (!didCancel) {
          setError(err as Error);
          setLoading(false);
        }
      }
    }

    fetchData();

    return () => {
      didCancel = true;
    };
  }, [url, JSON.stringify(options)]);

  return { data, loading, error };
}
```

**Usage**:

```tsx
const { data, loading, error } = useFetch<MyApiResponse>('/api/items');
if (loading) return <Spinner />;
if (error) return <ErrorMessage error={error} />;
return <List items={data} />;
```

**Best practices**:

* Accept generic `<T>` to make it reusable.
* Use cleanup (`didCancel`) to avoid setting state after unmount.
* Be careful about dependencies: `JSON.stringify(options)` is one way but has pitfalls (object identity, etc.)
* Consider caching or memoizing if necessary.

### Example 2: `useToggle` – Simple boolean toggle hook

```ts
import { useState, useCallback } from 'react';

function useToggle(initialValue: boolean = false): [boolean, () => void, (value: boolean) => void] {
  const [value, setValue] = useState<boolean>(initialValue);
  const toggle = useCallback(() => setValue(v => !v), []);
  const set = useCallback((val: boolean) => setValue(val), []);
  return [value, toggle, set];
}
```

**Usage**:

```tsx
const [isOpen, toggleOpen, setOpen] = useToggle(false);
```

Simple, reusable, good for UI toggles, modals, dropdowns.

### Example 3: `usePrevious` – Capture previous value

```ts
import { useRef, useEffect } from 'react';

function usePrevious<T>(value: T): T | undefined {
  const ref = useRef<T>();
  useEffect(() => {
    ref.current = value;
  }, [value]);
  return ref.current;
}
```

**Usage**:

```tsx
const [count, setCount] = useState(0);
const prevCount = usePrevious(count);
```

Useful when you need compare previous state/props to current.

### Best Practices & Pitfalls for Custom Hooks

* Start hook names with `use` (e.g., `useMyHook`) — ensures React recognizes them as hooks. ([GitHub][3])
* Custom hooks should **share logic**, **not state**. That means each hook call is independent unless you explicitly share state above it. ([react.dev][7])
* Avoid heavy logic in custom hook render path that triggers on every render without memoization. As one article warns: *“custom React hooks could very easily turn into the biggest performance killer, if not written and used very carefully.”* ([Developer Way][9])
* For performance sensitive hooks: memoize values (`useMemo`), callbacks (`useCallback`), avoid creating new objects every time which cause consumers to re-render.
* Document your hook’s API and assumptions (what dependencies should be passed, when it will re-run, side-effects, cleaning up).
* Use TypeScript to make hooks strongly typed (especially if you’re in TS).
* If the hook needs to share state across many components (global-ish), consider Context + hook rather than purely custom hook.

---

## 3. Performance Tools in React, How to Fix Issues

### Tools & Techniques

Here are tools you can use to profile, measure and fix performance bottlenecks in React apps:

* **React Developer Tools**: Browser extension (Chrome/Firefox) that shows component tree, props/state, and has a “Profiler” tab to measure each component’s render time.
* **Browser Performance tab / Lighthouse**: In Chrome DevTools -> Performance. You can record page load, interaction, see FPS, CPU usage, long tasks, etc.
* **Bundle analyzer**: e.g., Webpack Bundle Analyzer to inspect the JS bundle size, find large modules, dead code.
* **React’s own Optimizing performance docs**: e.g., ensure production build, avoid dev mode overhead. ([legacy.reactjs.org][6])
* **Code splitting / Lazy loading**: Use `React.lazy`, `Suspense`, dynamic import.
* **Monitoring & Real‐user metrics (RUM)**: Tools like Web Vitals, First Contentful Paint, Time to Interactive, etc.
* **Memoization tools**: `React.memo`, `useMemo`, `useCallback`, `PureComponent` (for class).
* **Avoiding unnecessary renders**: Using keys correctly, avoiding object/array as props unless memoized, avoid inline functions/objects in props unless needed.

### How to Fix Performance Issues

Once you detect a performance issue, here is how you can troubleshoot/fix:

1. **Check if the build is production**
   Always test performance with a production build. Dev build has extra overhead. ([legacy.reactjs.org][6])
2. **Profiler → identify “hot” components**
   In React Developer Tools Profiler, see which components render frequently, or take long to render.
3. **Check what triggers the render**

   * Are props changing unnecessarily (object/array identity changes)?
   * Are state updates too frequent or too coarse?
4. **Use memoization**

   * `React.memo` for components that receive props and re-render often but props didn’t change.
   * `useMemo` for expensive computations inside render. ([freecodecamp.org][5])
   * `useCallback` for functions passed to children to avoid child re-render due to new function reference.
5. **Avoid inline object/array/function in props if possible**
   Example: `<Child data={{foo}} />` will create a new object each render; better: memoize the object or move it outside render.
6. **Localize state**
   If state changes cause big tree to re-render, consider moving state lower in tree or splitting components.
7. **Code-split large modules**
   If initial bundle is huge and slows first load, split code by route or feature.
8. **Avoid large lists without virtualization**
   If rendering many items (thousands) in list, use virtualization (e.g., `react-window`, `react-virtualized`).
9. **Debounce/throttle interactions**
   For e.g., searching while typing: use `useTransition` (or debounce) so UI remains responsive. ([Medium][8])
10. **Avoid unnecessary side-effects**
    In `useEffect`, check dependencies: too broad dependency arrays can cause re-runs; missing cleanup can cause memory leaks or extra work.
11. **Check bundle size & tree-shaking**
    Remove unused dependencies, import only what you need, use ES modules so bundler can tree-shake.
12. **Consider server-side performance & hydration**
    If SSR is used, check hydration time, JS parsing/execution time. Research (like MRAH) shows optimizing modular rendering and adaptive hydration can improve load times. ([arXiv][10])

### Example Fix Scenario

**Scenario**: You see, via profiler, that a component `ItemList` re-renders every time some unrelated state changes in parent, even when its props haven’t changed.

**Fix**:

```tsx
// Make Child component memoized
const ItemList = React.memo(({ items, onSelect }: { items: Item[], onSelect: (id: number) => void }) => {
  // render list
});
```

Ensure that `items` and `onSelect` do not change identity each time parent renders:

```tsx
const memoizedOnSelect = useCallback((id: number) => {
  handleSelect(id);
}, [handleSelect]);

const memoizedItems = useMemo(() => itemsFromProps, [itemsFromProps]); // if itemsFromProps is stable

<ItemList items={memoizedItems} onSelect={memoizedOnSelect} />
```

This reduces unnecessary renders of `ItemList`.

---

## 4. Useful React Extensions & Developer Tools

Here are some recommended tools/extensions to boost your React productivity and debugging/optimization efficiency.

### Browser / Dev Tools

* **React Developer Tools** (Chrome & Firefox) — inspect React component tree, hooks, props/state, Profiler.
* **Redux DevTools** (if using Redux) — inspect actions, state changes, time-travel debugging.
* **VS Code extensions**:

  * *ESLint* + *Prettier* (for consistent code style)
  * *React/Redux snippets* (speed up boilerplate)
  * *Import-cost* (shows bundle size for imported modules)
  * *Code Spell Checker / TODO Highlight* (improve code quality)
* **Webpack Bundle Analyzer** or similar — visualize bundle size, big chunks, unused modules.

### Visual Studio Code Extensions for React + TS

* *Simple React Snippets* — React functional/class component gist.
* *TSLint / ESLint React Hooks plugin* — linting for hooks rules (e.g., exhaustive deps in `useEffect`).
* *Auto Import* — better auto imports for React modules.
* *GitLens* — for better git history within VSCode.
* *Tabnine / Copilot* — AI-assisted coding (you’re already planning usage of Copilot with your team).

### Performance Monitoring / Logging Tools

* *Web Vitals* (Google) — measure key metrics like FCP, LCP, TTI, CLS.
* *Sentry* or *LogRocket* — for monitoring production errors and performance.
* *Bundlephobia* (web tool) — check size of npm packages before installing.
* *Lighthouse* (Chrome) — audit for performance, accessibility, SEO.

### Productivity Tools / Patterns

* *Storybook* — component library and documentation, helps isolate UI components, improves maintainability.
* *Pre-commit hooks* (Husky) + lint-staged — ensure code quality before commit.
* *Commitlint* — enforce commit message conventions (helpful for team alignment).
* *TypeScript* — you already use it: strongly typed components/hooks reduce bugs.

---

Absolutely 💪 — those are **core React concepts** that come up *very often* in interviews, especially when the interviewer wants to test your understanding of how React actually works under the hood.

Here’s a detailed breakdown in **GitHub-style markdown** — you can paste this into your `React_Concepts.md` documentation or use it for team prep sessions.

---

# ⚛️ React Core Concepts — Functional vs Class, Controlled vs Uncontrolled, and More

## 1. What is a Functional Component?

A **Functional Component** is a plain JavaScript (or TypeScript) function that returns JSX.
Introduced as *stateless components*, they became powerful after React 16.8 with the introduction of **Hooks** (`useState`, `useEffect`, etc.) that gave them state and lifecycle-like capabilities.

### ✅ Example:

```jsx
import React, { useState, useEffect } from "react";

function Counter() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    console.log(`Count updated: ${count}`);
  }, [count]);

  return (
    <div>
      <h3>Count: {count}</h3>
      <button onClick={() => setCount(count + 1)}>Increment</button>
    </div>
  );
}

export default Counter;
```

### 💡 Advantages

* Easier to read, write, and test.
* Better performance (no need to bind `this`).
* Hooks enable reusability of logic (`useFetch`, `useToggle`, etc.).
* Works well with tree shaking and bundlers.

---

## 2. What is a Class Component?

A **Class Component** is an ES6 class that extends `React.Component` and uses `this.state` and `this.setState()` for managing state and lifecycle methods.

### ✅ Example:

```jsx
import React, { Component } from "react";

class Counter extends Component {
  state = { count: 0 };

  componentDidMount() {
    console.log("Component Mounted");
  }

  handleIncrement = () => {
    this.setState({ count: this.state.count + 1 });
  };

  render() {
    return (
      <div>
        <h3>Count: {this.state.count}</h3>
        <button onClick={this.handleIncrement}>Increment</button>
      </div>
    );
  }
}

export default Counter;
```

### ⚙️ Key Lifecycle Methods

| Phase      | Method                                            | Purpose                      |
| ---------- | ------------------------------------------------- | ---------------------------- |
| Mounting   | `constructor()`, `componentDidMount()`            | Initialize state, fetch data |
| Updating   | `shouldComponentUpdate()`, `componentDidUpdate()` | Optimize re-renders          |
| Unmounting | `componentWillUnmount()`                          | Cleanup (timers, listeners)  |

### ❌ Disadvantages

* Verbose and harder to test.
* Requires manual `this` binding.
* Hooks replaced lifecycle methods with simpler abstractions.

---

## 3. Difference: Functional vs Class Components

| Feature             | Functional Component                   | Class Component                 |
| ------------------- | -------------------------------------- | ------------------------------- |
| Syntax              | Function                               | ES6 Class                       |
| State               | `useState`, `useReducer`               | `this.state`, `this.setState()` |
| Lifecycle           | Hooks (`useEffect`, `useLayoutEffect`) | Lifecycle methods               |
| Reusability         | Custom Hooks                           | HOCs or Render Props            |
| Performance         | Slightly faster                        | Slightly heavier                |
| Binding             | No `this` keyword                      | Needs `this` binding            |
| Recommended in 2025 | ✅ Yes                                  | ⚠️ Legacy use only              |

> 💡 **Note:** Modern React (18+) recommends *Functional Components* for new projects. Class components still work but are being phased out of new codebases.

---

## 4. Controlled Components

A **Controlled Component** is one whose form data is handled by React state — meaning the input’s value is *controlled* by the component.

### ✅ Example:

```jsx
function ControlledForm() {
  const [name, setName] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    alert(`Submitted: ${name}`);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text"
        value={name}
        onChange={(e) => setName(e.target.value)} 
      />
      <button type="submit">Submit</button>
    </form>
  );
}
```

### 💡 Key Points:

* The form input’s value comes from React state.
* State updates trigger re-render.
* Easier to validate and manipulate input data in real time.

---

## 5. Uncontrolled Components

In **Uncontrolled Components**, form data is handled by the **DOM** directly using `ref`.
React doesn’t store or control the input’s value.

### ✅ Example:

```jsx
import { useRef } from "react";

function UncontrolledForm() {
  const inputRef = useRef();

  const handleSubmit = (e) => {
    e.preventDefault();
    alert(`Submitted: ${inputRef.current.value}`);
  };

  return (
    <form onSubmit={handleSubmit}>
      <input type="text" ref={inputRef} />
      <button type="submit">Submit</button>
    </form>
  );
}
```

### 💡 Key Points:

* Value is retrieved using `ref.current.value`.
* Good for quick or legacy integrations (e.g., 3rd-party libraries).
* Not ideal when validation or dynamic changes are needed.

---

## 6. Controlled vs Uncontrolled — Comparison

| Feature       | Controlled Component                     | Uncontrolled Component          |
| ------------- | ---------------------------------------- | ------------------------------- |
| Data Source   | React State                              | DOM (`ref`)                     |
| Value Access  | via `value` prop                         | via `ref.current.value`         |
| React Control | Full                                     | Limited                         |
| Validation    | Easy (state-based)                       | Manual                          |
| Use Case      | Forms, validation                        | Quick prototypes, external libs |
| Example       | `<input value={state} onChange={...} />` | `<input ref={inputRef} />`      |

> 💡 **Best Practice:** Prefer **controlled** components for predictable UI behavior. Use **uncontrolled** only when integrating non-React libraries or simple read-only cases.

---

## 7. Bonus: Hybrid Approach

Sometimes, you can combine both:

```jsx
function HybridInput() {
  const inputRef = useRef();
  const [value, setValue] = useState("");

  useEffect(() => {
    inputRef.current.value = value.toUpperCase();
  }, [value]);

  return (
    <input
      ref={inputRef}
      defaultValue={value}
      onChange={(e) => setValue(e.target.value)}
    />
  );
}
```

> Useful when integrating with legacy code or debounced inputs.

---

## 8. Common Interview Questions

1. 🧠 **What’s the difference between functional and class components?**
   → Functional components use hooks, are simpler, and have no `this`. Class components use `this.state` and lifecycle methods.

2. ⚙️ **What are controlled components in React?**
   → Components where form input values are controlled via React state.

3. 🧩 **Why use uncontrolled components?**
   → When you want direct DOM access (e.g., file uploads, 3rd-party integrations).

4. 🧾 **How do you handle forms in React?**
   → With controlled components using `useState` or with uncontrolled using `refs`.

5. 🔄 **How does `setState` differ from `useState` updates?**
   → `setState` is asynchronous and merges partial state; `useState` replaces the state variable entirely.

6. ⚡ **Can you use Hooks inside class components?**
   → ❌ No, Hooks only work inside functional components or custom hooks.

7. 🧱 **What is the difference between `defaultValue` and `value` in inputs?**

   * `value` → makes it controlled (must handle `onChange`).
   * `defaultValue` → initializes value for uncontrolled input.

8. 🧰 **When would you use `useRef` over `useState`?**
   → `useRef` doesn’t cause re-render; use for mutable values (DOM refs, timers, previous values).

9. 💬 **How does React handle two-way binding?**
   → By manually wiring state + `onChange` handler (unlike Angular which does automatic two-way binding).

10. ⚖️ **Which is better for performance: controlled or uncontrolled?**
    → Uncontrolled components may perform slightly better (fewer re-renders), but controlled ones offer better predictability and validation.

---

## 9. Example Interview Challenge 💻

> **Question:** Build a form with two inputs — name and age. Validate that age ≥ 18 before submission using a controlled approach.

### ✅ Example Solution

```jsx
import React, { useState } from "react";

function RegistrationForm() {
  const [form, setForm] = useState({ name: "", age: "" });
  const [error, setError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (form.age < 18) setError("Age must be at least 18!");
    else {
      setError("");
      alert(`Welcome ${form.name}!`);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="name" value={form.name} onChange={handleChange} placeholder="Name" />
      <input name="age" value={form.age} onChange={handleChange} placeholder="Age" />
      {error && <p style={{ color: "red" }}>{error}</p>}
      <button type="submit">Submit</button>
    </form>
  );
}
```

### 🧠 What is a Higher-Order Component (HOC) in React?

A **Higher-Order Component (HOC)** is an **advanced pattern** in React for **reusing component logic**.
It is **a function that takes a component as input and returns a new component** with additional functionality or data.

---

### 🧩 Definition

```js
const EnhancedComponent = higherOrderComponent(WrappedComponent);
```

* `WrappedComponent` → The original React component you want to enhance.
* `higherOrderComponent` → A function that adds extra features or data.
* `EnhancedComponent` → A new component that wraps the original one with added behavior.

---

### ⚙️ Example: With Logging

```jsx
import React from "react";

// HOC that logs props
function withLogger(WrappedComponent) {
  return function EnhancedComponent(props) {
    console.log("Current props:", props);
    return <WrappedComponent {...props} />;
  };
}

// Normal component
function Hello({ name }) {
  return <h1>Hello, {name}!</h1>;
}

// Enhanced component
const HelloWithLogger = withLogger(Hello);

export default function App() {
  return <HelloWithLogger name="Sathish" />;
}
```

🧩 Output in console:

```
Current props: { name: 'Sathish' }
```

---

### ⚡ Common Use Cases

| Use Case                         | Description                                                |
| -------------------------------- | ---------------------------------------------------------- |
| **Authentication/Authorization** | Wrap protected components to check if a user is logged in. |
| **Logging/Analytics**            | Add tracking logic around multiple components.             |
| **Data fetching**                | Inject fetched data as props.                              |
| **Error handling**               | Wrap components with error boundaries.                     |

---

### 🛠️ Example: HOC for Authentication

```jsx
function withAuth(WrappedComponent) {
  return function ProtectedComponent(props) {
    const isAuthenticated = localStorage.getItem("token");

    if (!isAuthenticated) {
      return <h3>Please log in to view this page</h3>;
    }

    return <WrappedComponent {...props} />;
  };
}

function Dashboard() {
  return <h2>Welcome to Dashboard</h2>;
}

export default withAuth(Dashboard);
```

---

### ⚠️ Best Practices & Limitations

✅ **Best Practices**

* Use **meaningful names** for HOCs (e.g., `withLogger`, `withAuth`, `withTheme`).
* **Do not mutate** the original component.
* Pass all props using `{...props}` to preserve functionality.
* Compose multiple HOCs using libraries like `recompose`.

🚫 **Limitations**

* Can create **"wrapper hell"** (too many nested components).
* Difficult to debug due to deep component trees.
* React Hooks make many HOC patterns **obsolete** (hooks are now preferred).

---

### 🧠 Hooks Alternative (Replacing HOCs)

Modern React prefers **custom hooks** over HOCs for logic reuse.

Example:

```jsx
function useAuth() {
  const [isAuthenticated, setAuthenticated] = React.useState(false);
  React.useEffect(() => {
    setAuthenticated(!!localStorage.getItem("token"));
  }, []);
  return isAuthenticated;
}

function Dashboard() {
  const isAuthenticated = useAuth();
  if (!isAuthenticated) return <h3>Please login</h3>;
  return <h2>Dashboard</h2>;
}
```

Great — here are **two parts** for you:

---

## A) Interview-style answers for key React Hooks

Below are several commonly asked hooks, each with a concise answer (for interview prep) plus a short code example. Feel free to pick and choose which ones you want to drill deeper.

### 1. `useState`

**Q :** What is `useState` and when should you use it?
**A :**

* `useState` is a React Hook that lets functional components have state. It returns a pair: the current state value, and a setter function to update it. ([GeeksforGeeks][1])
* Use it when you need to keep track of a value that changes over time (e.g., input value, toggle state, counter).
* Example:

  ```tsx
  const [count, setCount] = useState(0);

  // Later
  setCount(prev => prev + 1);
  ```
* Key notes:

  * When you call the setter, React schedules a re-render of the component.
  * If the new state is the same as the old (by reference/value depending), React may bail out.
  * When updating state that depends on previous state, use the functional form: `setCount(prev => prev+1)` to avoid stale values. ([GreatFrontEnd][2])

### 2. `useEffect`

**Q :** What is `useEffect` and how is it different from class-component lifecycle methods?
**A :**

* `useEffect` lets you perform side-effects in functional components (e.g., data fetching, subscriptions, manually manipulating DOM). ([GreatFrontEnd][2])
* It accepts two arguments: a function (the effect) and an optional dependencies array.

  * If no dependencies array is provided → runs after every render.
  * If empty array `[]` → runs only once after first render (componentDidMount equivalent).
  * If `[dep1, dep2]` → runs when any dependency changes.
* It also supports returning a cleanup function, which runs when component unmounts or before effect runs again.
* Example:

  ```tsx
  useEffect(() => {
    const id = setInterval(() => {
      console.log("tick");
    }, 1000);
    return () => clearInterval(id);
  }, []);
  ```
* Difference vs class methods:

  * In class: you had `componentDidMount`, `componentDidUpdate`, `componentWillUnmount`. With `useEffect` you can cover them all.
  * But you must be careful with the dependencies array to avoid infinite loops or missed updates. ([Medium][3])

### 3. `useRef`

**Q :** What is `useRef` used for?
**A :**

* `useRef` returns a mutable ref object whose `.current` property persists across renders. It does *not* trigger re-renders when changed.
* Use cases:

  * Accessing DOM elements (e.g., `const inputEl = useRef<HTMLInputElement>(null);`) and then `<input ref={inputEl} />`.
  * Storing any mutable value you want to persist without causing re-render (e.g., timer IDs, previous values).
* Example:

  ```tsx
  const countRef = useRef<number>(0);
  useEffect(() => {
    countRef.current++;
    console.log("Renders so far:", countRef.current);
  });
  ```
* Pitfall: changing `ref.current` will not cause the component to update UI. If you need UI update, you must use state.

### 4. `useContext`

**Q :** What is `useContext` and when is it helpful?
**A :**

* `useContext` lets a functional component subscribe to React context (created via `createContext`).
* Instead of drilling props down many levels, you can place a Provider high up, and any descendant can call `useContext(MyContext)` to read the value.
* Example:

  ```tsx
  const ThemeContext = createContext("light");
  function MyComponent() {
    const theme = useContext(ThemeContext);
    return <div className={theme}>…</div>;
  }
  ```
* Key: It simplifies sharing state/values across component tree without prop-drilling.

### 5. `useReducer`

**Q :** When should you use `useReducer` over `useState`?
**A :**

* `useReducer` is a hook that manages state by dispatching actions to a reducer function `(state, action) => newState`.
* Use it when:

  * You have **complex state logic** (many sub-values, multiple possible actions).
  * The next state depends on previous state (more cleanly expressed via reducer).
  * You want to centralize state updates.
* Example:

  ```tsx
  type State = { count: number; step: number };
  type Action = { type: "increment" } | { type: "setStep"; payload: number };

  const reducer = (state: State, action: Action): State => {
    switch (action.type) {
      case "increment":
        return { ...state, count: state.count + state.step };
      case "setStep":
        return { ...state, step: action.payload };
      default:
        return state;
    }
  };

  function Counter() {
    const [state, dispatch] = useReducer(reducer, { count: 0, step: 1 });
    // ...
  }
  ```

### 6. `useCallback` & `useMemo`

**Q :** What’s the difference between `useCallback` and `useMemo`?
**A :**

* `useCallback(fn, deps)` returns a memoized version of the callback `fn` that only changes when dependencies change. Useful when passing callbacks to child components so they don’t re-render unnecessarily.
* `useMemo(() => computeExpensiveValue(), deps)` returns a memoized value from the computation.
* In short: `useCallback` → memoize function; `useMemo` → memoize result/value. ([GreatFrontEnd][4])
* Example:

  ```tsx
  const memoizedCallback = useCallback(() => {
    doSomething(a, b);
  }, [a, b]);

  const memoizedValue = useMemo(() => compute(a, b), [a, b]);
  ```

### 7. `useLayoutEffect`

**Q :** How is `useLayoutEffect` different from `useEffect`?
**A :**

* `useLayoutEffect` runs synchronously **after** DOM mutations but **before** the browser paints. Useful for DOM measurements, synchronously reading layout (offsetWidth etc) and making visual adjustments before user sees. ([Medium][3])
* `useEffect` runs after the browser paint (asynchronous). For tasks that don’t need to block painting (e.g., network requests, subscriptions) prefer `useEffect`.
* Example:

  ```tsx
  useLayoutEffect(() => {
    const width = ref.current.offsetWidth;
    if (width > 500) {
      setLarge(true);
    }
  }, [ref]);
  ```

---

### Bonus: What to highlight in your answers

When asked about any hook, good points to raise:

* Why this hook was introduced (problem it solves).
* When you should use it.
* Pitfalls / mistakes (e.g., stale closures in `useEffect`, dependencies missing, over-memoization).
* Example code snippet with TypeScript if your stack uses TS.
* How it compares to older patterns (class lifecycle, HOCs, render props).
* Best practices (e.g., always list dependencies, avoid unnecessary state, never call hooks conditionally or inside loops).
  ([GreatFrontEnd][4])

---

## B) What’s new in React 19 (major changes)

Here are the key changes in React 19 that you should be aware of (good for interviews, architecture discussions, upgrade planning).

### ✅ Major New Features

* **Actions**: React 19 introduces “Actions” — essentially functions that can be used for async state updates, integrated with transitions, handling pending/error states, optimistic updates more naturally. ([react.dev][5])
* **New Hooks**: e.g., `useActionState`, `useOptimistic`, `useFormStatus`, plus the `use` API (to read promises/resources in render) and improved `useId`. ([react.dev][5])
* **Simplified Ref/forwardRef handling**: Easier passing of refs as props in many cases. ([freecodecamp.org][6])
* **Improved SSR & Streaming & Suspense enhancements**: Better streaming for server-side rendering, partial pre-rendering, improved hydration diffing. ([react.dev][7])
* **Resource/metadata/style/script APIs**: React 19 introduces built-in support for document metadata (title/meta tags), stylesheets, async scripts, preloading resources. ([GeeksforGeeks][8])
* **Compiler/optimization improvements**: Some article claims the new “React Compiler” reduces need for many manual optimizations (memoization) though this is somewhat aspirational. ([freecodecamp.org][6])

### ⚠️ Notable Changes / Breaking / Upgrade Considerations

* Removal of UMD builds: React 19 stops shipping UMD builds; if you’re relying on script tags older way you may need ESM from CDN. ([react.dev][9])
* Changes to StrictMode behaviour, improvements to Suspense fallback logic. ([react.dev][9])
* Libraries that use React internals (`SECRET_INTERNALS`) may break, so upgrading requires checking dependencies. ([react.dev][9])
* Some patterns (e.g., `forwardRef` use) simplified, so codebases may become cleaner but need review.

### 🧑‍💻 Interview-relevant points

* “What are Actions in React 19? How do they compare with earlier event handlers or `onSubmit` logic?”
* “What is the `use` hook? How might it simplify your async data fetching logic?”
* “How has SSR improved in React 19 (partial pre-rendering, streaming)?”
* “What should you check when upgrading an existing React 18-based application to React 19?”

---
Perfect 💪 — here’s a **complete 20-question React Hooks + React 19 interview guide**, formatted like a GitHub-ready `.md` file you can study or share with your team.

---

# 🧠 React Hooks & React 19 — Advanced Interview Questions & Answers

---

## 🔹 1. What are React Hooks and why were they introduced?

**Answer:**
Hooks allow you to use **state, lifecycle, and other React features** without writing class components.
They solve problems like:

* Logic reuse across components (previously HOCs/render props)
* Complex lifecycle methods in classes
* Better code organization & testing

Example:

```jsx
function Counter() {
  const [count, setCount] = useState(0);
  return <button onClick={() => setCount(count + 1)}>{count}</button>;
}
```

---

## 🔹 2. Explain `useState` and how it works internally.

**Answer:**
`useState` adds local state to functional components.
React keeps a state queue per component; each re-render retrieves state by hook order.

```jsx
const [count, setCount] = useState(0);
setCount(prev => prev + 1);
```

🧠 **Tip:** Always use the functional update form when new state depends on the previous one.

---

## 🔹 3. What is `useEffect` and how does it differ from lifecycle methods?

**Answer:**
`useEffect` handles side effects — API calls, subscriptions, DOM manipulation.
It replaces `componentDidMount`, `componentDidUpdate`, `componentWillUnmount`.

```jsx
useEffect(() => {
  console.log("Mounted");
  return () => console.log("Unmounted");
}, []);
```

🧠 **Key:** Cleanup runs before unmount or next effect call.

---

## 🔹 4. What are dependency arrays in `useEffect` and why are they important?

**Answer:**
The dependency array tells React when to re-run the effect.

* `[]` → runs once
* `[x]` → runs when `x` changes
* No array → runs on every render

Missing dependencies → stale data or bugs.
✅ Always include all values you reference inside the effect.

---

## 🔹 5. Difference between `useEffect` and `useLayoutEffect`

**Answer:**

| Feature  | `useEffect`             | `useLayoutEffect`           |
| -------- | ----------------------- | --------------------------- |
| Timing   | After paint (async)     | Before paint (sync)         |
| Use case | Fetching, subscriptions | DOM measurement, layout fix |
| Impact   | Non-blocking            | Can block paint             |

---

## 🔹 6. Explain `useRef` with an example.

**Answer:**
`useRef` holds a **mutable value** that doesn’t trigger re-render.

```jsx
const inputRef = useRef(null);
useEffect(() => inputRef.current.focus(), []);
return <input ref={inputRef} />;
```

Also useful to store previous state:

```jsx
const prevCount = useRef(count);
useEffect(() => { prevCount.current = count; }, [count]);
```

---

## 🔹 7. What is `useContext` and how does it avoid prop-drilling?

**Answer:**
Allows access to global values without passing props through every level.

```jsx
const ThemeContext = createContext("light");
const theme = useContext(ThemeContext);
```

⚠️ Re-renders all consumers when context changes → consider memoization.

---

## 🔹 8. Explain `useReducer` and when to prefer it over `useState`.

**Answer:**
When managing **complex state** or multiple updates.

```jsx
const reducer = (state, action) => {
  switch(action.type) {
    case "increment": return { count: state.count + 1 };
    default: return state;
  }
};
const [state, dispatch] = useReducer(reducer, { count: 0 });
```

Use it when logic grows complicated or you plan to migrate to Redux.

---

## 🔹 9. What is `useMemo` and how does it optimize performance?

**Answer:**
Memoizes **expensive calculations** between renders.

```jsx
const result = useMemo(() => computeExpensiveValue(a, b), [a, b]);
```

Prevents recomputation unless dependencies change.
⚠️ Don’t overuse — memoization itself has a cost.

---

## 🔹 10. What is `useCallback` and how is it different from `useMemo`?

**Answer:**
`useCallback(fn, deps)` returns the same function reference between renders.
Useful when passing callbacks to memoized children to prevent re-renders.

```jsx
const handleClick = useCallback(() => setCount(c => c + 1), []);
```

🧠 `useMemo` → memoize **value**, `useCallback` → memoize **function**.

---

## 🔹 11. What is `useImperativeHandle` used for?

**Answer:**
Customizes the instance value exposed to parent when using `forwardRef`.

```jsx
useImperativeHandle(ref, () => ({
  focus: () => inputRef.current.focus()
}));
```

Used when you want to expose limited methods from a child component.

---

## 🔹 12. What is `useId` and when is it useful?

**Answer:**
Generates unique, consistent IDs across server/client renders.

```jsx
const id = useId();
<label htmlFor={id}>Name</label>
<input id={id} />
```

Essential for accessibility & SSR-safe unique identifiers.

---

## 🔹 13. Explain `useTransition` and `useDeferredValue`.

**Answer:**
Both are for **concurrent rendering optimization**:

* `useTransition` → mark updates as *non-urgent*.
* `useDeferredValue` → delay rendering of non-urgent values.

Example:

```jsx
const [isPending, startTransition] = useTransition();
startTransition(() => setSearchQuery(input));
```

---

## 🔹 14. What is `useSyncExternalStore`?

**Answer:**
Ensures React state stays in sync with external stores (like Redux).
Improves concurrent rendering reliability.

```jsx
const snapshot = useSyncExternalStore(store.subscribe, store.getSnapshot);
```

---

## 🔹 15. What are custom hooks and why use them?

**Answer:**
Reusable functions that start with `use` and compose existing hooks.

Example:

```jsx
function useFetch(url) {
  const [data, setData] = useState();
  useEffect(() => { fetch(url).then(r => r.json()).then(setData); }, [url]);
  return data;
}
```

Promotes **reusability** and **clean separation of concerns**.

---

## 🔹 16. How to debug performance issues in React?

**Tools & Fixes:**

* **React DevTools Profiler** → measure re-render time.
* **Flamegraph** → visualize slow components.
* **Why Did You Render** (extension) → detect unnecessary re-renders.
* Optimize by:

  * Using `React.memo`, `useCallback`, `useMemo`.
  * Avoid creating inline objects/functions unnecessarily.
  * Split large lists via `React.lazy`, `Suspense`, or virtualization.

---

## 🔹 17. What is the difference between controlled and uncontrolled components?

**Answer:**

| Type         | State Managed By | Example                                  |
| ------------ | ---------------- | ---------------------------------------- |
| Controlled   | React            | `<input value={state} onChange={...} />` |
| Uncontrolled | DOM              | `<input defaultValue="hi" ref={ref} />`  |

Controlled = predictable; Uncontrolled = simpler but less flexible.

---

## 🔹 18. What are Higher-Order Components (HOCs)?

**Answer:**
A function that takes a component and returns an enhanced component.

```jsx
const withLogger = (Component) => (props) => {
  console.log(props);
  return <Component {...props} />;
};
```

Used for cross-cutting concerns like logging, authentication, etc.
⚠️ Hooks now often replace HOCs for reusability.

---

## 🔹 19. What’s new in React 19?

**Key Highlights:**

* 🧩 **Actions API** → Simplifies async form handling, optimistic UI.
* 🪝 **New Hooks**: `useActionState`, `useOptimistic`, `useFormStatus`, `use` (for async data in render).
* 🧠 **Improved SSR & Streaming** — partial pre-rendering.
* 💅 **Metadata, style, and script tags** management built-in.
* ⚙️ **Ref forwarding simplifications**, strict mode improvements.
* ❌ **UMD builds removed** — switch to ESM/CDN.

---

## 🔹 20. How do “Actions” work in React 19?

**Answer:**
Actions are async functions integrated with React’s transition model.
They handle pending, success, and error states declaratively.

```jsx
async function saveUser(data) {
  "use server"; // (when using server actions)
  await db.user.create(data);
}

<form action={saveUser}>
  <button type="submit">Save</button>
</form>
```

They simplify form submissions, error boundaries, and optimistic updates.

---

## 🧩 Bonus — Useful Extensions for React Development

| Category      | Extension / Tool                   | Use                                     |
| ------------- | ---------------------------------- | --------------------------------------- |
| Debugging     | React Developer Tools              | Inspect component tree, hooks, profiler |
| Performance   | Why Did You Render                 | Detect unnecessary re-renders           |
| Linting       | ESLint + eslint-plugin-react-hooks | Ensures correct hook usage              |
| Styling       | Tailwind CSS IntelliSense          | Autocomplete for Tailwind classes       |
| Productivity  | VSCode React Snippets              | Quick React boilerplate                 |
| Visualization | React DevTools Profiler            | Analyze render performance              |

---

## 🏁 Summary Table — Hooks Overview

| Hook                 | Category      | Purpose                      |
| -------------------- | ------------- | ---------------------------- |
| useState             | State         | Manage local state           |
| useEffect            | Side effects  | Run logic after render       |
| useContext           | Context       | Consume global values        |
| useRef               | DOM/state ref | Persist values w/o re-render |
| useMemo              | Performance   | Memoize expensive values     |
| useCallback          | Performance   | Memoize functions            |
| useReducer           | State         | Complex state logic          |
| useLayoutEffect      | DOM sync      | Run before paint             |
| useTransition        | Performance   | Mark non-urgent updates      |
| useSyncExternalStore | State         | External store integration   |

---
Let's go deep into **SSR (Server-Side Rendering)** — one of the most asked React interview topics, especially for performance and SEO optimization questions 👇

---

## ⚡ What is SSR (Server-Side Rendering) in React?

**SSR (Server-Side Rendering)** means **rendering React components on the server instead of the browser**.

When a user requests a page:

1. The **server generates the full HTML** (with data) for that route.
2. The **client receives the HTML** and shows the content instantly.
3. Then React **hydrates** the page — attaches event listeners and makes it interactive.

---

### 🧠 Why SSR?

✅ **Better SEO**
Search engines can easily crawl pre-rendered HTML pages.

✅ **Faster First Paint (TTFB)**
The user sees content before the JS bundle finishes loading.

✅ **Improved performance on slow devices**
Since the server does most of the computation.

---

## ⚙️ Example of SSR

Without SSR (Client-Side Rendering - CSR):

```jsx
// Browser fetches JS, renders after download
function App() {
  const [user, setUser] = useState(null);
  useEffect(() => {
    fetch('/api/user').then(res => res.json()).then(setUser);
  }, []);
  return user ? <h1>Hello {user.name}</h1> : <p>Loading...</p>;
}
```

With SSR (Server renders HTML):

```js
// server.js
import express from "express";
import ReactDOMServer from "react-dom/server";
import App from "./App.js";

const app = express();

app.get("/", (req, res) => {
  const html = ReactDOMServer.renderToString(<App />);
  res.send(`
    <!DOCTYPE html>
    <html>
      <body>
        <div id="root">${html}</div>
        <script src="/bundle.js"></script>
      </body>
    </html>
  `);
});

app.listen(3000);
```

Now the client gets the HTML **instantly**, and then React hydrates the app.

---

## 🔁 Hydration Process

Hydration = attaching React’s event handlers to the HTML already rendered on the server.

```js
import { hydrateRoot } from "react-dom/client";
import App from "./App";

hydrateRoot(document.getElementById("root"), <App />);
```

---

## 🧱 SSR vs CSR vs SSG vs ISR

| Rendering Type                            | Where Rendering Happens  | When                             | Pros                            | Cons                                   |
| ----------------------------------------- | ------------------------ | -------------------------------- | ------------------------------- | -------------------------------------- |
| **CSR (Client-Side Rendering)**           | Browser                  | On demand (after JS loads)       | Great for SPAs, fast navigation | Slow first paint, SEO issues           |
| **SSR (Server-Side Rendering)**           | Server                   | On each request                  | SEO-friendly, fast TTFB         | More server load, slower per-request   |
| **SSG (Static Site Generation)**          | Build time               | Once (pre-built HTML)            | Super fast, cached              | Not dynamic, needs rebuild for changes |
| **ISR (Incremental Static Regeneration)** | Server (build + runtime) | Build + after cache invalidation | Mix of static + dynamic         | Slightly complex setup                 |

---

## ⚙️ Frameworks that Support SSR

| Framework   | Description                                      |
| ----------- | ------------------------------------------------ |
| **Next.js** | Most popular React framework for SSR, SSG, ISR   |
| **Remix**   | Focused on data loading and routing optimization |
| **Gatsby**  | Primarily SSG with SSR capabilities              |
| **Razzle**  | SSR boilerplate for React without Next.js        |

---

## 🧩 SSR Lifecycle in React 18/19

React 18 introduced **Streaming SSR**:

* Instead of sending all HTML at once, it streams partial HTML as it’s rendered.
* Helps reduce TTFB (Time To First Byte).

Example:

```js
import { renderToPipeableStream } from 'react-dom/server';
import App from './App';

app.get('/', (req, res) => {
  const stream = renderToPipeableStream(<App />, {
    onShellReady() {
      res.statusCode = 200;
      stream.pipe(res);
    },
  });
});
```

React 19 improves **Selective Hydration** — only rehydrates the parts the user interacts with first, improving UX and load times.

---

## 🚀 Performance & Optimization Techniques for SSR

1. **Use caching** (Redis, CDN) for repeated requests.
2. **Code-splitting** — load JS bundles per route.
3. **Pre-fetch data** before render using `getServerSideProps` (Next.js).
4. **Streaming + Suspense** for incremental rendering.
5. **Avoid large client hydration payloads** (don’t over-hydrate static sections).

---

## 🔍 Interview-Level Questions

1. **What is the difference between SSR and CSR?**

   > SSR renders HTML on the server before sending it to the browser, while CSR renders everything in the browser after fetching JS.

2. **Why is SSR important for SEO?**

   > Because crawlers can read pre-rendered HTML without needing to execute JavaScript.

3. **What is Hydration in SSR?**

   > Hydration attaches event listeners to server-rendered HTML to make it interactive.

4. **Difference between SSR and SSG?**

   > SSR generates pages on each request; SSG pre-builds all pages at build time.

5. **What are the drawbacks of SSR?**

   > Increased server load, complex caching, slower requests under heavy traffic.

---
