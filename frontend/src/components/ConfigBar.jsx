const FIELDS = [
  { key: 'ingestion', label: 'Ingestion' },
  { key: 'pros', label: 'Pro Registry' },
  { key: 'dispatch', label: 'Dispatch Core' },
  { key: 'evaluation', label: 'Evaluation' },
]

export default function ConfigBar({ config, onChange }) {
  return (
    <div className="config">
      {FIELDS.map(f => (
        <div key={f.key}>
          <label>{f.label}</label>
          <input
            type="text"
            value={config[f.key]}
            onChange={e => onChange({ ...config, [f.key]: e.target.value })}
          />
        </div>
      ))}
    </div>
  )
}
