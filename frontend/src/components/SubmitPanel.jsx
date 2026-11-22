import { useEffect, useState } from 'react'
import { fetchRecentRequests, listDoctors, submitRequest } from '../api'

export default function SubmitPanel({ config }) {
  const [text, setText] = useState('')
  const [msg, setMsg] = useState(null)
  const [busy, setBusy] = useState(false)
  const [assignment, setAssignment] = useState(null)
  const requestId = assignment?.requestId
  const assignmentStatus = assignment?.status
  const assignedDoctorId = assignment?.doctorId
  const assignedDoctor = assignment?.doctor

  useEffect(() => {
    if (!requestId || assignmentStatus === 'accepted') return

    let active = true
    let timeout

    async function refreshAssignment() {
      try {
        const rows = await fetchRecentRequests(config.evaluation, 500)
        const request = rows.find(row => row.request_id === requestId)

        if (request?.doctor_id) {
          let doctor = assignedDoctorId === request.doctor_id ? assignedDoctor : null
          let profileError = null
          if (!doctor) {
            try {
              const doctors = await listDoctors(config.pros)
              doctor = doctors.find(profile => String(profile.id) === String(request.doctor_id)) || null
              if (!doctor) profileError = 'Doctor profile was not found.'
            } catch (e) {
              profileError = `Could not load doctor profile: ${e.message}`
            }
          }

          if (active) {
            setAssignment(current => current?.requestId === requestId
              ? {
                  ...current,
                  status: request.outcome,
                  doctorId: request.doctor_id,
                  doctor,
                  profileError,
                  lookupError: null,
                }
              : current)
          }
        } else if (active) {
          setAssignment(current => current?.requestId === requestId
            ? { ...current, lookupError: null }
            : current)
        }
      } catch (e) {
        if (active) {
          setAssignment(current => current?.requestId === requestId
            ? { ...current, lookupError: `Could not check assignment: ${e.message}` }
            : current)
        }
      } finally {
        if (active) timeout = setTimeout(refreshAssignment, 3000)
      }
    }

    refreshAssignment()
    return () => {
      active = false
      clearTimeout(timeout)
    }
  }, [requestId, assignmentStatus, assignedDoctorId, assignedDoctor, config.evaluation, config.pros])

  async function handleSubmit() {
    if (!text.trim()) return
    setBusy(true)
    setMsg(null)
    setAssignment(null)
    try {
      const response = await submitRequest(config.ingestion, text.trim())
      setAssignment(response.requestId
        ? { requestId: response.requestId, status: 'searching' }
        : { status: 'tracking_error' })
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
      {assignment && (
        <div className={`msg ${assignment.doctorId ? 'ok' : ''}`} role="status" aria-live="polite">
          {assignment.status === 'tracking_error' ? (
            <>Request submitted, but the server did not return an ID to track its assignment.</>
          ) : assignment.doctorId ? (
            <>
              {assignment.status === 'accepted'
                ? 'Doctor accepted the request.'
                : assignment.status === 'refused' || assignment.status === 'timeout'
                  ? 'The previous match was declined or timed out; searching for another doctor.'
                  : 'Matched with a doctor.'}
              <div>
                <strong>{assignment.doctor?.specialty || 'Doctor'}</strong>
                {assignment.doctor?.description ? ` — ${assignment.doctor.description}` : ''}
              </div>
              <div className="mono" style={{ fontSize: 12, marginTop: 4 }}>
                Doctor ID: {assignment.doctorId}
              </div>
              {assignment.profileError && <div>{assignment.profileError}</div>}
            </>
          ) : (
            <>Request submitted. Looking for a doctor…</>
          )}
          {assignment.lookupError && <div>{assignment.lookupError}</div>}
          {assignment.requestId && (
            <div className="mono" style={{ fontSize: 12, marginTop: 4 }}>
              Request ID: {assignment.requestId}
            </div>
          )}
        </div>
      )}
    </section>
  )
}
