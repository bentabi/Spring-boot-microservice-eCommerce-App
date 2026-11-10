// Same-origin path — Vite dev server proxies /product-composite to the backend
// (see vite.config.ts), so no CORS configuration is needed on the services.
const API = '/product-composite'
const POLL_MS = 5000
const app = document.getElementById('app')!

let pollTimer: number | undefined

interface Recommendation {
  recommendationId: number
  author: string
  rate: number
  content: string
}

interface Review {
  reviewId: number
  author: string
  subject: string
  content: string
}

interface Product {
  productId: number
  name: string
  brand: string
  category: string
  price: number
  rating: number
  countInStock: number
  description: string
  image: string
  recommendations?: Recommendation[] | null
  reviews?: Review[] | null
}

function stopPolling() {
  if (pollTimer !== undefined) {
    clearInterval(pollTimer)
    pollTimer = undefined
  }
}

// onerror swaps a broken image URL for a "No image" placeholder (note: class is
// unquoted so it stays valid inside the double-quoted attribute).
const IMG_ONERROR = `this.onerror=null;this.outerHTML='<div class=placeholder>No image</div>'`

function thumb(p: Product): string {
  return p.image
    ? `<img class="thumb" src="${p.image.replace(/"/g, '&quot;')}" alt="" loading="lazy" onerror="${IMG_ONERROR}">`
    : `<div class="placeholder">No image</div>`
}

function searchBar(value = ''): string {
  return `
    <form id="search-form" class="search-bar">
      <input name="q" placeholder="Search products by name..." value="${value.replace(/"/g, '&quot;')}" />
      <button type="submit">Search</button>
    </form>
  `
}

function wireSearchBar() {
  const form = document.getElementById('search-form')
  if (!form) return
  form.addEventListener('submit', (e) => {
    e.preventDefault()
    const q = (new FormData(e.target as HTMLFormElement).get('q') as string).trim()
    window.location.hash = q ? `#/search/${encodeURIComponent(q)}` : '#/'
  })
}

function productCards(products: Product[]): string {
  return products.map(p => `
    <div class="product-card">
      ${thumb(p)}
      <div class="info">
        <h3><a href="#/product/${p.productId}">${p.name}</a></h3>
        <p>Brand: ${p.brand} | Category: ${p.category}</p>
        <p>Price: $${p.price} | Stock: ${p.countInStock}</p>
      </div>
    </div>
  `).join('')
}

// Refreshes only the #product-list container so the search box (and its focus)
// is preserved while polling.
async function refreshList() {
  const container = document.getElementById('product-list')
  if (!container) { stopPolling(); return }
  try {
    const res = await fetch(API)
    const products: Product[] = await res.json()
    container.innerHTML = products.length === 0
      ? '<p>No products found. <a href="#/create">Add one!</a></p>'
      : productCards(products)
  } catch {
    // keep the last good content on a transient error
  }
}

async function renderList() {
  app.innerHTML =
    '<h2>All Products</h2>' +
    searchBar() +
    `<p class="poll-note">Auto-refreshes every ${POLL_MS / 1000}s</p>` +
    '<div id="product-list">Loading...</div>'
  wireSearchBar()
  await refreshList()
  pollTimer = window.setInterval(refreshList, POLL_MS)
}

async function renderSearch(term: string) {
  const decoded = decodeURIComponent(term)
  const head = `<h2>Search results for "${decoded}"</h2>` + searchBar(decoded)
  app.innerHTML = head + '<p>Loading...</p>'
  wireSearchBar()
  const res = await fetch(`${API}/search?name=${encodeURIComponent(decoded)}`)
  const products: Product[] = await res.json()
  const body = products.length === 0
    ? '<p>No products matched. <a href="#/">Back to all</a></p>'
    : productCards(products)
  app.innerHTML = head + body
  wireSearchBar()
}

function stars(rate: number): string {
  const n = Math.max(0, Math.min(5, Math.round(rate)))
  return `<span class="stars">${'★'.repeat(n)}${'☆'.repeat(5 - n)}</span>`
}

function reviewsSection(reviews?: Review[] | null): string {
  const count = reviews ? reviews.length : 0
  if (count === 0) return '<div class="detail-section"><h3>Reviews</h3><p class="muted">No reviews yet.</p></div>'
  const items = reviews!.map(r => `
    <div class="sub-card">
      <button class="del-btn" data-type="review" data-rid="${r.reviewId}">Delete</button>
      <strong>${r.subject}</strong> <span class="muted">— ${r.author}</span>
      <p>${r.content}</p>
    </div>`).join('')
  return `<div class="detail-section"><h3>Reviews (${count})</h3>${items}</div>`
}

function recommendationsSection(recs?: Recommendation[] | null): string {
  const count = recs ? recs.length : 0
  if (count === 0) return '<div class="detail-section"><h3>Recommendations</h3><p class="muted">No recommendations yet.</p></div>'
  const items = recs!.map(r => `
    <div class="sub-card">
      <button class="del-btn" data-type="recommendation" data-rid="${r.recommendationId}">Delete</button>
      <strong>${r.author}</strong> ${stars(r.rate)} <span class="muted">(${r.rate}/5)</span>
      <p>${r.content}</p>
    </div>`).join('')
  return `<div class="detail-section"><h3>Recommendations (${count})</h3>${items}</div>`
}

async function renderDetail(id: string) {
  app.innerHTML = '<p>Loading...</p>'
  const res = await fetch(`${API}/${id}`)
  if (!res.ok) { app.innerHTML = '<p>Product not found. <a href="#/">Back</a></p>'; return }
  const p: Product = await res.json()
  const image = p.image
    ? `<img class="detail-img" src="${p.image.replace(/"/g, '&quot;')}" alt="" onerror="${IMG_ONERROR}">`
    : `<div class="placeholder">No image</div>`
  app.innerHTML = `
    <h2>${p.name}</h2>
    ${image}
    <p><strong>Brand:</strong> ${p.brand}</p>
    <p><strong>Category:</strong> ${p.category}</p>
    <p><strong>Price:</strong> $${p.price}</p>
    <p><strong>Rating:</strong> ${p.rating}</p>
    <p><strong>In Stock:</strong> ${p.countInStock}</p>
    <p><strong>Description:</strong> ${p.description}</p>
    ${recommendationsSection(p.recommendations)}
    <div class="detail-section">
      <h3>Recommend this product</h3>
      <form id="recommendation-form">
        <label>Your name</label><input name="author" required />
        <label>Rating (1-5)</label><input name="rate" type="number" min="1" max="5" value="5" required />
        <label>Comment</label><input name="content" required />
        <button type="submit">Submit recommendation</button>
      </form>
    </div>
    ${reviewsSection(p.reviews)}
    <div class="detail-section">
      <h3>Write a review</h3>
      <form id="review-form">
        <label>Your name</label><input name="author" required />
        <label>Subject</label><input name="subject" required />
        <label>Review</label><input name="content" required />
        <button type="submit">Submit review</button>
      </form>
    </div>
    <p style="margin-top:24px"><a href="#/">&larr; Back to list</a></p>
  `

  document.getElementById('review-form')!.addEventListener('submit', async (e) => {
    e.preventDefault()
    const form = e.target as HTMLFormElement
    const data = Object.fromEntries(new FormData(form))
    const res = await fetch(`${API}/${id}/review`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ author: data.author, subject: data.subject, content: data.content })
    })
    if (res.ok) { renderDetail(id) }   // reload to show the new review
    else { alert('Failed to submit review') }
  })

  document.getElementById('recommendation-form')!.addEventListener('submit', async (e) => {
    e.preventDefault()
    const form = e.target as HTMLFormElement
    const data = Object.fromEntries(new FormData(form))
    const res = await fetch(`${API}/${id}/recommendation`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ author: data.author, rate: Number(data.rate), content: data.content })
    })
    if (res.ok) { renderDetail(id) }   // reload to show the new recommendation
    else { alert('Failed to submit recommendation') }
  })

  app.querySelectorAll('.del-btn').forEach(btn => btn.addEventListener('click', async () => {
    const type = (btn as HTMLElement).dataset.type   // 'review' | 'recommendation'
    const rid = (btn as HTMLElement).dataset.rid
    if (!confirm(`Delete this ${type}?`)) return
    const res = await fetch(`${API}/${id}/${type}/${rid}`, { method: 'DELETE' })
    if (res.ok) { renderDetail(id) }   // reload to reflect the deletion
    else { alert(`Failed to delete ${type}`) }
  }))
}

function renderCreate() {
  app.innerHTML = `
    <h2>Add Product</h2>
    <form id="create-form">
      <label>Product ID</label><input name="productId" type="number" required />
      <label>Name</label><input name="name" required />
      <label>Brand</label><input name="brand" required />
      <label>Category</label><input name="category" required />
      <label>Price</label><input name="price" type="number" step="0.01" required />
      <label>Weight</label><input name="weight" type="number" required />
      <label>Count In Stock</label><input name="countInStock" type="number" required />
      <label>Description</label><input name="description" />
      <label>Image URL</label><input name="image" placeholder="https://..." />
      <button type="submit">Create Product</button>
    </form>
  `
  document.getElementById('create-form')!.addEventListener('submit', async (e) => {
    e.preventDefault()
    const form = e.target as HTMLFormElement
    const data = Object.fromEntries(new FormData(form))
    const body = {
      productId: Number(data.productId),
      name: data.name,
      brand: data.brand,
      category: data.category,
      price: Number(data.price),
      weight: Number(data.weight),
      countInStock: Number(data.countInStock),
      description: data.description || '',
      image: data.image || '',
      rating: 0,
      recommendations: [],
      reviews: []
    }
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    if (res.ok) { window.location.hash = '#/' }
    else { alert('Failed to create product') }
  })
}

function route() {
  stopPolling() // never leave a poll timer running on a non-list view
  const hash = window.location.hash || '#/'
  if (hash === '#/') renderList()
  else if (hash === '#/create') renderCreate()
  else if (hash.startsWith('#/product/')) renderDetail(hash.split('/')[2])
  else if (hash.startsWith('#/search/')) renderSearch(hash.split('/')[2])
  else renderList()
}

window.addEventListener('hashchange', route)
route()
