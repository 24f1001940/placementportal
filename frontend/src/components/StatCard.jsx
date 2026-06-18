export default function StatCard({ icon: Icon, label, value, tone = 'blue' }) {
  const toneClass = {
    blue: 'bg-blue-50 text-blue-700',
    teal: 'bg-teal-50 text-teal-700',
    amber: 'bg-amber-50 text-amber-700',
    rose: 'bg-rose-50 text-rose-700',
  }[tone]

  return (
    <div className="surface rounded-lg p-4">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            {label}
          </p>
          <p className="mt-2 text-3xl font-black">{value}</p>
        </div>
        <div className={`grid h-10 w-10 place-items-center rounded-lg ${toneClass}`}>
          <Icon size={21} />
        </div>
      </div>
    </div>
  )
}
