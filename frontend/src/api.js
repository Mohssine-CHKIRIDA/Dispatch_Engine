const DEFAULTS = {
  ingestion: 'http://localhost:8080',
  pros: 'http://localhost:9091',
  dispatch: 'http://localhost:9092',
  evaluation: 'http://localhost:8091',
}

export function loadConfig() {
  try {
    const saved = JSON.parse(localStorage.getItem('pulsaride-config') || '{}')
    return { ...DEFAULTS, ...saved }
  } catch {
    return DEFAULTS
  }
}

export function saveConfig(cfg) {
  localStorage.setItem('pulsaride-config', JSON.stringify(cfg))
}

function strip(url) {
  return url.replace(/\/$/, '')
}

export async function submitRequest(baseUrl, rawText) {
  const res = await fetch(`${strip(baseUrl)}/requests`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawText }),
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json().catch(() => ({}))
}

export async function createDoctor(baseUrl, { specialty, description }) {
  const res = await fetch(`${strip(baseUrl)}/pros`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ specialty, description }),
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json().catch(() => ({}))
}

export async function listDoctors(baseUrl) {
  const res = await fetch(`${strip(baseUrl)}/pros`)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

export async function toggleDoctorAvailability(baseUrl, id, available) {
  const res = await fetch(`${strip(baseUrl)}/pros/${id}/availability`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ available }),
  })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
}

export async function fetchRecentRequests(baseUrl, limit = 30) {
  const res = await fetch(`${strip(baseUrl)}/requests/recent?limit=${limit}`)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

export async function fetchKpis(baseUrl) {
  const res = await fetch(`${strip(baseUrl)}/kpis`)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

export async function assignAction(baseUrl, requestId, doctorId, action) {
  const res = await fetch(
    `${strip(baseUrl)}/assignments/${requestId}/${action}?doctorId=${doctorId}`,
    { method: 'POST' }
  )
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
}
