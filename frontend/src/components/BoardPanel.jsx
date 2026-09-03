import { useEffect, useState, useCallback } from 'react'
import { fetchRecentRequests, assignAction } from '../api'

function urgencyClass(u) {
  return `u${u ?? 0}`
}

export default function BoardPanel({ config }) {
  const [rows, setRows] = useState(null)
  const [error, setError] = useState(null)

  const refresh = useCallback(async () => {
    try {
      const data = await fetchRecentRequests(config.evaluation, 30)
      setRows(data)
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }, [config.evaluation])

  useEffect(() => {
    refresh()
    const id = setInterval(refresh, 3000)
    return () => clearInterval(id)
  }, [refresh])

  async function handleAction(requestId, doctorId, action) {
    try {
      await assignAction(config.dispatch, requestId, doctorId, action)
      refresh()
    } catch (e) {
      alert(`Failed to ${action}: ${e.message}`)
    }
  }

  return (
    <section>
      <h1>Live board</h1>
      <p className="sub">Polls Evaluation's request feed every 3s. Manual accept/refuse/close buttons stand in for a real doctor client.</p>

      <div className="board-row head">
        <div></div><div>request</div><div>doctor / score</div><div>urgency</div><div>status</div><div>actions</div>
      </div>

      {error && <div className="empty">Couldn't reach Evaluation: {error}</div>}
      {!error && rows === null && <div className="empty">Waiting for data…</div>}
      {!error && rows && rows.length === 0 && (
        <div className="empty">No requests yet — submit one to see it here.</div>
      )}
      {!error && rows && rows.map(r => (
        <div className="board-row" key={r.request_id}>
          <div className={`urgency-bar ${urgencyClass(r.urgency_score)}`} />
          <div className="mono">{r.request_id.slice(0, 8)}</div>
          <div className="mono" style={{ fontSize: 12, color: 'var(--ink-soft)' }}>
            {r.doctor_id ? r.doctor_id.slice(0, 8) : '—'}
            {r.match_score != null ? ` · ${r.match_score.toFixed(3)}` : ''}
          </div>
          <div>{r.urgency_score ?? '—'}</div>
          <div><span className={`badge ${r.outcome}`}>{r.outcome.replace('_', ' ')}</span></div>
          <div className="actions">
            {r.outcome === 'pending' && (
              <>
                <button className="btn ghost small" onClick={() => handleAction(r.request_id, r.doctor_id, 'accept')}>✓</button>
                <button className="btn ghost small" onClick={() => handleAction(r.request_id, r.doctor_id, 'refuse')}>✕</button>
              </>
            )}
            {r.outcome === 'accepted' && (
              <button className="btn ghost small" onClick={() => handleAction(r.request_id, r.doctor_id, 'close')}>Close</button>
            )}
          </div>
        </div>
      ))}
    </section>
  )
}
