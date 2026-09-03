import { useEffect, useState, useCallback, useRef } from 'react'
import { createDoctor, listDoctors, toggleDoctorAvailability } from '../api'

export default function DoctorsPanel({ config }) {
  const [specialty, setSpecialty] = useState('')
  const [description, setDescription] = useState('')
  const [doctors, setDoctors] = useState([])
  const [msg, setMsg] = useState(null)
  const localDoctors = useRef([]) // fallback when GET /pros doesn't exist yet

  const refresh = useCallback(async () => {
    try {
      const remote = await listDoctors(config.pros)
      setDoctors(remote)
    } catch {
      // No list endpoint yet - fall back to what we've created this session.
      setDoctors(localDoctors.current)
    }
  }, [config.pros])

  useEffect(() => {
    refresh()
    const id = setInterval(refresh, 8000)
    return () => clearInterval(id)
  }, [refresh])

  function flash(ok, text) {
    setMsg({ ok, text })
    setTimeout(() => setMsg(null), 5000)
  }

  async function handleCreate() {
    if (!specialty.trim() || !description.trim()) return
    try {
      const data = await createDoctor(config.pros, { specialty, description })
      localDoctors.current = [
        ...localDoctors.current,
        { id: data.id || data.proId || '(unknown)', specialty, description, available: true },
      ]
      setSpecialty('')
      setDescription('')
      flash(true, 'Doctor created.')
      refresh()
    } catch (e) {
      flash(false, `Failed to create doctor: ${e.message}`)
    }
  }

  async function handleToggle(id, current) {
    try {
      await toggleDoctorAvailability(config.pros, id, !current)
      localDoctors.current = localDoctors.current.map(d =>
        d.id === id ? { ...d, available: !current } : d
      )
      refresh()
    } catch (e) {
      flash(false, `Failed to update availability: ${e.message}`)
    }
  }

  return (
    <section>
      <h1>Doctors</h1>
      <p className="sub">Create profiles via Pro Registry and toggle availability.</p>
      <div className="row">
        <input
          type="text" placeholder="Specialty (e.g. cardiologie)" style={{ flex: 1 }}
          value={specialty} onChange={e => setSpecialty(e.target.value)}
        />
        <input
          type="text" placeholder="Description" style={{ flex: 2 }}
          value={description} onChange={e => setDescription(e.target.value)}
        />
        <button className="btn small" onClick={handleCreate}>Add doctor</button>
      </div>
      {msg && <div className={`msg ${msg.ok ? 'ok' : 'err'}`}>{msg.text}</div>}

      <div style={{ marginTop: 16 }}>
        <div className="doctor-row head">
          <div>Specialty / description</div><div>ID</div><div>Status</div><div></div>
        </div>
        {doctors.length === 0 && <div className="empty">No doctors yet.</div>}
        {doctors.map(d => (
          <div className="doctor-row" key={d.id}>
            <div>
              <strong>{d.specialty}</strong>
              <div style={{ color: 'var(--ink-soft)', fontSize: 12 }}>{d.description}</div>
            </div>
            <div className="mono">{String(d.id).slice(0, 8)}</div>
            <div>
              <span className={`dot ${d.available ? 'on' : 'off'}`} />
              {d.available ? 'Available' : 'Busy'}
            </div>
            <div>
              <button className="btn ghost small" onClick={() => handleToggle(d.id, !!d.available)}>Toggle</button>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
