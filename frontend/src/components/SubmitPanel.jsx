import { useState } from 'react'
import { submitRequest } from '../api'

export default function SubmitPanel({ config }) {
  const [text, setText] = useState('')
  const [msg, setMsg] = useState(null)
  const [busy, setBusy] = useState(false)

  async function handleSubmit() {
    if (!text.trim()) return
    setBusy(true)
    setMsg(null)
    try {
      await submitRequest(config.ingestion, text.trim())
      setMsg({ ok: true, text: 'Request submitted — check the Live board shortly.' })
      setText('')
    } catch (e) {
      setMsg({ ok: false, text: `Failed to submit: ${e.message}. Check the Ingestion URL and CORS config.` })
    } finally {
      setBusy(false)
      setTimeout(() => setMsg(null), 5000)
    }
  }

  return (
    <section>
      <h1>Submit a patient request</h1>
      <p className="sub">Sends free-text patient description to Ingestion, exactly like the scenario simulator does.</p>
      <div className="field">
        <label>Patient text</label>
        <textarea
          value={text}
          onChange={e => setText(e.target.value)}
          placeholder="e.g. I have a fever for 3 days and my 5-year-old son has difficulty swallowing."
        />
      </div>
      <button className="btn" onClick={handleSubmit} disabled={busy}>
        {busy ? 'Submitting…' : 'Submit request'}
      </button>
      {msg && <div className={`msg ${msg.ok ? 'ok' : 'err'}`}>{msg.text}</div>}
    </section>
  )
}
