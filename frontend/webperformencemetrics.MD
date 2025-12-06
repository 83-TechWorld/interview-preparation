# Web Performance Metrics - Complete Documentation

## Table of Contents
1. [Introduction](#introduction)
2. [Core Web Vitals](#core-web-vitals)
3. [Additional Performance Metrics](#additional-performance-metrics)
4. [Measurement Tools & Libraries](#measurement-tools--libraries)
5. [Enterprise Case Studies](#enterprise-case-studies)
6. [Implementation Guide](#implementation-guide)
7. [Optimization Strategies](#optimization-strategies)
8. [Monitoring & Alerting](#monitoring--alerting)

---

## Introduction

Web performance directly impacts user experience, conversion rates, and SEO rankings. Google uses Core Web Vitals as ranking factors, making performance optimization critical for business success.

### Why Performance Matters

- **1 second delay** = 7% reduction in conversions (Amazon)
- **0.1 second improvement** = 1% increase in revenue (Walmart)
- **53% of mobile users** abandon sites that take longer than 3 seconds to load
- **Core Web Vitals** are official Google ranking factors since 2021

---

## Core Web Vitals

Google's three primary metrics for measuring user experience.

### 1. Largest Contentful Paint (LCP)

**What it measures:** Loading performance - when the largest visible content element renders.

**Thresholds:**
- ✅ Good: ≤ 2.5 seconds
- ⚠️ Needs Improvement: 2.5 - 4.0 seconds
- ❌ Poor: > 4.0 seconds

**Common LCP Elements:**
- `<img>` elements
- `<video>` poster images
- Background images via CSS `url()`
- Block-level text elements

**How to Measure:**
```javascript
import {onLCP} from 'web-vitals';

onLCP((metric) => {
  console.log('LCP:', metric.value);
  // Send to analytics
  sendToAnalytics({
    name: metric.name,
    value: metric.value,
    id: metric.id,
    rating: metric.rating // 'good', 'needs-improvement', 'poor'
  });
});
```

**Common Issues & Fixes:**
- Slow server response → Optimize backend, use CDN
- Render-blocking resources → Defer non-critical CSS/JS
- Slow resource load times → Optimize images, use modern formats (WebP, AVIF)
- Client-side rendering → Use SSR or SSG

**Advanced Optimization:**
```javascript
// Preload LCP image
<link rel="preload" as="image" href="hero.jpg" fetchpriority="high">

// Prioritize LCP image
<img src="hero.jpg" fetchpriority="high" alt="Hero">
```

---

### 2. Interaction to Next Paint (INP)

**What it measures:** Responsiveness - latency of all user interactions throughout page lifecycle.

**Replaces:** First Input Delay (FID) - officially replaced March 2024

**Thresholds:**
- ✅ Good: ≤ 200ms
- ⚠️ Needs Improvement: 200 - 500ms
- ❌ Poor: > 500ms

**What counts as interaction:**
- Mouse clicks
- Taps on touchscreen
- Keyboard presses

**How to Measure:**
```javascript
import {onINP} from 'web-vitals';

onINP((metric) => {
  console.log('INP:', metric.value);
  // Log the specific interaction that caused poor INP
  console.log('Attribution:', metric.attribution);
});
```

**Attribution API (Detailed Debugging):**
```javascript
import {onINP} from 'web-vitals/attribution';

onINP((metric) => {
  const {attribution} = metric;
  console.log('Element:', attribution.interactionTarget); // Which element
  console.log('Type:', attribution.interactionType); // 'pointer', 'keyboard'
  console.log('Input Delay:', attribution.inputDelay); // Time to start processing
  console.log('Processing Time:', attribution.processingDuration); // JS execution
  console.log('Presentation Delay:', attribution.presentationDelay); // Rendering
});
```

**Common Issues & Fixes:**
- Long JavaScript tasks → Break up tasks with `requestIdleCallback` or `scheduler.yield()`
- Heavy event handlers → Debounce/throttle, use passive listeners
- Layout thrashing → Batch DOM reads/writes
- Large bundle sizes → Code splitting, lazy loading

**Task Splitting Example:**
```javascript
// Bad: Long blocking task
function processLargeArray(items) {
  items.forEach(item => heavyProcessing(item));
}

// Good: Yielding to main thread
async function processLargeArray(items) {
  for (const item of items) {
    heavyProcessing(item);
    // Yield to main thread every 50ms
    if (performance.now() - lastYield > 50) {
      await scheduler.yield();
      lastYield = performance.now();
    }
  }
}
```

---

### 3. Cumulative Layout Shift (CLS)

**What it measures:** Visual stability - unexpected layout shifts during page lifetime.

**Thresholds:**
- ✅ Good: ≤ 0.1
- ⚠️ Needs Improvement: 0.1 - 0.25
- ❌ Poor: > 0.25

**How it's calculated:**
```
CLS = Impact Fraction × Distance Fraction
```
- Impact Fraction: % of viewport affected by shift
- Distance Fraction: Distance element moved / viewport height

**How to Measure:**
```javascript
import {onCLS} from 'web-vitals';

onCLS((metric) => {
  console.log('CLS:', metric.value);
  // Get details about each shift
  metric.entries.forEach((entry) => {
    console.log('Shift amount:', entry.value);
    console.log('Elements:', entry.sources);
  });
});
```

**Common Causes & Fixes:**

1. **Images without dimensions**
```html
<!-- Bad -->
<img src="hero.jpg" alt="Hero">

<!-- Good -->
<img src="hero.jpg" alt="Hero" width="1200" height="600">
<!-- or use aspect-ratio CSS -->
<img src="hero.jpg" alt="Hero" style="aspect-ratio: 16/9;">
```

2. **Ads, embeds, iframes without reserved space**
```css
.ad-container {
  min-height: 250px; /* Reserve space */
}
```

3. **Dynamically injected content**
```javascript
// Bad: Inserting above existing content
element.insertAdjacentHTML('afterbegin', newContent);

// Good: Insert below, or reserve space
element.insertAdjacentHTML('beforeend', newContent);
```

4. **Web fonts causing FOIT/FOUT**
```css
@font-face {
  font-family: 'CustomFont';
  src: url('font.woff2') format('woff2');
  font-display: swap; /* or optional */
}
```

**Advanced: PerformanceObserver**
```javascript
const observer = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    if (!entry.hadRecentInput) { // Ignore user-initiated shifts
      console.log('Layout shift:', entry);
      console.log('Shifted elements:', entry.sources);
    }
  }
});

observer.observe({type: 'layout-shift', buffered: true});
```

---

## Additional Performance Metrics

### Time to First Byte (TTFB)

**What it measures:** Server response time - from navigation start to first byte received.

**Thresholds:**
- ✅ Good: ≤ 800ms
- ⚠️ Needs Improvement: 800ms - 1800ms
- ❌ Poor: > 1800ms

**Components:**
1. Redirect time
2. Service worker startup (if applicable)
3. DNS lookup
4. TCP connection + TLS negotiation
5. Request → Response (server processing)

**How to Measure:**
```javascript
import {onTTFB} from 'web-vitals';

onTTFB((metric) => {
  console.log('TTFB:', metric.value);
});

// Or using Navigation Timing API
const ttfb = performance.timing.responseStart - performance.timing.navigationStart;
```

**Optimization Strategies:**
- Use CDN for static assets
- Enable HTTP/2 or HTTP/3
- Optimize database queries
- Implement server-side caching (Redis, Memcached)
- Use edge computing (Cloudflare Workers, Vercel Edge Functions)
- Optimize hosting infrastructure

---

### First Contentful Paint (FCP)

**What it measures:** When first DOM content renders (text, image, canvas, SVG).

**Thresholds:**
- ✅ Good: ≤ 1.8 seconds
- ⚠️ Needs Improvement: 1.8 - 3.0 seconds
- ❌ Poor: > 3.0 seconds

**How to Measure:**
```javascript
import {onFCP} from 'web-vitals';

onFCP((metric) => {
  console.log('FCP:', metric.value);
});

// Using PerformanceObserver
const observer = new PerformanceObserver((list) => {
  const entries = list.getEntriesByName('first-contentful-paint');
  console.log('FCP:', entries[0].startTime);
});

observer.observe({type: 'paint', buffered: true});
```

**Improvement Techniques:**
- Eliminate render-blocking resources
- Minify CSS
- Remove unused CSS
- Preconnect to required origins
- Reduce server response times

---

### Time to Interactive (TTI)

**What it measures:** When page becomes fully interactive and can respond to user input reliably.

**Requirements:**
1. Page has displayed useful content (FCP)
2. Event handlers registered for most visible elements
3. Page responds to user interactions within 50ms

**Thresholds:**
- ✅ Good: ≤ 3.8 seconds
- ⚠️ Needs Improvement: 3.8 - 7.3 seconds
- ❌ Poor: > 7.3 seconds

**How to Measure:**
```javascript
// Using Lighthouse/web-vitals (lab metric only)
// Not available via web-vitals npm package - measure via Lighthouse
```

**Optimization:**
- Minimize main thread work
- Reduce JavaScript execution time
- Code split large bundles
- Defer non-critical JavaScript

---

### Total Blocking Time (TBT)

**What it measures:** Total time main thread was blocked from responding to input, between FCP and TTI.

**Thresholds:**
- ✅ Good: ≤ 200ms
- ⚠️ Needs Improvement: 200 - 600ms
- ❌ Poor: > 600ms

**Calculation:**
Sum of blocking portions of all long tasks (>50ms) between FCP and TTI.

**Example:**
```
Task 1: 70ms → Blocking time: 20ms (70-50)
Task 2: 100ms → Blocking time: 50ms (100-50)
Task 3: 40ms → Blocking time: 0ms (under 50ms)
TBT = 20 + 50 + 0 = 70ms
```

**How to Measure:**
```javascript
// Lab metric - measure via Lighthouse
// Can detect long tasks with PerformanceObserver:

const observer = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    if (entry.duration > 50) {
      console.log('Long task detected:', entry.duration);
    }
  }
});

observer.observe({type: 'longtask', buffered: true});
```

---

### Resource Metrics

#### Request Count
**What it measures:** Total HTTP requests made by page.

**Best Practices:**
- Aim for < 50 requests on initial load
- Combine files where possible
- Use HTTP/2 multiplexing
- Implement lazy loading

**How to Measure:**
```javascript
// Count all resources loaded
const resources = performance.getEntriesByType('resource');
console.log('Total requests:', resources.length);

// Group by type
const byType = resources.reduce((acc, resource) => {
  const type = resource.initiatorType;
  acc[type] = (acc[type] || 0) + 1;
  return acc;
}, {});
console.log(byType); // {script: 15, img: 23, css: 5, ...}
```

#### Bundle Size
**What it measures:** Total JavaScript/CSS size transferred and parsed.

**Thresholds:**
- JavaScript: < 200KB gzipped for initial bundle
- Critical CSS: < 14KB (fits in first TCP round trip)

**How to Measure:**
```javascript
// Total transfer size
const totalSize = performance.getEntriesByType('resource')
  .reduce((sum, resource) => sum + (resource.transferSize || 0), 0);

console.log('Total transferred:', (totalSize / 1024 / 1024).toFixed(2), 'MB');

// JavaScript only
const jsSize = performance.getEntriesByType('resource')
  .filter(r => r.initiatorType === 'script')
  .reduce((sum, r) => sum + (r.transferSize || 0), 0);
```

**Tools:**
- webpack-bundle-analyzer
- source-map-explorer
- bundlephobia.com (check npm package sizes)

#### JavaScript Execution Time
**What it measures:** Time spent parsing, compiling, and executing JavaScript.

**Target:** < 2 seconds on mobile devices

**How to Measure:**
```javascript
// Using Performance timeline
const scriptEntries = performance.getEntriesByType('resource')
  .filter(entry => entry.initiatorType === 'script');

scriptEntries.forEach(entry => {
  console.log(entry.name, 'took', entry.duration, 'ms');
});

// Measure function execution
console.time('myFunction');
myFunction();
console.timeEnd('myFunction');

// User Timing API
performance.mark('start-processing');
// ... do work ...
performance.mark('end-processing');
performance.measure('processing-time', 'start-processing', 'end-processing');

const measure = performance.getEntriesByName('processing-time')[0];
console.log('Processing took:', measure.duration, 'ms');
```

---

## Measurement Tools & Libraries

### 1. Lighthouse

**Type:** Lab tool (simulated, controlled environment)

**Installation:**
```bash
# Chrome DevTools (built-in)
# Or CLI
npm install -g lighthouse

# Run
lighthouse https://example.com --view
lighthouse https://example.com --output=json --output-path=./report.json
```

**CI/CD Integration:**
```javascript
// lighthouse-ci.js
const lighthouse = require('lighthouse');
const chromeLauncher = require('chrome-launcher');

async function runLighthouse(url) {
  const chrome = await chromeLauncher.launch({chromeFlags: ['--headless']});
  const options = {
    logLevel: 'info',
    output: 'json',
    port: chrome.port
  };
  
  const runnerResult = await lighthouse(url, options);
  const score = runnerResult.lhr.categories.performance.score * 100;
  
  await chrome.kill();
  
  if (score < 90) {
    throw new Error(`Performance score ${score} is below threshold`);
  }
}
```

---

### 2. web-vitals Library

**Type:** RUM (Real User Monitoring)

**Installation:**
```bash
npm install web-vitals
```

**Basic Implementation:**
```javascript
import {onCLS, onINP, onLCP, onFCP, onTTFB} from 'web-vitals';

function sendToAnalytics(metric) {
  // Google Analytics 4
  gtag('event', metric.name, {
    value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
    metric_id: metric.id,
    metric_value: metric.value,
    metric_delta: metric.delta,
  });
}

onCLS(sendToAnalytics);
onINP(sendToAnalytics);
onLCP(sendToAnalytics);
onFCP(sendToAnalytics);
onTTFB(sendToAnalytics);
```

**Advanced Implementation with Attribution:**
```javascript
import {onLCP, onCLS, onINP} from 'web-vitals/attribution';

onLCP((metric) => {
  console.log('LCP Element:', metric.attribution.element);
  console.log('LCP Resource URL:', metric.attribution.url);
  console.log('Time to First Byte:', metric.attribution.timeToFirstByte);
  console.log('Resource Load Delay:', metric.attribution.resourceLoadDelay);
  console.log('Element Render Delay:', metric.attribution.elementRenderDelay);
  
  // Send detailed data
  sendToAnalytics({
    metric: 'LCP',
    value: metric.value,
    element: metric.attribution.element,
    url: metric.attribution.url
  });
});
```

---

### 3. PerformanceObserver API

**Type:** Native browser API for performance monitoring

**Basic Usage:**
```javascript
// Observe all performance entries
const observer = new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    console.log(entry.name, entry.entryType, entry.startTime, entry.duration);
  }
});

// Observe specific types
observer.observe({
  entryTypes: [
    'navigation',     // Page navigation
    'resource',       // Resource loading
    'paint',          // FCP, FP
    'largest-contentful-paint',
    'layout-shift',
    'longtask',       // Tasks > 50ms
    'measure',        // User timing marks
  ]
});
```

**Advanced: Resource Timing**
```javascript
const observer = new PerformanceObserver((list) => {
  list.getEntries().forEach((entry) => {
    // Detailed timing breakdown
    const timing = {
      name: entry.name,
      dns: entry.domainLookupEnd - entry.domainLookupStart,
      tcp: entry.connectEnd - entry.connectStart,
      tls: entry.connectEnd - entry.secureConnectionStart,
      ttfb: entry.responseStart - entry.requestStart,
      download: entry.responseEnd - entry.responseStart,
      total: entry.responseEnd - entry.startTime,
      size: entry.transferSize,
      cached: entry.transferSize === 0
    };
    
    console.log(timing);
  });
});

observer.observe({type: 'resource', buffered: true});
```

---

### 4. Chrome DevTools Performance Panel

**How to use:**
1. Open DevTools → Performance tab
2. Click Record (or Ctrl+E)
3. Perform actions on page
4. Stop recording
5. Analyze:
   - Main thread activity
   - Long tasks (red corners)
   - Layout shifts (blue bars)
   - Network waterfall
   - FPS graph

**CPU Throttling:**
Settings → Throttling → 4x slowdown (simulates mobile)

**Screenshots:**
Enable "Screenshots" to see visual progression

---

### 5. WebPageTest

**URL:** https://webpagetest.org

**Features:**
- Test from multiple global locations
- Various device/connection profiles
- Filmstrip view
- Detailed waterfall charts
- Comparison tests

**Advanced Configuration:**
```javascript
// Custom script for WebPageTest
logData 0
navigate https://example.com
logData 1
execAndWait document.querySelector('.load-more').click()
```

**API Usage:**
```bash
curl "https://www.webpagetest.org/runtest.php?url=https://example.com&k=YOUR_API_KEY&f=json"
```

---

### 6. Bundle Analyzers

#### Webpack Bundle Analyzer
```bash
npm install --save-dev webpack-bundle-analyzer
```

```javascript
// webpack.config.js
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = {
  plugins: [
    new BundleAnalyzerPlugin({
      analyzerMode: 'static',
      openAnalyzer: false,
      reportFilename: 'bundle-report.html'
    })
  ]
};
```

#### Source Map Explorer
```bash
npm install -g source-map-explorer

# Analyze bundle
source-map-explorer bundle.js bundle.js.map
```

#### Rollup Plugin Visualizer
```javascript
// rollup.config.js
import {visualizer} from 'rollup-plugin-visualizer';

export default {
  plugins: [
    visualizer({
      filename: './stats.html',
      open: true
    })
  ]
};
```

---

### 7. RUM Services

#### Google Analytics 4 + Web Vitals
```javascript
// Automatic reporting to GA4
import {onCLS, onFCP, onLCP, onTTFB, onINP} from 'web-vitals';

function sendToGoogleAnalytics({name, delta, value, id, rating}) {
  gtag('event', name, {
    event_category: 'Web Vitals',
    event_label: id,
    value: Math.round(name === 'CLS' ? delta * 1000 : delta),
    metric_rating: rating,
    non_interaction: true
  });
}

onCLS(sendToGoogleAnalytics);
onFCP(sendToGoogleAnalytics);
onLCP(sendToGoogleAnalytics);
onTTFB(sendToGoogleAnalytics);
onINP(sendToGoogleAnalytics);
```

#### Custom RUM Implementation
```javascript
class PerformanceMonitor {
  constructor(endpoint) {
    this.endpoint = endpoint;
    this.batch = [];
    this.init();
  }

  init() {
    // Monitor Core Web Vitals
    onCLS((metric) => this.send(metric));
    onLCP((metric) => this.send(metric));
    onINP((metric) => this.send(metric));
    
    // Monitor custom metrics
    this.observeLongTasks();
    this.observeResourceTiming();
    
    // Flush on page unload
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        this.flush();
      }
    });
  }

  send(metric) {
    this.batch.push({
      ...metric,
      url: window.location.href,
      userAgent: navigator.userAgent,
      connection: navigator.connection?.effectiveType,
      timestamp: Date.now()
    });

    if (this.batch.length >= 10) {
      this.flush();
    }
  }

  flush() {
    if (this.batch.length === 0) return;

    navigator.sendBeacon(
      this.endpoint,
      JSON.stringify(this.batch)
    );
    
    this.batch = [];
  }

  observeLongTasks() {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        this.send({
          name: 'longtask',
          value: entry.duration,
          attribution: entry.attribution
        });
      }
    });
    observer.observe({type: 'longtask', buffered: true});
  }

  observeResourceTiming() {
    const observer = new PerformanceObserver((list) => {
      for (const entry of list.getEntries()) {
        // Only track slow resources
        if (entry.duration > 1000) {
          this.send({
            name: 'slow-resource',
            url: entry.name,
            duration: entry.duration,
            size: entry.transferSize
          });
        }
      }
    });
    observer.observe({type: 'resource', buffered: true});
  }
}

// Initialize
const monitor = new PerformanceMonitor('https://api.example.com/metrics');
```

---

## Enterprise Case Studies

### 1. Netflix - Prefetching Strategy

**Challenge:** Reduce Time to Interactive on slow networks

**Solution:**
```javascript
// Predict user navigation and prefetch resources
class PrefetchManager {
  constructor() {
    this.observer = new IntersectionObserver(
      (entries) => this.handleIntersection(entries),
      { rootMargin: '50px' }
    );
    this.prefetched = new Set();
  }

  observe(links) {
    links.forEach(link => this.observer.observe(link));
  }

  handleIntersection(entries) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        this.prefetch(entry.target.href);
      }
    });
  }

  prefetch(url) {
    if (this.prefetched.has(url)) return;
    
    const link = document.createElement('link');
    link.rel = 'prefetch';
    link.href = url;
    link.as = 'document';
    document.head.appendChild(link);
    
    this.prefetched.add(url);
  }
}

// Usage: Prefetch show pages when hovering over thumbnails
const prefetcher = new PrefetchManager();
prefetcher.observe(document.querySelectorAll('.show-card a'));
```

**Results:**
- 30% faster page transitions
- Reduced perceived load time
- Better user engagement

**Key Takeaways:**
- Use Intersection Observer for smart prefetching
- Consider user's network speed (Network Information API)
- Implement priority queuing for prefetch requests

---

### 2. Amazon - Image Optimization

**Challenge:** Reduce LCP from hero images

**Implementation:**
```html
<!-- Responsive images with modern formats -->
<picture>
  <source 
    type="image/avif"
    srcset="
      product-400.avif 400w,
      product-800.avif 800w,
      product-1200.avif 1200w
    "
    sizes="(max-width: 640px) 100vw, 50vw"
  >
  <source 
    type="image/webp"
    srcset="
      product-400.webp 400w,
      product-800.webp 800w,
      product-1200.webp 1200w
    "
    sizes="(max-width: 640px) 100vw, 50vw"
  >
  <img 
    src="product-800.jpg"
    alt="Product"
    width="800"
    height="600"
    loading="eager"
    fetchpriority="high"
    decoding="async"
  >
</picture>
```

**Progressive Image Loading:**
```javascript
// Load low-quality placeholder first, then high-quality
class ProgressiveImage {
  constructor(container, lowSrc, highSrc) {
    this.container = container;
    this.lowSrc = lowSrc;
    this.highSrc = highSrc;
    this.load();
  }

  load() {
    // Load low-quality immediately
    const lowImg = new Image();
    lowImg.src = this.lowSrc;
    lowImg.onload = () => {
      this.container.style.backgroundImage = `url(${this.lowSrc})`;
      this.container.classList.add('loaded');
    };

    // Load high-quality in background
    const highImg = new Image();
    highImg.src = this.highSrc;
    highImg.onload = () => {
      this.container.style.backgroundImage = `url(${this.highSrc})`;
      this.container.classList.add('high-quality');
    };
  }
}
```

**Results:**
- 40% improvement in LCP
- AVIF saves 50% bandwidth vs JPEG
- Better perceived performance with progressive loading

---

### 3. Shopify - Third-Party Script Management

**Challenge:** Third-party scripts blocking main thread (analytics, chat, ads)

**Solution: Facade Pattern**
```javascript
// Lazy-load third-party chat widget
class ChatFacade {
  constructor() {
    this.loaded = false;
    this.init();
  }

  init() {
    // Show fake chat button
    const button = document.getElementById('chat-button');
    button.addEventListener('click', () => this.loadRealWidget(), {once: true});
    
    // Or load on idle
    if ('requestIdleCallback' in window) {
      requestIdleCallback(() => this.loadRealWidget(), {timeout: 2000});
    }
  }

  loadRealWidget() {
    if (this.loaded) return;
    this.loaded = true;

    // Dynamically load script
    const script = document.createElement('script');
    script.src = 'https://chat-widget.example.com/widget.js';
    script.async = true;
    document.body.appendChild(script);
  }
}

new ChatFacade();
```

**Script Loading Strategy:**
```javascript
// Priority-based script loading
class ScriptLoader {
  constructor() {
    this.queue = {
      critical: [],   // Load immediately
      high: [],       // Load on DOMContentLoaded
      low: []        // Load on window.load or idle
    };
  }

  add(src, priority = 'low', attributes = {}) {
    this.queue[priority].push({src, attributes});
  }

  async load() {
    // Critical scripts - block rendering
    await this.loadScripts(this.queue.critical, false);
    
    // High priority - after DOMContentLoaded
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', () => {
        this.loadScripts(this.queue.high, true);
      });
    } else {
      this.loadScripts(this.queue.high, true);
    }
    
    // Low priority - after page load
    window.addEventListener('load', () => {
      requestIdleCallback(() => {
        this.loadScripts(this.queue.low, true);
      });
    });
  }

  loadScripts(scripts, async = true) {
    return Promise.all(scripts.map(({src, attributes}) => {
      return new Promise((resolve, reject) => {
        const script = document.createElement('script');
        script.src = src;
        script.async = async;
        
        Object.entries(attributes).forEach(([key, value]) => {
          script.setAttribute(key, value);
        });
        
        script.onload = resolve;
        script.onerror = reject;
        document.head.appendChild(script);
      });
    }));
  }
}

// Usage
const loader = new ScriptLoader();
loader.add('/analytics.js', 'high');
loader.add('/chat-widget.js', 'low');
loader.add('/ab-testing.js', 'high');
loader.load();
```

**Results:**
- 65% reduction in Total Blocking Time
- INP improved from 400ms to 150ms
- Third-party scripts no longer block main thread

---

### 4. Pinterest - Lazy Loading & Virtual Scrolling

**Challenge:** Render thousands of images efficiently

**Solution: Virtual Scrolling**
```javascript
class VirtualScroller {
  constructor(container, items, itemHeight, renderItem) {
    this.container = container;
    this.items = items;
    this.itemHeight = itemHeight;
    this.renderItem = renderItem;
    
    this.visibleItems = [];
    this.startIndex = 0;
    this.endIndex = 0;
    
    this.init();
  }

  init() {
    // Create scroll container with proper height
    const totalHeight = this.items.length * this.itemHeight;
    this.container.style.height = `${totalHeight}px`;
    this.container.style.position = 'relative';
    
    // Calculate visible range
    this.calculateVisible();
    this.render();
    
    // Update on scroll (throttled)
    let ticking = false;
    window.addEventListener('scroll', () => {
      if (!ticking) {
        requestAnimationFrame(() => {
          this.calculateVisible();
          this.render();
          ticking = false;
        });
        ticking = true;
      }
    });
  }

  calculateVisible() {
    const scrollTop = window.pageYOffset;
    const viewportHeight = window.innerHeight;
    
    // Add buffer above and below viewport
    const buffer = 5;
    this.startIndex = Math.max(0, Math.floor(scrollTop / this.itemHeight) - buffer);
    this.endIndex = Math.min(
      this.items.length,
      Math.ceil((scrollTop + viewportHeight) / this.itemHeight) + buffer
    );
  }

  render() {
    // Clear container
    this.container.innerHTML = '';
    
    // Render only visible items
    for (let i = this.startIndex; i < this.endIndex; i++) {
      const item = this.renderItem(this.items[i]);
      item.style.position = 'absolute';
      item.style.top = `${i * this.itemHeight}px`;
      this.container.appendChild(item);
    }
  }
}

// Usage for Pinterest-style grid
const images = [...]; // Array of image data
const scroller = new VirtualScroller(
  document.getElementById('feed'),
  images,
  250, // Item height
  (imageData) => {
    const div = document.createElement('div');
    div.className = 'pin';
    div.innerHTML = `
      <img 
        data-src="${imageData.url}" 
        alt="${imageData.title}"
        loading="lazy"
        class="lazy-image"
      >
    `;
    return div;
  }
);
```

**Intersection Observer for Lazy Loading:**
```javascript
class LazyLoader {
  constructor(selector, options = {}) {
    this.options = {
      root: null,
      rootMargin: '50px',
      threshold: 0.01,
      ...options
    };
    
    this.observer = new IntersectionObserver(
      (entries) => this.handleIntersection(entries),
      this.options
    );
    
    this.observe(selector);
  }

  observe(selector) {
    document.querySelectorAll(selector).forEach(img => {
      this.observer.observe(img);
    });
  }

  handleIntersection(entries) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        this.loadImage(entry.target);
        this.observer.unobserve(entry.target);
      }
    });
  }

  loadImage(img) {
    const src = img.getAttribute('data-src');
    if (!src) return;
    
    // Use decode() for better performance
    img.src = src;
    img.decode()
      .then(() => {
        img.classList.add('loaded');
      })
      .catch(() => {
        console.error('Image failed to load:', src);
      });
  }
}

// Initialize
const lazyLoader = new LazyLoader('.lazy-image');
```

**Results:**
- Render 10,000+ pins without performance degradation
- 90% reduction in initial DOM nodes
- Smooth 60fps scrolling
- Memory usage stays constant

---

### 5. Twitter/X - Code Splitting & Route-Based Loading

**Challenge:** Massive JavaScript bundle slowing initial load

**Solution: Route-based code splitting**
```javascript
// Using React lazy() and Suspense
import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// Lazy load route components
const Home = lazy(() => import('./routes/Home'));
const Profile = lazy(() => import('./routes/Profile'));
const Tweet = lazy(() => import('./routes/Tweet'));
const Messages = lazy(() => import('./routes/Messages'));
const Notifications = lazy(() => import('./routes/Notifications'));

// Loading component
function LoadingFallback() {
  return <div className="skeleton-loader" />;
}

function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<LoadingFallback />}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/profile/:username" element={<Profile />} />
          <Route path="/tweet/:id" element={<Tweet />} />
          <Route path="/messages" element={<Messages />} />
          <Route path="/notifications" element={<Notifications />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
```

**Preloading Critical Routes:**
```javascript
// Preload likely next routes on hover/focus
function RoutePreloader() {
  useEffect(() => {
    const links = document.querySelectorAll('[data-preload]');
    
    links.forEach(link => {
      link.addEventListener('mouseenter', () => {
        const route = link.getAttribute('data-preload');
        preloadRoute(route);
      }, { once: true });
    });
  }, []);

  return null;
}

function preloadRoute(route) {
  const routes = {
    home: () => import('./routes/Home'),
    profile: () => import('./routes/Profile'),
    messages: () => import('./routes/Messages')
  };
  
  if (routes[route]) {
    routes[route]();
  }
}

// Usage in navigation
<Link to="/messages" data-preload="messages">
  Messages
</Link>
```

**Webpack Configuration:**
```javascript
// webpack.config.js
module.exports = {
  optimization: {
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        // Vendor code
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor',
          priority: 10
        },
        // Common code shared across routes
        common: {
          minChunks: 2,
          priority: 5,
          reuseExistingChunk: true
        },
        // Heavy libraries (split separately)
        charts: {
          test: /[\\/]node_modules[\\/](chart\.js|d3|recharts)[\\/]/,
          name: 'charts',
          priority: 20
        }
      }
    },
    // Create runtime chunk
    runtimeChunk: 'single'
  }
};
```

**Results:**
- Initial bundle: 800KB → 150KB (81% reduction)
- TTI improved by 3.2 seconds
- Subsequent route loads: < 200ms

---

### 6. Airbnb - Server-Side Rendering & Hydration

**Challenge:** Balance SEO with interactive user experience

**Solution: Progressive Hydration**
```javascript
// Progressive hydration - hydrate components as needed
import { hydrate } from 'react-dom';
import { lazy, Suspense } from 'react';

class ProgressiveHydrator {
  constructor() {
    this.components = new Map();
    this.observer = new IntersectionObserver(
      (entries) => this.handleIntersection(entries),
      { rootMargin: '50px' }
    );
  }

  register(selector, loader) {
    this.components.set(selector, loader);
    document.querySelectorAll(selector).forEach(el => {
      this.observer.observe(el);
    });
  }

  handleIntersection(entries) {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        this.hydrate(entry.target);
        this.observer.unobserve(entry.target);
      }
    });
  }

  async hydrate(element) {
    const selector = element.className;
    const loader = this.components.get(`.${selector}`);
    
    if (!loader) return;
    
    const Component = await loader();
    hydrate(<Component.default />, element);
  }
}

// Usage
const hydrator = new ProgressiveHydrator();

// Register components to hydrate on visibility
hydrator.register('.search-widget', () => import('./SearchWidget'));
hydrator.register('.map-component', () => import('./MapComponent'));
hydrator.register('.calendar', () => import('./Calendar'));
```

**Streaming SSR (React 18):**
```javascript
// server.js
import { renderToPipeableStream } from 'react-dom/server';

app.get('*', (req, res) => {
  const { pipe } = renderToPipeableStream(
    <App />,
    {
      bootstrapScripts: ['/client.js'],
      onShellReady() {
        res.setHeader('Content-Type', 'text/html');
        pipe(res);
      },
      onError(error) {
        console.error(error);
      }
    }
  );
});
```

**Results:**
- TTFB: 1.2s → 400ms
- FCP: 2.8s → 1.1s
- SEO maintained while improving interactivity
- Selective hydration reduces JS execution time by 60%

---

### 7. Google - Resource Hints & Predictive Prefetching

**Implementation:**
```html
<!-- DNS prefetch for external domains -->
<link rel="dns-prefetch" href="https://fonts.googleapis.com">
<link rel="dns-prefetch" href="https://analytics.google.com">

<!-- Preconnect for critical third parties -->
<link rel="preconnect" href="https://api.example.com" crossorigin>

<!-- Preload critical assets -->
<link rel="preload" href="/critical.css" as="style">
<link rel="preload" href="/hero.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="/hero.jpg" as="image" fetchpriority="high">

<!-- Prefetch likely next navigation -->
<link rel="prefetch" href="/dashboard" as="document">
```

**Machine Learning-Based Prefetching:**
```javascript
class PredictivePrefetcher {
  constructor() {
    this.navigationHistory = [];
    this.patterns = new Map();
    this.load();
  }

  track(currentPage) {
    // Track user navigation patterns
    if (this.navigationHistory.length > 0) {
      const previousPage = this.navigationHistory[this.navigationHistory.length - 1];
      const key = previousPage;
      
      if (!this.patterns.has(key)) {
        this.patterns.set(key, new Map());
      }
      
      const nextPages = this.patterns.get(key);
      nextPages.set(currentPage, (nextPages.get(currentPage) || 0) + 1);
    }
    
    this.navigationHistory.push(currentPage);
    this.save();
    this.prefetchLikely();
  }

  prefetchLikely() {
    const currentPage = window.location.pathname;
    const nextPages = this.patterns.get(currentPage);
    
    if (!nextPages) return;
    
    // Sort by frequency and prefetch top 3
    const sorted = [...nextPages.entries()]
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3);
    
    sorted.forEach(([page]) => {
      this.prefetch(page);
    });
  }

  prefetch(url) {
    const link = document.createElement('link');
    link.rel = 'prefetch';
    link.href = url;
    document.head.appendChild(link);
  }

  save() {
    localStorage.setItem('nav-patterns', JSON.stringify([...this.patterns]));
  }

  load() {
    const saved = localStorage.getItem('nav-patterns');
    if (saved) {
      this.patterns = new Map(JSON.parse(saved));
    }
  }
}

// Initialize
const prefetcher = new PredictivePrefetcher();
prefetcher.track(window.location.pathname);

// Track on navigation
window.addEventListener('popstate', () => {
  prefetcher.track(window.location.pathname);
});
```

**Results:**
- 20% faster perceived navigation
- Prefetch accuracy: 75%
- Minimal bandwidth overhead (< 100KB per session)

---

### 8. Medium - Font Loading Optimization

**Challenge:** Prevent layout shift and FOIT (Flash of Invisible Text)

**Solution: Font Display Strategy**
```css
@font-face {
  font-family: 'Charter';
  src: url('/fonts/charter.woff2') format('woff2');
  font-weight: 400;
  font-style: normal;
  font-display: swap; /* or optional */
}

/* System font stack fallback */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 
               Roboto, Oxygen, Ubuntu, Cantarell, 'Helvetica Neue', 
               Arial, sans-serif;
}

/* Apply custom font after load */
.fonts-loaded body {
  font-family: 'Charter', -apple-system, BlinkMacSystemFont, serif;
}
```

**Font Loading API:**
```javascript
class FontLoader {
  async load() {
    if (!('fonts' in document)) {
      return;
    }

    // Preload critical fonts
    const fonts = [
      new FontFace('Charter', 'url(/fonts/charter.woff2)', {
        weight: '400',
        style: 'normal'
      }),
      new FontFace('Charter', 'url(/fonts/charter-bold.woff2)', {
        weight: '700',
        style: 'normal'
      })
    ];

    try {
      // Load all fonts
      const loaded = await Promise.all(
        fonts.map(font => font.load())
      );
      
      // Add to document
      loaded.forEach(font => document.fonts.add(font));
      
      // Add class to trigger CSS
      document.documentElement.classList.add('fonts-loaded');
      
      // Store in cache
      sessionStorage.setItem('fonts-loaded', 'true');
    } catch (error) {
      console.error('Font loading failed:', error);
    }
  }
}

// Load fonts (skip if already loaded this session)
if (sessionStorage.getItem('fonts-loaded')) {
  document.documentElement.classList.add('fonts-loaded');
} else {
  const loader = new FontLoader();
  loader.load();
}
```

**Subsetting Fonts:**
```bash
# Use glyphhanger to subset fonts to only used characters
npm install -g glyphhanger

# Generate subset
glyphhanger https://example.com --subset=font.ttf --formats=woff2

# Result: font-subset.woff2 (only includes used glyphs)
```

**Results:**
- CLS reduced from 0.15 to 0.02
- Font file size: 120KB → 35KB (subsetting)
- No FOIT or FOUT with proper fallback matching

---

## Implementation Guide

### Quick Start: Adding Performance Monitoring

**Step 1: Install web-vitals**
```bash
npm install web-vitals
```

**Step 2: Create monitoring module**
```javascript
// performance-monitor.js
import {onCLS, onINP, onLCP, onFCP, onTTFB} from 'web-vitals';

function sendToAnalytics(metric) {
  const body = JSON.stringify({
    name: metric.name,
    value: metric.value,
    rating: metric.rating,
    delta: metric.delta,
    id: metric.id,
    navigationType: metric.navigationType,
    url: window.location.href,
    userAgent: navigator.userAgent,
    connection: navigator.connection?.effectiveType
  });

  // Use sendBeacon for reliability
  if (navigator.sendBeacon) {
    navigator.sendBeacon('/api/metrics', body);
  } else {
    fetch('/api/metrics', {
      method: 'POST',
      body,
      keepalive: true
    }).catch(console.error);
  }
}

// Initialize monitoring
export function initPerformanceMonitoring() {
  onCLS(sendToAnalytics);
  onINP(sendToAnalytics);
  onLCP(sendToAnalytics);
  onFCP(sendToAnalytics);
  onTTFB(sendToAnalytics);
}
```

**Step 3: Add to your app**
```javascript
// main.js
import { initPerformanceMonitoring } from './performance-monitor';

// Initialize after app loads
if (process.env.NODE_ENV === 'production') {
  initPerformanceMonitoring();
}
```

**Step 4: Create backend endpoint**
```javascript
// api/metrics.js (Node.js/Express)
app.post('/api/metrics', (req, res) => {
  const metric = req.body;
  
  // Log to your analytics service
  console.log('Performance metric:', metric);
  
  // Store in database or forward to analytics service
  // analyticsService.track(metric);
  
  res.status(200).send('OK');
});
```

---

## Optimization Strategies

### 1. Critical Rendering Path Optimization

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  
  <!-- Critical CSS inlined -->
  <style>
    /* Only above-the-fold styles */
    body { margin: 0; font-family: system-ui; }
    .header { background: #333; color: white; padding: 1rem; }
  </style>
  
  <!-- Preconnect to critical origins -->
  <link rel="preconnect" href="https://api.example.com">
  
  <!-- Preload critical resources -->
  <link rel="preload" href="/app.js" as="script">
  <link rel="preload" href="/hero.jpg" as="image" fetchpriority="high">
  
  <!-- Async load non-critical CSS -->
  <link rel="preload" href="/styles.css" as="style" 
        onload="this.onload=null;this.rel='stylesheet'">
  <noscript><link rel="stylesheet" href="/styles.css"></noscript>
</head>
<body>
  <!-- Content here -->
  
  <!-- Defer non-critical JavaScript -->
  <script defer src="/app.js"></script>
</body>
</html>
```

### 2. Image Optimization Checklist

```javascript
// Automatic image optimization in Next.js
import Image from 'next/image';

function ProductImage({ src, alt }) {
  return (
    <Image
      src={src}
      alt={alt}
      width={800}
      height={600}
      sizes="(max-width: 768px) 100vw, 50vw"
      priority={false} // Only true for LCP image
      placeholder="blur"
      blurDataURL="/placeholder.jpg"
    />
  );
}
```

**Manual Optimization:**
- Use WebP/AVIF formats (50-70% smaller)
- Implement responsive images (`srcset`)
- Set explicit dimensions
- Use `loading="lazy"` for off-screen images
- Use `fetchpriority="high"` for LCP image
- Compress with tools like ImageOptim, Squoosh

### 3. JavaScript Optimization

```javascript
// Code splitting by route
const Dashboard = lazy(() => import('./Dashboard'));
const Settings = lazy(() => import('./Settings'));

// Dynamic imports for heavy features
button.addEventListener('click', async () => {
  const { Chart } = await import('./chart-library');
  new Chart(data);
});

// Tree shaking - import only what you need
import { debounce } from 'lodash-es'; // ✓ Good
import _ from 'lodash'; // ✗ Bad (imports everything)

// Defer non-critical code
if ('requestIdleCallback' in window) {
  requestIdleCallback(() => {
    // Non-critical initialization
    initAnalytics();
  });
}
```

### 4. Caching Strategy

```javascript
// Service Worker caching
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('v1').then((cache) => {
      return cache.addAll([
        '/',
        '/styles.css',
        '/app.js',
        '/logo.svg'
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      // Cache first, then network
      return response || fetch(event.request);
    })
  );
});
```

**HTTP Cache Headers:**
```javascript
// Express.js
app.use((req, res, next) => {
  // Immutable assets (versioned)
  if (req.url.match(/\.(js|css|jpg|png|woff2)$/)) {
    res.setHeader('Cache-Control', 'public, max-age=31536000, immutable');
  }
  // HTML - always revalidate
  else if (req.url.endsWith('.html')) {
    res.setHeader('Cache-Control', 'no-cache');
  }
  next();
});
```

---

## Monitoring & Alerting

### Setting Up Performance Budgets

```javascript
// lighthouse-budget.json
{
  "path": "/*",
  "timings": [
    {
      "metric": "interactive",
      "budget": 3800
    },
    {
      "metric": "first-contentful-paint",
      "budget": 1800
    }
  ],
  "resourceSizes": [
    {
      "resourceType": "script",
      "budget": 300
    },
    {
      "resourceType": "image",
      "budget": 500
    },
    {
      "resourceType": "total",
      "budget": 1000
    }
  ],
  "resourceCounts": [
    {
      "resourceType": "third-party",
      "budget": 10
    }
  ]
}
```

**CI/CD Integration:**
```yaml
# .github/workflows/performance.yml
name: Performance Check

on: [pull_request]

jobs:
  lighthouse:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run Lighthouse
        uses: treosh/lighthouse-ci-action@v9
        with:
          urls: |
            https://staging.example.com
          budgetPath: ./lighthouse-budget.json
          uploadArtifacts: true
```

### Real-Time Alerting

```javascript
// Alert on performance degradation
class PerformanceAlerter {
  constructor(thresholds) {
    this.thresholds = thresholds;
    this.initMonitoring();
  }

  initMonitoring() {
    onLCP((metric) => this.check('LCP', metric));
    onINP((metric) => this.check('INP', metric));
    onCLS((metric) => this.check('CLS', metric));
  }

  check(name, metric) {
    const threshold = this.thresholds[name];
    
    if (metric.rating === 'poor' || metric.value > threshold) {
      this.alert({
        metric: name,
        value: metric.value,
        threshold,
        url: window.location.href,
        timestamp: Date.now()
      });
    }
  }

  alert(data) {
    // Send to monitoring service
    fetch('/api/alerts', {
      method: 'POST',
      body: JSON.stringify(data)
    });
    
    // Log to console in development
    if (process.env.NODE_ENV === 'development') {
      console.warn('Performance threshold exceeded:', data);
    }
  }
}

// Initialize with thresholds
const alerter = new PerformanceAlerter({
  LCP: 2500,
  INP: 200,
  CLS: 0.1
});
```

---

## Conclusion

Performance optimization is an ongoing process. Key principles:

1. **Measure first** - Use RUM data to identify real issues
2. **Prioritize Core Web Vitals** - They impact SEO and user experience
3. **Optimize iteratively** - Focus on biggest wins first
4. **Monitor continuously** - Set up alerts and budgets
5. **Test on real devices** - Throttle CPU/network to simulate conditions

### Further Resources

- [web.dev/vitals](https://web.dev/vitals) - Official Google documentation
- [WebPageTest](https://webpagetest.org) - Free performance testing
- [Chrome DevTools Performance](https://developer.chrome.com/docs/devtools/performance)
- [Performance Budget Calculator](https://perf-budget-calculator.firebaseapp.com)

### Performance Checklist