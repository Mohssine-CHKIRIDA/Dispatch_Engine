const TABS = [
  { id: "submit", label: "Submit request" },
  { id: "doctors", label: "Doctors" },
  { id: "board", label: "Live board" },
  { id: "kpis", label: "KPIs" },
];

export default function Nav({ active, onChange }) {
  return (
    <nav>
      <div className="brand">
        <img src="/logo.jpeg" alt="Pulsaride" />
        <div className="name">
          Pulsaride
          <span>Dispatch console</span>
        </div>
      </div>
      {TABS.map((tab) => (
        <button
          key={tab.id}
          className={active === tab.id ? "active" : ""}
          onClick={() => onChange(tab.id)}
        >
          <span className="pulse-dot" />
          {tab.label}
        </button>
      ))}
    </nav>
  );
}
