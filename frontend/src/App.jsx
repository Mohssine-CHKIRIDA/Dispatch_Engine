import { useState } from 'react'
import Nav from './components/Nav'
import ConfigBar from './components/ConfigBar'
import SubmitPanel from './components/SubmitPanel'
import DoctorsPanel from './components/DoctorsPanel'
import BoardPanel from './components/BoardPanel'
import KpisPanel from './components/KpisPanel'
import { loadConfig, saveConfig } from './api'

export default function App() {
  const [tab, setTab] = useState('submit')
  const [config, setConfig] = useState(loadConfig())

  function handleConfigChange(next) {
    setConfig(next)
    saveConfig(next)
  }

  return (
    <div className="app">
      <Nav active={tab} onChange={setTab} />
      <main>
        <ConfigBar config={config} onChange={handleConfigChange} />
        {tab === 'submit' && <SubmitPanel config={config} />}
        {tab === 'doctors' && <DoctorsPanel config={config} />}
        {tab === 'board' && <BoardPanel config={config} />}
        {tab === 'kpis' && <KpisPanel config={config} />}
      </main>
    </div>
  )
}
