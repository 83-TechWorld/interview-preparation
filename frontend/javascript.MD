# JavaScript Interview Preparation

## Table of Contents
1. [Core Concepts](#core-concepts)
2. [Data Structures](#data-structures)
3. [DOM Manipulation](#dom-manipulation)
4. [Runtime APIs](#runtime-apis)
5. [Practice Questions](#practice-questions)

---

## Core Concepts

### Closures
A closure is a function that has access to variables in its outer (enclosing) lexical scope, even after the outer function has returned.

**Example:**
```javascript
function createCounter() {
  let count = 0; // Private variable
  
  return {
    increment: function() {
      count++;
      return count;
    },
    decrement: function() {
      count--;
      return count;
    },
    getCount: function() {
      return count;
    }
  };
}

const counter = createCounter();
console.log(counter.increment()); // 1
console.log(counter.increment()); // 2
console.log(counter.getCount());  // 2
```

**Real-world use case:**
```javascript
function createLogger(prefix) {
  return function(message) {
    console.log(`[${prefix}] ${message}`);
  };
}

const errorLogger = createLogger('ERROR');
const infoLogger = createLogger('INFO');

errorLogger('Something went wrong'); // [ERROR] Something went wrong
infoLogger('Process completed');      // [INFO] Process completed
```

---

### Callbacks
A callback is a function passed as an argument to another function, to be executed later.

**Example:**
```javascript
// Simple callback
function fetchData(callback) {
  setTimeout(() => {
    const data = { id: 1, name: 'John' };
    callback(data);
  }, 1000);
}

fetchData((data) => {
  console.log('Data received:', data);
});

// Array methods with callbacks
const numbers = [1, 2, 3, 4, 5];
const doubled = numbers.map(num => num * 2); // [2, 4, 6, 8, 10]
const evens = numbers.filter(num => num % 2 === 0); // [2, 4]
```

**Callback Hell (avoid this):**
```javascript
getData(function(a) {
  getMoreData(a, function(b) {
    getMoreData(b, function(c) {
      getMoreData(c, function(d) {
        console.log(d);
      });
    });
  });
});
```

---

### Promise
A Promise represents the eventual completion (or failure) of an asynchronous operation.

**Example:**
```javascript
// Creating a Promise
const fetchUser = (userId) => {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (userId > 0) {
        resolve({ id: userId, name: 'John Doe' });
      } else {
        reject(new Error('Invalid user ID'));
      }
    }, 1000);
  });
};

// Using Promise
fetchUser(1)
  .then(user => {
    console.log('User:', user);
    return fetchUser(2);
  })
  .then(user => {
    console.log('Another user:', user);
  })
  .catch(error => {
    console.error('Error:', error.message);
  })
  .finally(() => {
    console.log('Operation completed');
  });
```

**Promise chaining:**
```javascript
fetch('https://api.example.com/user/1')
  .then(response => response.json())
  .then(user => fetch(`https://api.example.com/posts?userId=${user.id}`))
  .then(response => response.json())
  .then(posts => console.log('User posts:', posts))
  .catch(error => console.error('Error:', error));
```

---

### Async/Await
Async/await is syntactic sugar over Promises, making asynchronous code look synchronous.

**Example:**
```javascript
// Basic async/await
async function getUser(userId) {
  try {
    const response = await fetch(`https://api.example.com/user/${userId}`);
    const user = await response.json();
    return user;
  } catch (error) {
    console.error('Error fetching user:', error);
    throw error;
  }
}

// Using the async function
async function displayUserData() {
  try {
    const user = await getUser(1);
    console.log('User:', user);
    
    const posts = await fetch(`https://api.example.com/posts?userId=${user.id}`);
    const postsData = await posts.json();
    console.log('Posts:', postsData);
  } catch (error) {
    console.error('Failed to load user data:', error);
  }
}

displayUserData();
```

**Parallel execution:**
```javascript
async function fetchMultipleUsers() {
  try {
    // Run in parallel
    const [user1, user2, user3] = await Promise.all([
      getUser(1),
      getUser(2),
      getUser(3)
    ]);
    
    console.log('All users:', user1, user2, user3);
  } catch (error) {
    console.error('Error:', error);
  }
}
```

---

### The 'this' Keyword
The value of `this` depends on how a function is called.

**Example:**
```javascript
// 1. Global context
console.log(this); // Window (browser) or global (Node.js)

// 2. Object method
const person = {
  name: 'John',
  greet: function() {
    console.log(`Hello, I'm ${this.name}`);
  }
};
person.greet(); // "Hello, I'm John"

// 3. Lost context
const greet = person.greet;
greet(); // "Hello, I'm undefined" (this is lost)

// 4. Arrow functions (lexical this)
const person2 = {
  name: 'Jane',
  hobbies: ['reading', 'coding'],
  showHobbies: function() {
    this.hobbies.forEach(hobby => {
      // Arrow function inherits 'this' from parent scope
      console.log(`${this.name} likes ${hobby}`);
    });
  }
};
person2.showHobbies();

// 5. Explicit binding
function introduce() {
  console.log(`I'm ${this.name}`);
}
const user = { name: 'Alice' };
introduce.call(user);  // "I'm Alice"
introduce.apply(user); // "I'm Alice"
const boundIntroduce = introduce.bind(user);
boundIntroduce(); // "I'm Alice"

// 6. Constructor
function Person(name) {
  this.name = name;
}
const john = new Person('John');
console.log(john.name); // "John"
```

---

## Data Structures

### Arrays
Arrays are ordered collections of elements.

**Common operations:**
```javascript
// Creation
const arr = [1, 2, 3, 4, 5];
const arr2 = new Array(5); // [empty × 5]

// Accessing
console.log(arr[0]); // 1
console.log(arr[arr.length - 1]); // 5

// Modification
arr.push(6);        // Add to end: [1, 2, 3, 4, 5, 6]
arr.pop();          // Remove from end: [1, 2, 3, 4, 5]
arr.unshift(0);     // Add to start: [0, 1, 2, 3, 4, 5]
arr.shift();        // Remove from start: [1, 2, 3, 4, 5]
arr.splice(2, 1);   // Remove at index: [1, 2, 4, 5]

// Iteration
arr.forEach(num => console.log(num));
const doubled = arr.map(num => num * 2);
const evens = arr.filter(num => num % 2 === 0);
const sum = arr.reduce((acc, num) => acc + num, 0);

// Search
console.log(arr.includes(3)); // true
console.log(arr.indexOf(3));  // 2
console.log(arr.find(num => num > 3)); // 4

// Transform
console.log(arr.slice(1, 3)); // [2, 3]
console.log(arr.join('-'));   // "1-2-4-5"
console.log([...arr, 6, 7]);  // Spread: [1, 2, 4, 5, 6, 7]
```

---

### Maps
Maps are collections of key-value pairs where keys can be any type.

**Example:**
```javascript
// Creation
const userMap = new Map();

// Setting values
userMap.set('id', 1);
userMap.set('name', 'John');
userMap.set('isActive', true);

// Can use objects as keys
const keyObj = { id: 1 };
userMap.set(keyObj, 'Object key value');

// Getting values
console.log(userMap.get('name')); // "John"
console.log(userMap.has('email')); // false
console.log(userMap.size); // 4

// Iteration
userMap.forEach((value, key) => {
  console.log(`${key}: ${value}`);
});

for (const [key, value] of userMap) {
  console.log(`${key} = ${value}`);
}

// Convert to array
const entries = [...userMap.entries()];
const keys = [...userMap.keys()];
const values = [...userMap.values()];

// Deletion
userMap.delete('id');
userMap.clear(); // Remove all
```

**Map vs Object:**
```javascript
// Map advantages:
// 1. Any type as key
const map = new Map();
map.set(1, 'number key');
map.set(true, 'boolean key');
map.set(() => {}, 'function key');

// 2. Size property
console.log(map.size);

// 3. Better performance for frequent additions/deletions
// 4. Maintains insertion order
```

---

### Sets
Sets are collections of unique values.

**Example:**
```javascript
// Creation
const numbers = new Set([1, 2, 3, 3, 4, 4, 5]);
console.log(numbers); // Set(5) {1, 2, 3, 4, 5}

// Adding values
numbers.add(6);
numbers.add(6); // Duplicates ignored

// Checking existence
console.log(numbers.has(3)); // true
console.log(numbers.size); // 6

// Deletion
numbers.delete(6);

// Iteration
numbers.forEach(num => console.log(num));
for (const num of numbers) {
  console.log(num);
}

// Conversion
const arr = [...numbers]; // Set to Array
const uniqueArr = [...new Set([1, 1, 2, 2, 3])]; // Remove duplicates

// Set operations
const setA = new Set([1, 2, 3]);
const setB = new Set([3, 4, 5]);

// Union
const union = new Set([...setA, ...setB]); // {1, 2, 3, 4, 5}

// Intersection
const intersection = new Set([...setA].filter(x => setB.has(x))); // {3}

// Difference
const difference = new Set([...setA].filter(x => !setB.has(x))); // {1, 2}
```

---

### Stacks
Stack is a LIFO (Last In, First Out) data structure.

**Implementation:**
```javascript
class Stack {
  constructor() {
    this.items = [];
  }
  
  push(element) {
    this.items.push(element);
  }
  
  pop() {
    if (this.isEmpty()) return undefined;
    return this.items.pop();
  }
  
  peek() {
    if (this.isEmpty()) return undefined;
    return this.items[this.items.length - 1];
  }
  
  isEmpty() {
    return this.items.length === 0;
  }
  
  size() {
    return this.items.length;
  }
  
  clear() {
    this.items = [];
  }
}

// Usage
const stack = new Stack();
stack.push(1);
stack.push(2);
stack.push(3);
console.log(stack.peek()); // 3
console.log(stack.pop());  // 3
console.log(stack.size()); // 2

// Real-world example: Browser history
class BrowserHistory {
  constructor() {
    this.history = new Stack();
  }
  
  visit(url) {
    this.history.push(url);
    console.log(`Visited: ${url}`);
  }
  
  back() {
    this.history.pop();
    const current = this.history.peek();
    console.log(`Back to: ${current}`);
  }
}
```

---

### Trees
Trees are hierarchical data structures with nodes.

**Binary Tree Example:**
```javascript
class TreeNode {
  constructor(value) {
    this.value = value;
    this.left = null;
    this.right = null;
  }
}

class BinaryTree {
  constructor() {
    this.root = null;
  }
  
  insert(value) {
    const newNode = new TreeNode(value);
    
    if (!this.root) {
      this.root = newNode;
      return;
    }
    
    const insertNode = (node, newNode) => {
      if (newNode.value < node.value) {
        if (!node.left) {
          node.left = newNode;
        } else {
          insertNode(node.left, newNode);
        }
      } else {
        if (!node.right) {
          node.right = newNode;
        } else {
          insertNode(node.right, newNode);
        }
      }
    };
    
    insertNode(this.root, newNode);
  }
  
  // Depth-First Search (DFS)
  inOrderTraversal(node = this.root, result = []) {
    if (node) {
      this.inOrderTraversal(node.left, result);
      result.push(node.value);
      this.inOrderTraversal(node.right, result);
    }
    return result;
  }
  
  // Breadth-First Search (BFS)
  levelOrderTraversal() {
    if (!this.root) return [];
    
    const result = [];
    const queue = [this.root];
    
    while (queue.length > 0) {
      const node = queue.shift();
      result.push(node.value);
      
      if (node.left) queue.push(node.left);
      if (node.right) queue.push(node.right);
    }
    
    return result;
  }
  
  search(value, node = this.root) {
    if (!node) return false;
    if (node.value === value) return true;
    
    if (value < node.value) {
      return this.search(value, node.left);
    } else {
      return this.search(value, node.right);
    }
  }
}

// Usage
const tree = new BinaryTree();
tree.insert(5);
tree.insert(3);
tree.insert(7);
tree.insert(1);
tree.insert(9);

console.log(tree.inOrderTraversal());  // [1, 3, 5, 7, 9]
console.log(tree.levelOrderTraversal()); // [5, 3, 7, 1, 9]
console.log(tree.search(7)); // true
```

---

## DOM Manipulation

### DOM Traversal
Navigating through the DOM tree.

**Example:**
```javascript
// Getting elements
const element = document.getElementById('myId');
const elements = document.getElementsByClassName('myClass');
const divs = document.getElementsByTagName('div');
const element2 = document.querySelector('.myClass');
const elements2 = document.querySelectorAll('div.myClass');

// Parent/Child relationships
const parent = element.parentNode;
const children = element.children; // HTMLCollection
const firstChild = element.firstElementChild;
const lastChild = element.lastElementChild;

// Siblings
const nextSibling = element.nextElementSibling;
const prevSibling = element.previousElementSibling;

// Traversal example
function findAllParents(element) {
  const parents = [];
  let current = element.parentElement;
  
  while (current) {
    parents.push(current);
    current = current.parentElement;
  }
  
  return parents;
}

// Finding closest ancestor
const closestForm = element.closest('form');
const hasClass = element.matches('.myClass');
```

---

### DOM Creation
Creating new elements dynamically.

**Example:**
```javascript
// Creating elements
const div = document.createElement('div');
const text = document.createTextNode('Hello World');
const fragment = document.createDocumentFragment();

// Setting attributes and content
div.id = 'myDiv';
div.className = 'container';
div.setAttribute('data-id', '123');
div.innerHTML = '<p>Paragraph</p>';
div.textContent = 'Plain text';

// Building complex structures
function createUserCard(user) {
  const card = document.createElement('div');
  card.className = 'user-card';
  
  const name = document.createElement('h3');
  name.textContent = user.name;
  
  const email = document.createElement('p');
  email.textContent = user.email;
  
  const button = document.createElement('button');
  button.textContent = 'View Profile';
  button.onclick = () => viewProfile(user.id);
  
  card.appendChild(name);
  card.appendChild(email);
  card.appendChild(button);
  
  return card;
}

// Using DocumentFragment for performance
function createMultipleElements(items) {
  const fragment = document.createDocumentFragment();
  
  items.forEach(item => {
    const li = document.createElement('li');
    li.textContent = item;
    fragment.appendChild(li);
  });
  
  document.getElementById('list').appendChild(fragment);
}
```

---

### DOM Manipulation
Modifying existing elements.

**Example:**
```javascript
// Modifying content
element.textContent = 'New text';
element.innerHTML = '<strong>Bold text</strong>';

// Modifying attributes
element.setAttribute('title', 'Tooltip');
element.getAttribute('title');
element.removeAttribute('title');
element.hasAttribute('title');

// Modifying classes
element.classList.add('active');
element.classList.remove('inactive');
element.classList.toggle('highlight');
element.classList.contains('active'); // true/false
element.classList.replace('old', 'new');

// Modifying styles
element.style.color = 'red';
element.style.backgroundColor = 'blue';
element.style.cssText = 'color: red; background: blue;';

// Adding/removing elements
parent.appendChild(child);
parent.insertBefore(newChild, referenceChild);
parent.removeChild(child);
parent.replaceChild(newChild, oldChild);
element.remove(); // Modern way

// Cloning
const clone = element.cloneNode(true); // true = deep clone
```

---

### Accessing Element/Node Properties
Reading element information.

**Example:**
```javascript
// Element dimensions and position
const width = element.offsetWidth;  // Width including padding and border
const height = element.offsetHeight;
const clientWidth = element.clientWidth; // Width excluding border
const scrollWidth = element.scrollWidth; // Total scrollable width

const rect = element.getBoundingClientRect();
console.log(rect.top, rect.left, rect.width, rect.height);

// Scroll position
const scrollTop = element.scrollTop;
const scrollLeft = element.scrollLeft;

// Computed styles
const styles = window.getComputedStyle(element);
console.log(styles.color);
console.log(styles.fontSize);

// Data attributes
element.dataset.userId = '123'; // <div data-user-id="123">
console.log(element.dataset.userId);

// Form properties
const input = document.querySelector('input');
console.log(input.value);
console.log(input.checked); // For checkboxes
console.log(input.disabled);
```

---

### Event Delegation
Handling events efficiently using event bubbling.

**Example:**
```javascript
// Without delegation (inefficient for many elements)
document.querySelectorAll('.button').forEach(button => {
  button.addEventListener('click', handleClick);
});

// With delegation (efficient)
document.getElementById('container').addEventListener('click', (e) => {
  if (e.target.matches('.button')) {
    handleClick(e);
  }
});

// Real-world example: Todo list
document.getElementById('todoList').addEventListener('click', (e) => {
  const target = e.target;
  
  // Delete button
  if (target.matches('.delete-btn')) {
    target.closest('.todo-item').remove();
  }
  
  // Complete checkbox
  if (target.matches('.complete-checkbox')) {
    target.closest('.todo-item').classList.toggle('completed');
  }
  
  // Edit button
  if (target.matches('.edit-btn')) {
    const todoItem = target.closest('.todo-item');
    editTodo(todoItem);
  }
});

// Dynamic content example
document.getElementById('userList').addEventListener('click', (e) => {
  if (e.target.matches('[data-user-id]')) {
    const userId = e.target.dataset.userId;
    loadUserDetails(userId);
  }
});

// Event delegation with multiple events
function addDelegatedListener(parent, eventType, selector, handler) {
  parent.addEventListener(eventType, (e) => {
    if (e.target.matches(selector)) {
      handler(e);
    }
  });
}

// Usage
addDelegatedListener(document, 'click', '.btn', handleButtonClick);
addDelegatedListener(document, 'input', '.search', handleSearch);
```

---

## Runtime APIs

### setTimeout()
Executes code after a specified delay.

**Example:**
```javascript
// Basic usage
setTimeout(() => {
  console.log('Executed after 2 seconds');
}, 2000);

// With arguments
function greet(name, greeting) {
  console.log(`${greeting}, ${name}!`);
}
setTimeout(greet, 1000, 'John', 'Hello');

// Clearing timeout
const timeoutId = setTimeout(() => {
  console.log('This might not execute');
}, 5000);

// Cancel it
clearTimeout(timeoutId);

// Real-world example: Toast notification
function showToast(message, duration = 3000) {
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = message;
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.remove();
  }, duration);
}

// Debounce helper (prevent rapid execution)
function debounce(func, delay) {
  let timeoutId;
  return function(...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func.apply(this, args), delay);
  };
}

// Usage
const debouncedSearch = debounce((query) => {
  console.log('Searching for:', query);
}, 500);
```

---

### setInterval()
Executes code repeatedly at specified intervals.

**Example:**
```javascript
// Basic usage
const intervalId = setInterval(() => {
  console.log('Executed every 2 seconds');
}, 2000);

// Clearing interval
clearInterval(intervalId);

// Real-world example: Clock
function startClock() {
  function updateClock() {
    const now = new Date();
    const timeString = now.toLocaleTimeString();
    document.getElementById('clock').textContent = timeString;
  }
  
  updateClock(); // Initial call
  return setInterval(updateClock, 1000);
}

const clockInterval = startClock();

// Stop clock
// clearInterval(clockInterval);

// Countdown timer
function countdown(seconds) {
  let remaining = seconds;
  
  const interval = setInterval(() => {
    console.log(remaining);
    remaining--;
    
    if (remaining < 0) {
      clearInterval(interval);
      console.log('Countdown finished!');
    }
  }, 1000);
  
  return interval;
}

countdown(10);

// Throttle helper (limit execution rate)
function throttle(func, limit) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

// Usage
const throttledScroll = throttle(() => {
  console.log('Scroll event');
}, 1000);

window.addEventListener('scroll', throttledScroll);
```

---

### fetch()
Makes HTTP requests and returns a Promise.

**Example:**
```javascript
// Basic GET request
fetch('https://api.example.com/users')
  .then(response => {
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  })
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));

// With async/await
async function getUsers() {
  try {
    const response = await fetch('https://api.example.com/users');
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Failed to fetch users:', error);
    throw error;
  }
}

// POST request
async function createUser(userData) {
  try {
    const response = await fetch('https://api.example.com/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer token123'
      },
      body: JSON.stringify(userData)
    });
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Failed to create user:', error);
    throw error;
  }
}

// PUT request
async function updateUser(userId, updates) {
  const response = await fetch(`https://api.example.com/users/${userId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updates)
  });
  return response.json();
}

// DELETE request
async function deleteUser(userId) {
  const response = await fetch(`https://api.example.com/users/${userId}`, {
    method: 'DELETE'
  });
  return response.ok;
}

// With timeout
async function fetchWithTimeout(url, timeout = 5000) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);
  
  try {
    const response = await fetch(url, {
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    return response.json();
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error('Request timeout');
    }
    throw error;
  }
}

// Retry mechanism
async function fetchWithRetry(url, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      const response = await fetch(url);
      if (response.ok) return response.json();
    } catch (error) {
      if (i === retries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

---

## Practice Questions

### 1. Debounce
**Problem:** Implement a debounce function that delays executing a function until after a specified time has elapsed since the last invocation.

**Link:** https://lnkd.in/gs-f9jcP

**Solution:**
```javascript
function debounce(func, delay) {
  let timeoutId;
  
  return function debounced(...args) {
    // Clear previous timer
    clearTimeout(timeoutId);
    
    // Set new timer
    timeoutId = setTimeout(() => {
      func.apply(this, args);
    }, delay);
  };
}

// Usage example
const expensiveOperation = (query) => {
  console.log('Searching for:', query);
  // Simulate API call
};

const debouncedSearch = debounce(expensiveOperation, 500);

// These calls will only execute once after 500ms of inactivity
debouncedSearch('a');
debouncedSearch('ab');
debouncedSearch('abc'); // Only this executes

// Real-world usage
const searchInput = document.querySelector('#search');
searchInput.addEventListener('input', debounce((e) => {
  searchAPI(e.target.value);
}, 300));
```

---

### 2. Classnames
**Problem:** Implement a function to conditionally join class names together.

**Link:** https://lnkd.in/gtqadcR4

**Solution:**
```javascript
function classnames(...args) {
  const classes = [];
  
  args.forEach(arg => {
    if (!arg) return;
    
    const argType = typeof arg;
    
    if (argType === 'string' || argType === 'number') {
      classes.push(arg);
    } else if (Array.isArray(arg)) {
      const inner = classnames(...arg);
      if (inner) classes.push(inner);
    } else if (argType === 'object') {
      Object.keys(arg).forEach(key => {
        if (arg[key]) {
          classes.push(key);
        }
      });
    }
  });
  
  return classes.join(' ');
}

// Usage examples
console.log(classnames('foo', 'bar')); 
// 'foo bar'

console.log(classnames('foo', { bar: true, duck: false }, 'baz')); 
// 'foo bar baz'

console.log(classnames({ 'foo-bar': true })); 
// 'foo-bar'

console.log(classnames({ foo: true }, { bar: true })); 
// 'foo bar'

console.log(classnames({ foo: true, bar: true })); 
// 'foo bar'

console.log(classnames(['foo', 'bar'])); 
// 'foo bar'

// Real-world React example
function Button({ primary, large, disabled, className, children }) {
  const btnClass = classnames(
    'btn',
    {
      'btn-primary': primary,
      'btn-large': large,
      'btn-disabled': disabled
    },
    className
  );
  
  return <button className={btnClass}>{children}</button>;
}
```

---

### 3. Flatten
**Problem:** Implement a function to flatten a nested array.

**Link:** https://lnkd.in/gQuh7JuE

**Solution:**
```javascript
// Recursive approach
function flatten(arr) {
  const result = [];
  
  for (const item of arr) {
    if (Array.isArray(item)) {
      result.push(...flatten(item));
    } else {
      result.push(item);
    }
  }
  
  return result;
}

// Alternative with reduce
function flattenReduce(arr) {
  return arr.reduce((acc, item) => {
    return acc.concat(Array.isArray(item) ? flattenReduce(item) : item);
  }, []);
}

// With depth control
function flattenDepth(arr, depth = 1) {
  if (depth === 0) return arr;
  
  return arr.reduce((acc, item) => {
    return acc.concat(
      Array.isArray(item) ? flattenDepth(item, depth - 1) : item
    );
  }, []);
}

// Using stack (iterative)
function flattenIterative(arr) {
  const stack = [...arr];
  const result = [];
  
  while (stack.length) {
    const item = stack.pop();
    
    if (Array.isArray(item)) {
      stack.push(...item);
    } else {
      result.unshift(item);
    }
  }
  
  return result;
}

// Test cases
console.log(flatten([1, [2, 3], [4, [5, 6]]])); 
// [1, 2, 3, 4, 5, 6]

console.log(flattenDepth([1, [2, [3, [4]]]], 2)); 
// [1, 2, 3, [4]]

console.log(flatten([[1], [2], [3]])); 
// [1, 2, 3]

// Native method (ES2019+)
console.log([1, [2, [3, [4]]]].flat(Infinity)); 
// [1, 2, 3, 4]
```

---

### 4. Throttle
**Problem:** Implement a throttle function that limits function execution to once per specified time period.

**Link:** https://lnkd.in/g-528va2

**Solution:**
```javascript
function throttle(func, limit) {
  let inThrottle;
  let lastFunc;
  let lastRan;
  
  return function throttled(...args) {
    const context = this;
    
    if (!inThrottle) {
      func.apply(context, args);
      lastRan = Date.now();
      inThrottle = true;
    } else {
      clearTimeout(lastFunc);
      lastFunc = setTimeout(() => {
        if (Date.now() - lastRan >= limit) {
          func.apply(context, args);
          lastRan = Date.now();
        }
      }, limit - (Date.now() - lastRan));
    }
  };
}

// Simpler version (executes at start of period)
function throttleSimple(func, limit) {
  let inThrottle;
  
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}

// Usage example
const logScroll = () => {
  console.log('Scroll event at:', Date.now());
};

const throttledScroll = throttle(logScroll, 1000);

window.addEventListener('scroll', throttledScroll);

// Real-world: Resize handler
const handleResize = throttle(() => {
  console.log('Window size:', window.innerWidth, window.innerHeight);
  // Update layout
}, 250);

window.addEventListener('resize', handleResize);

// API rate limiting
const apiCall = throttle(async (data) => {
  const response = await fetch('/api/data', {
    method: 'POST',
    body: JSON.stringify(data)
  });
  return response.json();
}, 1000);
```

---

### 5. Promise.all
**Problem:** Implement your own version of Promise.all.

**Link:** https://lnkd.in/gHxEydem

**Solution:**
```javascript
function promiseAll(promises) {
  return new Promise((resolve, reject) => {
    if (!Array.isArray(promises)) {
      return reject(new TypeError('Argument must be an array'));
    }
    
    const results = [];
    let completedCount = 0;
    const totalPromises = promises.length;
    
    if (totalPromises === 0) {
      return resolve([]);
    }
    
    promises.forEach((promise, index) => {
      // Wrap in Promise.resolve to handle non-promise values
      Promise.resolve(promise)
        .then(value => {
          results[index] = value;
          completedCount++;
          
          if (completedCount === totalPromises) {
            resolve(results);
          }
        })
        .catch(error => {
          reject(error);
        });
    });
  });
}

// Test cases
const p1 = Promise.resolve(1);
const p2 = new Promise(resolve => setTimeout(() => resolve(2), 100));
const p3 = Promise.resolve(3);

promiseAll([p1, p2, p3])
  .then(results => console.log(results)) // [1, 2, 3]
  .catch(error => console.error(error));

// With rejection
const p4 = Promise.reject('Error!');
promiseAll([p1, p2, p4])
  .then(results => console.log(results))
  .catch(error => console.error(error)); // 'Error!'

// With non-promise values
promiseAll([1, 2, 3, Promise.resolve(4)])
  .then(results => console.log(results)); // [1, 2, 3, 4]

// Real-world usage
async function fetchMultipleUsers(userIds) {
  const promises = userIds.map(id => 
    fetch(`/api/users/${id}`).then(r => r.json())
  );
  
  try {
    const users = await promiseAll(promises);
    return users;
  } catch (error) {
    console.error('Failed to fetch users:', error);
    throw error;
  }
}
```

---

### 6. Deep Clone
**Problem:** Implement a function to deep clone an object.

**Link:** https://lnkd.in/g5Bn2wMV

**Solution:**
```javascript
function deepClone(obj, hash = new WeakMap()) {
  // Handle null and primitives
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }
  
  // Handle circular references
  if (hash.has(obj)) {
    return hash.get(obj);
  }
  
  // Handle Date
  if (obj instanceof Date) {
    return new Date(obj.getTime());
  }
  
  // Handle RegExp
  if (obj instanceof RegExp) {
    return new RegExp(obj.source, obj.flags);
  }
  
  // Handle Array
  if (Array.isArray(obj)) {
    const arrCopy = [];
    hash.set(obj, arrCopy);
    
    obj.forEach((item, index) => {
      arrCopy[index] = deepClone(item, hash);
    });
    
    return arrCopy;
  }
  
  // Handle Map
  if (obj instanceof Map) {
    const mapCopy = new Map();
    hash.set(obj, mapCopy);
    
    obj.forEach((value, key) => {
      mapCopy.set(key, deepClone(value, hash));
    });
    
    return mapCopy;
  }
  
  // Handle Set
  if (obj instanceof Set) {
    const setCopy = new Set();
    hash.set(obj, setCopy);
    
    obj.forEach(value => {
      setCopy.add(deepClone(value, hash));
    });
    
    return setCopy;
  }
  
  // Handle Object
  const objCopy = Object.create(Object.getPrototypeOf(obj));
  hash.set(obj, objCopy);
  
  Object.keys(obj).forEach(key => {
    objCopy[key] = deepClone(obj[key], hash);
  });
  
  // Copy symbols
  Object.getOwnPropertySymbols(obj).forEach(symbol => {
    objCopy[symbol] = deepClone(obj[symbol], hash);
  });
  
  return objCopy;
}

// Test cases
const original = {
  name: 'John',
  age: 30,
  hobbies: ['reading', 'coding'],
  address: {
    city: 'New York',
    country: 'USA'
  },
  date: new Date(),
  regex: /test/gi,
  map: new Map([['key', 'value']]),
  set: new Set([1, 2, 3])
};

// Circular reference
original.self = original;

const cloned = deepClone(original);

console.log(cloned.name); // 'John'
console.log(cloned.hobbies === original.hobbies); // false
console.log(cloned.self === cloned); // true (circular ref maintained)

// Simpler version (doesn't handle all edge cases)
function deepCloneSimple(obj) {
  return JSON.parse(JSON.stringify(obj));
  // Limitations: no functions, dates become strings, no circular refs
}
```

---

### 7. Event Emitter
**Problem:** Implement an event emitter class.

**Link:** https://lnkd.in/gnfc3WtW

**Solution:**
```javascript
class EventEmitter {
  constructor() {
    this.events = {};
  }
  
  // Subscribe to event
  on(eventName, callback) {
    if (!this.events[eventName]) {
      this.events[eventName] = [];
    }
    
    this.events[eventName].push(callback);
    
    // Return unsubscribe function
    return () => {
      this.off(eventName, callback);
    };
  }
  
  // Subscribe to event (fires once)
  once(eventName, callback) {
    const wrapper = (...args) => {
      callback(...args);
      this.off(eventName, wrapper);
    };
    
    this.on(eventName, wrapper);
  }
  
  // Unsubscribe from event
  off(eventName, callback) {
    if (!this.events[eventName]) return;
    
    this.events[eventName] = this.events[eventName].filter(
      cb => cb !== callback
    );
  }
  
  // Emit event
  emit(eventName, ...args) {
    if (!this.events[eventName]) return;
    
    this.events[eventName].forEach(callback => {
      callback(...args);
    });
  }
  
  // Remove all listeners
  removeAllListeners(eventName) {
    if (eventName) {
      delete this.events[eventName];
    } else {
      this.events = {};
    }
  }
  
  // Get listener count
  listenerCount(eventName) {
    return this.events[eventName]?.length || 0;
  }
}

// Usage example
const emitter = new EventEmitter();

// Subscribe to events
const unsubscribe = emitter.on('userLogin', (user) => {
  console.log(`User logged in: ${user.name}`);
});

emitter.on('userLogin', (user) => {
  console.log(`Welcome, ${user.name}!`);
});

// One-time listener
emitter.once('appStart', () => {
  console.log('App started!');
});

// Emit events
emitter.emit('userLogin', { name: 'John', id: 1 });
// Output: 
// User logged in: John
// Welcome, John!

emitter.emit('appStart'); // App started!
emitter.emit('appStart'); // (nothing happens)

// Unsubscribe
unsubscribe();
emitter.emit('userLogin', { name: 'Jane', id: 2 });
// Output: Welcome, Jane! (first listener removed)

// Real-world usage
class Store extends EventEmitter {
  constructor() {
    super();
    this.state = {};
  }
  
  setState(newState) {
    this.state = { ...this.state, ...newState };
    this.emit('stateChange', this.state);
  }
  
  getState() {
    return this.state;
  }
}

const store = new Store();

store.on('stateChange', (state) => {
  console.log('State updated:', state);
});

store.setState({ user: 'John' });
```

---

### 8. Map Async Limit
**Problem:** Implement a function to map over an array with async operations, limiting concurrent executions.

**Link:** https://lnkd.in/gMtk8qrq

**Solution:**
```javascript
async function mapAsyncLimit(array, limit, asyncFn) {
  const results = [];
  const executing = [];
  
  for (const [index, item] of array.entries()) {
    // Create promise for current item
    const promise = Promise.resolve().then(() => asyncFn(item, index, array));
    results.push(promise);
    
    // If we're at the limit, wait for one to complete
    if (limit <= array.length) {
      const executing = promise.then(() => 
        executing.splice(executing.indexOf(executing), 1)
      );
      
      executing.push(executing);
      
      if (executing.length >= limit) {
        await Promise.race(executing);
      }
    }
  }
  
  return Promise.all(results);
}

// Alternative implementation
async function mapAsyncLimitAlt(array, limit, asyncFn) {
  const results = new Array(array.length);
  let index = 0;
  
  async function runWorker() {
    while (index < array.length) {
      const currentIndex = index++;
      results[currentIndex] = await asyncFn(array[currentIndex], currentIndex, array);
    }
  }
  
  // Create limited number of workers
  const workers = Array(Math.min(limit, array.length))
    .fill(null)
    .map(() => runWorker());
  
  await Promise.all(workers);
  return results;
}

// Usage example
async function fetchUser(id) {
  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1000));
  return { id, name: `User ${id}` };
}

const userIds = [1, 2, 3, 4, 5, 6, 7, 8];

// Only 3 concurrent requests at a time
mapAsyncLimit(userIds, 3, fetchUser)
  .then(users => console.log(users))
  .catch(error => console.error(error));

// Real-world: Image processing
async function processImage(url) {
  const response = await fetch(url);
  const blob = await response.blob();
  // Process image...
  return { url, processed: true };
}

const imageUrls = [
  'image1.jpg', 'image2.jpg', 'image3.jpg', 
  'image4.jpg', 'image5.jpg'
];

// Process max 2 images at a time
const processedImages = await mapAsyncLimit(
  imageUrls, 
  2, 
  processImage
);

// Batch API requests
async function batchProcess(items, batchSize) {
  const results = [];
  
  for (let i = 0; i < items.length; i += batchSize) {
    const batch = items.slice(i, i + batchSize);
    const batchResults = await Promise.all(
      batch.map(item => processItem(item))
    );
    results.push(...batchResults);
  }
  
  return results;
}
```

---

### 9. Deep Equal
**Problem:** Implement a function to check deep equality between two values.

**Link:** https://lnkd.in/gq4psMYF

**Solution:**
```javascript
function deepEqual(a, b) {
  // Same reference
  if (a === b) return true;
  
  // Handle null
  if (a === null || b === null) return false;
  
  // Different types
  if (typeof a !== typeof b) return false;
  
  // Primitives (already checked with ===)
  if (typeof a !== 'object') return false;
  
  // Handle Date
  if (a instanceof Date && b instanceof Date) {
    return a.getTime() === b.getTime();
  }
  
  // Handle RegExp
  if (a instanceof RegExp && b instanceof RegExp) {
    return a.toString() === b.toString();
  }
  
  // Handle Array
  if (Array.isArray(a) && Array.isArray(b)) {
    if (a.length !== b.length) return false;
    
    for (let i = 0; i < a.length; i++) {
      if (!deepEqual(a[i], b[i])) return false;
    }
    
    return true;
  }
  
  // Handle Map
  if (a instanceof Map && b instanceof Map) {
    if (a.size !== b.size) return false;
    
    for (const [key, value] of a) {
      if (!b.has(key) || !deepEqual(value, b.get(key))) {
        return false;
      }
    }
    
    return true;
  }
  
  // Handle Set
  if (a instanceof Set && b instanceof Set) {
    if (a.size !== b.size) return false;
    
    for (const value of a) {
      if (!b.has(value)) return false;
    }
    
    return true;
  }
  
  // Handle Object
  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  
  if (keysA.length !== keysB.length) return false;
  
  for (const key of keysA) {
    if (!keysB.includes(key)) return false;
    if (!deepEqual(a[key], b[key])) return false;
  }
  
  return true;
}

// Test cases
console.log(deepEqual(1, 1)); // true
console.log(deepEqual(1, '1')); // false
console.log(deepEqual([1, 2, 3], [1, 2, 3])); // true
console.log(deepEqual([1, 2, 3], [1, 2, 4])); // false

console.log(deepEqual(
  { a: 1, b: { c: 2 } },
  { a: 1, b: { c: 2 } }
)); // true

console.log(deepEqual(
  { a: 1, b: { c: 2 } },
  { a: 1, b: { c: 3 } }
)); // false

console.log(deepEqual(
  new Date('2024-01-01'),
  new Date('2024-01-01')
)); // true

console.log(deepEqual(
  new Map([['a', 1]]),
  new Map([['a', 1]])
)); // true

console.log(deepEqual(
  new Set([1, 2, 3]),
  new Set([1, 2, 3])
)); // true

// Real-world usage
function hasStateChanged(prevState, nextState) {
  return !deepEqual(prevState, nextState);
}

// Testing
function assertEqual(actual, expected) {
  if (!deepEqual(actual, expected)) {
    throw new Error(`Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}
```

---

### 10. Memoize
**Problem:** Implement a memoization function to cache function results.

**Link:** https://lnkd.in/gWHM-ku9

**Solution:**
```javascript
function memoize(fn) {
  const cache = new Map();
  
  return function memoized(...args) {
    const key = JSON.stringify(args);
    
    if (cache.has(key)) {
      console.log('Returning from cache');
      return cache.get(key);
    }
    
    const result = fn.apply(this, args);
    cache.set(key, result);
    return result;
  };
}

// Advanced version with custom key generator
function memoizeAdvanced(fn, options = {}) {
  const {
    maxSize = Infinity,
    keyGenerator = JSON.stringify,
    ttl = null // Time to live in ms
  } = options;
  
  const cache = new Map();
  const timestamps = new Map();
  
  return function memoized(...args) {
    const key = keyGenerator(args);
    
    // Check TTL
    if (ttl && timestamps.has(key)) {
      const age = Date.now() - timestamps.get(key);
      if (age > ttl) {
        cache.delete(key);
        timestamps.delete(key);
      }
    }
    
    if (cache.has(key)) {
      return cache.get(key);
    }
    
    const result = fn.apply(this, args);
    
    // Implement LRU eviction if needed
    if (cache.size >= maxSize) {
      const firstKey = cache.keys().next().value;
      cache.delete(firstKey);
      timestamps.delete(firstKey);
    }
    
    cache.set(key, result);
    if (ttl) timestamps.set(key, Date.now());
    
    return result;
  };
}

// Usage examples

// Expensive computation
function fibonacci(n) {
  if (n <= 1) return n;
  return fibonacci(n - 1) + fibonacci(n - 2);
}

const memoizedFib = memoize(fibonacci);

console.time('First call');
console.log(memoizedFib(40)); // Takes time
console.timeEnd('First call');

console.time('Second call');
console.log(memoizedFib(40)); // Instant (from cache)
console.timeEnd('Second call');

// API calls
const fetchUser = memoize(async (userId) => {
  console.log('Fetching user from API...');
  const response = await fetch(`/api/users/${userId}`);
  return response.json();
});

// First call hits API
await fetchUser(1);

// Second call returns cached result
await fetchUser(1);

// With TTL (cache expires after 5 seconds)
const cachedFetch = memoizeAdvanced(fetch, {
  ttl: 5000,
  maxSize: 100,
  keyGenerator: (args) => args[0] // Use URL as key
});

// Real-world: Expensive calculations
function calculateComplexMetrics(data) {
  console.log('Performing complex calculation...');
  // Expensive operation
  return data.reduce((sum, val) => sum + val * Math.sqrt(val), 0);
}

const memoizedCalc = memoize(calculateComplexMetrics);

const data = [1, 2, 3, 4, 5];
memoizedCalc(data); // Calculates
memoizedCalc(data); // Returns from cache

// Method memoization
class DataProcessor {
  constructor() {
    this.process = memoize(this.process.bind(this));
  }
  
  process(data) {
    console.log('Processing data...');
    return data.map(x => x * 2);
  }
}

const processor = new DataProcessor();
processor.process([1, 2, 3]); // Processes
processor.process([1, 2, 3]); // Cached
```

---

### 11. Curry
**Problem:** Implement a curry function that transforms a function to be callable in multiple ways.

**Link:** https://lnkd.in/g48WfADC

**Solution:**
```javascript
function curry(fn) {
  return function curried(...args) {
    // If we have all arguments, call the function
    if (args.length >= fn.length) {
      return fn.apply(this, args);
    }
    
    // Otherwise, return a new function that collects more arguments
    return function(...moreArgs) {
      return curried.apply(this, args.concat(moreArgs));
    };
  };
}

// Advanced version with placeholder support
function curryAdvanced(fn, arity = fn.length) {
  const PLACEHOLDER = Symbol('curry-placeholder');
  
  return function curried(...args) {
    // Replace placeholders with actual values
    const resolvedArgs = args.map((arg, i) => 
      arg === PLACEHOLDER ? args[i] : arg
    );
    
    // Count non-placeholder arguments
    const validArgs = resolvedArgs.filter(arg => arg !== PLACEHOLDER);
    
    if (validArgs.length >= arity) {
      return fn.apply(this, resolvedArgs.slice(0, arity));
    }
    
    return function(...nextArgs) {
      const newArgs = resolvedArgs.map((arg, i) =>
        arg === PLACEHOLDER && nextArgs.length > 0 ? nextArgs.shift() : arg
      );
      return curried(...newArgs, ...nextArgs);
    };
  };
  
  curried.PLACEHOLDER = PLACEHOLDER;
  return curried;
}

// Usage examples

// Basic curry
function add(a, b, c) {
  return a + b + c;
}

const curriedAdd = curry(add);

console.log(curriedAdd(1)(2)(3)); // 6
console.log(curriedAdd(1, 2)(3)); // 6
console.log(curriedAdd(1)(2, 3)); // 6
console.log(curriedAdd(1, 2, 3)); // 6

// Partial application
const add5 = curriedAdd(5);
console.log(add5(3, 2)); // 10
console.log(add5(10, 20)); // 35

// Real-world examples

// 1. Building URLs
function buildUrl(protocol, domain, path) {
  return `${protocol}://${domain}/${path}`;
}

const curriedBuildUrl = curry(buildUrl);
const httpsUrl = curriedBuildUrl('https');
const apiUrl = httpsUrl('api.example.com');

console.log(apiUrl('users')); // https://api.example.com/users
console.log(apiUrl('posts')); // https://api.example.com/posts

// 2. Event handlers
function handleEvent(eventType, selector, handler, event) {
  if (event.target.matches(selector)) {
    handler(event);
  }
}

const curriedHandle = curry(handleEvent);
const handleClick = curriedHandle('click');
const handleButtonClick = handleClick('.button');

document.addEventListener('click', handleButtonClick((e) => {
  console.log('Button clicked!', e);
}));

// 3. Logging
function log(level, timestamp, message) {
  console.log(`[${level}] ${timestamp}: ${message}`);
}

const curriedLog = curry(log);
const errorLog = curriedLog('ERROR');
const timedErrorLog = errorLog(new Date().toISOString());

timedErrorLog('Something went wrong'); 
// [ERROR] 2024-01-01T12:00:00.000Z: Something went wrong

// 4. Data transformation
function map(fn, array) {
  return array.map(fn);
}

const curriedMap = curry(map);
const double = curriedMap(x => x * 2);

console.log(double([1, 2, 3])); // [2, 4, 6]
console.log(double([5, 10, 15])); // [10, 20, 30]

// 5. Filtering
function filter(predicate, array) {
  return array.filter(predicate);
}

const curriedFilter = curry(filter);
const getEvens = curriedFilter(x => x % 2 === 0);

console.log(getEvens([1, 2, 3, 4, 5])); // [2, 4]

// 6. Composition with curry
const compose = (...fns) => x => fns.reduceRight((v, f) => f(v), x);

const addTen = x => x + 10;
const multiplyByTwo = x => x * 2;
const square = x => x * x;

const calculate = compose(square, multiplyByTwo, addTen);
console.log(calculate(5)); // ((5 + 10) * 2)^2 = 900

// With placeholder (advanced)
const _ = curryAdvanced.PLACEHOLDER;
const curriedAddAdv = curryAdvanced(add);

console.log(curriedAddAdv(1, _, 3)(2)); // 6
console.log(curriedAddAdv(_, 2, _)(1, 3)); // 6
```

---

## Additional Resources

### Performance Tips
- Use `const` and `let` instead of `var`
- Avoid global variables
- Use event delegation for dynamic content
- Debounce/throttle expensive operations
- Use `requestAnimationFrame` for animations
- Minimize DOM manipulation
- Use Web Workers for heavy computations

### Best Practices
- Write pure functions when possible
- Avoid callback hell (use Promises/async-await)
- Handle errors properly (try-catch, .catch())
- Use meaningful variable names
- Comment complex logic
- Keep functions small and focused
- Follow DRY principle (Don't Repeat Yourself)

### Modern JavaScript Features
- Destructuring
- Spread/Rest operators
- Template literals
- Optional chaining (`?.`)
- Nullish coalescing (`??`)
- Array methods (map, filter, reduce, find, etc.)
- Promises and async/await
- Modules (import/export)
- Classes

Here’s a **complete GitHub-style Markdown (`.md`) file** containing **FAANG-level JavaScript interview questions** with **answers, explanations, and categorized topics** — designed for deep understanding and hands-on preparation.

---

````markdown
# 🚀 JavaScript Interview Questions for FAANG & Product-Based Companies

A comprehensive guide to advanced JavaScript questions with solutions and explanations — to help you ace technical interviews at companies like **Google, Meta, Amazon, Apple, Netflix**, and other product-based organizations.

---

## 🧠 Core JavaScript Concepts

### 1. What is the difference between `var`, `let`, and `const`?
**Answer:**
- `var` → Function-scoped, hoisted, can be redeclared.
- `let` → Block-scoped, hoisted but not initialized (Temporal Dead Zone).
- `const` → Block-scoped, cannot be reassigned or redeclared.

**Example:**
```js
for (var i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 1000);
}
// Output: 3 3 3

for (let i = 0; i < 3; i++) {
  setTimeout(() => console.log(i), 1000);
}
// Output: 0 1 2
````

---

### 2. What is hoisting in JavaScript?

**Answer:**
Hoisting is a mechanism where variable and function declarations are moved to the top of their scope during compilation.

**Example:**

```js
console.log(a); // undefined
var a = 5;

// Behind the scenes:
var a;
console.log(a);
a = 5;
```

---

### 3. Explain the difference between shallow copy and deep copy.

**Answer:**

* **Shallow copy:** Copies only the reference of nested objects.
* **Deep copy:** Recursively copies all nested objects.

**Example:**

```js
const obj = { name: "Alice", address: { city: "Paris" } };
const shallow = { ...obj };
shallow.address.city = "London";
console.log(obj.address.city); // London

const deep = JSON.parse(JSON.stringify(obj));
deep.address.city = "Tokyo";
console.log(obj.address.city); // London
```

---

## ⚙️ Advanced Concepts

### 4. What is event loop in JavaScript?

**Answer:**
Event loop allows **non-blocking I/O** by handling async operations using **callback queue** and **microtask queue**.

**Execution Order:**

1. Call Stack (sync code)
2. Microtask Queue (Promises, MutationObserver)
3. Callback Queue (setTimeout, setInterval)

**Example:**

```js
console.log("Start");
setTimeout(() => console.log("Timeout"), 0);
Promise.resolve().then(() => console.log("Promise"));
console.log("End");

// Output: Start → End → Promise → Timeout
```

---

### 5. What is a closure and how is it used?

**Answer:**
A closure is created when a function remembers the variables from its lexical scope even after that function executes.

**Example:**

```js
function counter() {
  let count = 0;
  return function () {
    return ++count;
  };
}
const c = counter();
console.log(c()); // 1
console.log(c()); // 2
```

Used in data hiding, memoization, and function factories.

---

### 6. Explain `this` in different contexts.

**Answer:**

* **Global context:** `this` → `window` (in browsers)
* **Object method:** `this` → the object
* **Arrow function:** Inherits `this` from parent scope
* **Event listener:** Refers to the DOM element

**Example:**

```js
const obj = {
  name: "JS",
  normal() { console.log(this.name); },
  arrow: () => console.log(this.name)
};
obj.normal(); // JS
obj.arrow();  // undefined
```

---

## 🔁 Asynchronous JavaScript

### 7. Explain async/await vs Promises.

**Answer:**
`async/await` is syntactic sugar over Promises to make asynchronous code look synchronous.

**Example:**

```js
async function getData() {
  try {
    const res = await fetch("https://api.github.com");
    const data = await res.json();
    console.log(data);
  } catch (err) {
    console.error(err);
  }
}
```

---

### 8. What are microtasks and macrotasks?

**Answer:**

* **Microtasks:** Promises, MutationObserver
* **Macrotasks:** setTimeout, setInterval, setImmediate

Execution order → All microtasks run before next macrotask.

---

## 🧩 Functional Programming

### 9. Explain higher-order functions.

**Answer:**
Functions that take another function as argument or return a function.

**Example:**

```js
function multiply(factor) {
  return function (x) {
    return x * factor;
  };
}
const double = multiply(2);
console.log(double(5)); // 10
```

---

### 10. Difference between `map`, `forEach`, `filter`, and `reduce`.

| Method      | Returns        | Modifies Original Array | Use Case                  |
| ----------- | -------------- | ----------------------- | ------------------------- |
| `map()`     | New array      | ❌                       | Transform elements        |
| `forEach()` | `undefined`    | ✅                       | Side effects              |
| `filter()`  | Filtered array | ❌                       | Condition-based selection |
| `reduce()`  | Single value   | ❌                       | Aggregation               |

---

## 🧱 Object-Oriented JS

### 11. What are prototypes in JavaScript?

**Answer:**
Prototypes enable inheritance. Every object has an internal link to another object called its prototype.

**Example:**

```js
function Person(name) {
  this.name = name;
}
Person.prototype.sayHi = function() {
  console.log("Hi " + this.name);
};

const p = new Person("Alice");
p.sayHi(); // Hi Alice
```

---

### 12. How does the `new` keyword work?

1. Creates a new empty object
2. Sets the prototype
3. Binds `this` to the new object
4. Returns the new object

**Example:**

```js
function Car(model) {
  this.model = model;
}
const c = new Car("Tesla");
```

---

## 🧮 Data Structures & Algorithms (JS Implementation)

### 13. Reverse a string without using built-in methods.

```js
function reverse(str) {
  let res = '';
  for (let i = str.length - 1; i >= 0; i--) {
    res += str[i];
  }
  return res;
}
```

---

### 14. Implement a debounce function.

**Use case:** Prevents function from being called too frequently.

```js
function debounce(fn, delay) {
  let timer;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}
```

---

### 15. Implement a throttle function.

**Use case:** Ensures a function runs at most once per interval.

```js
function throttle(fn, delay) {
  let last = 0;
  return function(...args) {
    const now = Date.now();
    if (now - last >= delay) {
      fn.apply(this, args);
      last = now;
    }
  };
}
```

---

## 🧩 Coding Round Examples

### 16. Flatten a nested array.

```js
function flatten(arr) {
  return arr.reduce(
    (acc, val) => acc.concat(Array.isArray(val) ? flatten(val) : val),
    []
  );
}
flatten([1, [2, [3, 4]], 5]); // [1, 2, 3, 4, 5]
```

---

### 17. Deep clone an object.

```js
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj;
  const clone = Array.isArray(obj) ? [] : {};
  for (let key in obj) {
    clone[key] = deepClone(obj[key]);
  }
  return clone;
}
```

---

### 18. Implement memoization.

```js
function memoize(fn) {
  const cache = {};
  return function(...args) {
    const key = JSON.stringify(args);
    if (cache[key]) return cache[key];
    cache[key] = fn(...args);
    return cache[key];
  };
}
const fib = memoize(n => (n <= 1 ? n : fib(n-1) + fib(n-2)));
```

---

Perfect 🔥 — here’s your **next-level FAANG-style JavaScript deep-dive document** in full **GitHub Markdown (`.md`)** format.
It includes **Event Delegation**, **Service Workers**, **WeakMap vs Map**, **Garbage Collection**, **Shadow DOM**, **Polyfills**, **Generators & Iterators**, **Proxy & Reflect APIs**, **Pub/Sub model**, **Internal JS working**, and extended **Shallow vs Deep Copy** examples.

---

````markdown
# ⚙️ Advanced JavaScript Deep-Dive — FAANG & Product Company Preparation

This guide covers **in-depth JavaScript internals and modern web engineering concepts** that senior engineers and FAANG-level interviews often test.

---

## 🔁 Event Delegation

### ❓ What is Event Delegation?
**Answer:**
Event delegation is a technique where you attach a single event listener to a **parent element**, which listens for events on its **child elements** via **event bubbling**.

### 💡 Why it's useful:
- Reduces number of event listeners
- Handles dynamic elements (added later)
- Improves performance

**Example:**
```js
document.getElementById("list").addEventListener("click", function(e) {
  if (e.target && e.target.nodeName === "LI") {
    console.log("List item clicked:", e.target.textContent);
  }
});
````

If new `<li>` items are added later, the parent still handles clicks — no re-binding needed.

---

## 🧱 Service Workers & Caching

### ❓ What is a Service Worker?

A **service worker** is a background script that intercepts network requests, enabling features like **offline caching**, **push notifications**, and **background sync**.

### ⚙️ Lifecycle:

1. **Register**
2. **Install** (cache assets)
3. **Activate** (cleanup old caches)
4. **Fetch Intercept** (serve cached or network data)

**Example:**

```js
// Register service worker
navigator.serviceWorker.register("/sw.js");

// sw.js
self.addEventListener("install", (e) => {
  e.waitUntil(
    caches.open("v1").then(cache => cache.addAll(["/", "/index.html", "/app.js"]))
  );
});

self.addEventListener("fetch", (e) => {
  e.respondWith(
    caches.match(e.request).then(response => response || fetch(e.request))
  );
});
```

Used heavily in **PWAs (Progressive Web Apps)**.

---

## 🗺️ WeakMap vs Map

| Feature            | `Map`             | `WeakMap`                           |
| ------------------ | ----------------- | ----------------------------------- |
| Keys               | Any type          | Only **objects**                    |
| Garbage Collection | ❌ Persistent      | ✅ Keys are weakly held              |
| Iteration          | Iterable          | Not iterable                        |
| Use Case           | General key-value | Object-based memory-safe references |

**Example:**

```js
let obj = { name: "Alice" };
let weak = new WeakMap();
weak.set(obj, "metadata");

obj = null; // object is garbage collected automatically
```

Used in frameworks like **React (Fiber memory management)**.

---

## 🧹 Garbage Collection in JS

### ❓ How does Garbage Collection work?

JavaScript uses **automatic garbage collection** — removing objects no longer reachable from the root (`window` or global scope).

### 📦 Algorithms:

* **Mark-and-Sweep:** Marks reachable objects and removes unreferenced ones.
* **Reference Counting:** (older) Removes when ref count hits 0.

**Example of a memory leak:**

```js
let data = [];
function leak() {
  data.push(new Array(1000000).fill("memory"));
}
```

→ Always reachable via `data`, so never collected.

**Tip:** Use `WeakMap` / `WeakSet` to prevent leaks with dynamic objects.

---

## 🌗 Shadow DOM

### ❓ What is Shadow DOM?

It’s a **scoped DOM tree** attached to elements, allowing encapsulated styling and markup.

Used by **Web Components**, **React**, and **LitElement**.

**Example:**

```js
const host = document.querySelector("#host");
const shadow = host.attachShadow({ mode: "open" });
shadow.innerHTML = `
  <style>p { color: red; }</style>
  <p>Hello from Shadow DOM</p>
`;
```

> CSS inside the Shadow DOM doesn’t leak outside and vice versa.

---

## 🧩 Polyfills

### ❓ What is a Polyfill?

A polyfill is **custom JS code that replicates newer JS features** for older browsers that don’t support them.

### Example: Polyfill for `Function.prototype.bind`

```js
if (!Function.prototype.bind) {
  Function.prototype.bind = function(context, ...args) {
    const fn = this;
    return function(...innerArgs) {
      return fn.apply(context, [...args, ...innerArgs]);
    };
  };
}
```

### Example: Polyfill for `Promise.all`

```js
Promise.all = function(promises) {
  return new Promise((resolve, reject) => {
    let results = [];
    let completed = 0;
    promises.forEach((p, i) => {
      Promise.resolve(p)
        .then(res => {
          results[i] = res;
          if (++completed === promises.length) resolve(results);
        })
        .catch(reject);
    });
  });
};
```

---

## ⚙️ Generators & Iterators

### ❓ What are Iterators?

Objects implementing the **Iterator protocol** with a `next()` method returning `{ value, done }`.

### ❓ What are Generators?

Functions that can **pause execution** using `yield` and **resume** later.

**Example:**

```js
function* counter() {
  let i = 0;
  while (i < 3) yield i++;
}

const gen = counter();
console.log(gen.next().value); // 0
console.log(gen.next().value); // 1
console.log(gen.next().value); // 2
```

Used in:

* Lazy evaluation
* Infinite data streams
* Async flow control

---

## 🧭 Proxy & Reflect APIs

### ❓ What is a Proxy?

A `Proxy` wraps an object and intercepts operations like get/set/delete.

**Example:**

```js
const user = { name: "Alice", age: 25 };
const proxy = new Proxy(user, {
  get(target, prop) {
    console.log(`Accessed: ${prop}`);
    return target[prop];
  },
  set(target, prop, value) {
    console.log(`Modified: ${prop}`);
    target[prop] = value;
    return true;
  }
});

proxy.name; // Accessed: name
proxy.age = 30; // Modified: age
```

### 🪞 Reflect API

Used with Proxy for safe meta-operations.

```js
Reflect.set(user, "age", 40);
Reflect.get(user, "age"); // 40
```

---

## 📣 Pub/Sub Model in JavaScript

### ❓ What is Pub/Sub?

**Publish-Subscribe pattern** allows decoupling — publishers send events, and subscribers react to them.

**Example Implementation:**

```js
class PubSub {
  constructor() {
    this.events = {};
  }
  subscribe(event, listener) {
    if (!this.events[event]) this.events[event] = [];
    this.events[event].push(listener);
  }
  publish(event, data) {
    if (this.events[event]) {
      this.events[event].forEach(fn => fn(data));
    }
  }
}

const pubsub = new PubSub();
pubsub.subscribe("msg", data => console.log("Received:", data));
pubsub.publish("msg", "Hello Subscribers!");
```

Used in frameworks and messaging systems like Redux, Node.js EventEmitter, and RxJS.

---

## ⚙️ How JavaScript Internally Works

### 🔹 JavaScript Engine Architecture (e.g., V8)

1. **Parsing → AST (Abstract Syntax Tree)**
2. **Interpreter (Ignition)** → Generates bytecode
3. **Compiler (TurboFan)** → Optimizes hot code paths
4. **Garbage Collector** manages memory
5. **Event Loop + Call Stack** executes async code

### 🧩 Key Components:

* **Call Stack:** Tracks function execution
* **Heap:** Memory allocation area
* **Queue:** Tasks waiting to be executed
* **Microtask Queue:** Promises
* **Web APIs:** Timers, Fetch, DOM

**Example Visualization:**

```text
JS Engine → Call Stack → Web API → Callback Queue → Event Loop → Stack
```

---

## 🧬 Shallow vs Deep Copy (Extended Examples)

### Shallow Copy using Spread

```js
const original = { name: "John", address: { city: "NY" } };
const shallow = { ...original };
shallow.address.city = "Boston";
console.log(original.address.city); // Boston
```

### Deep Copy using recursion

```js
function deepClone(obj) {
  if (obj === null || typeof obj !== "object") return obj;
  const clone = Array.isArray(obj) ? [] : {};
  for (let key in obj) clone[key] = deepClone(obj[key]);
  return clone;
}
```

### Deep Copy with structuredClone (modern)

```js
const deep = structuredClone(original);
deep.address.city = "LA";
console.log(original.address.city); // NY
```

### Deep Copy with Lodash

```js
import cloneDeep from "lodash/cloneDeep";
const deep = cloneDeep(original);
```

---

## 🧠 Summary Cheat Sheet

| Concept               | Key Idea                    | Used In                      |
| --------------------- | --------------------------- | ---------------------------- |
| **Event Delegation**  | Single listener on parent   | Dynamic DOM                  |
| **Service Worker**    | Offline caching, background | PWAs                         |
| **WeakMap**           | Weak refs for GC            | Memory optimization          |
| **Shadow DOM**        | Scoped DOM & CSS            | Web Components               |
| **Proxy & Reflect**   | Intercept object ops        | State management, validation |
| **Generator**         | Pausable function           | Async control, Iterators     |
| **Pub/Sub**           | Decoupled communication     | Redux, EventEmitter          |
| **Polyfill**          | Backward compatibility      | Browser support              |
| **Garbage Collector** | Auto memory management      | Performance stability        |

---
---

**Happy Learning! 🚀**
