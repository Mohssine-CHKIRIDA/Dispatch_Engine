import { useEffect, useState, useCallback } from 'react'
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { fetchKpis } from '../api'

function secs(n) { return n == null ? '—' : `${n.toFixed(1)}s` }
function pct(n) { return n == null ? '—' : `${(n * 100).toFixed(0)}%` }

const COLORS = { accepted: '#2F8F5B', refused: '#B23B3B', timeout: '#BD7A2C' }

export default function KpisPanel({ config }) {
  const [kpis, setKpis] = useState(null)
  const [error, setError] = useState(null)

  const refresh = useCallback(async () => {
    try {
      const data = await fetchKpis(config.evaluation)
      setKpis(data)
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }, [config.evaluation])

  useEffect(() => {
    refresh()
    const id = setInterval(refresh, 5000)
    return () => clearInterval(id)
  }, [refresh])

  if (error) {
    return (
      <section>
        <h1>KPIs</h1>
        <div className="empty">Couldn't reach Evaluation: {error}</div>
      </section>
    )
  }

  if (!kpis) {
    return (
      <section>
        <h1>KPIs</h1>
        <div className="empty">Waiting for data…</div>
      </section>
    )
  }

  const pieData = [
    { name: 'Accepted', key: 'accepted', value: kpis.raw_outcome_counts?.accepted || 0 },
    { name: 'Refused', key: 'refused', value: kpis.raw_outcome_counts?.refused || 0 },
    { name: 'Timed out', key: 'timeout', value: kpis.raw_outcome_counts?.timeout || 0 },
  ].filter(d => d.value > 0)

  return (
    <section>
      <h1>KPIs</h1>
      <p className="sub">Computed by Evaluation from the full event log. Refreshes every 5s.</p>

      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="val mono">{secs(kpis.ttfa_seconds?.avg)}</div>
          <div className="label">Avg time to first proposal</div>
        </div>
        <div className="kpi-card">
          <div className="val mono">{secs(kpis.ttfa_seconds?.median)}</div>
          <div className="label">Median time to first proposal</div>
        </div>
        <div className="kpi-card">
          <div className="val mono">
            {kpis.avg_match_score_accepted != null ? kpis.avg_match_score_accepted.toFixed(3) : '—'}
          </div>
          <div className="label">Avg match score (accepted)</div>
        </div>
      </div>

      <div className="chart-card">
        <div className="title">Proposal outcomes</div>
        {pieData.length === 0 ? (
          <div className="empty">No resolved proposals yet.</div>
        ) : (
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={3}>
                {pieData.map(d => <Cell key={d.key} fill={COLORS[d.key]} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>

      <p className="sub">
        Acceptance rate: <strong className="mono">{pct(kpis.acceptance_rate)}</strong>
        {'  ·  '}Refusal rate: <strong className="mono">{pct(kpis.refusal_rate)}</strong>
        {'  ·  '}Timeout rate: <strong className="mono">{pct(kpis.timeout_rate)}</strong>
        {'  ·  '}Avg proposals/request:{' '}
        <strong className="mono">
          {kpis.avg_proposals_per_request != null ? kpis.avg_proposals_per_request.toFixed(2) : '—'}
        </strong>
      </p>
    </section>
  )
}
