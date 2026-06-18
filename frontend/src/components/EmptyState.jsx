import { Inbox } from 'lucide-react'

export default function EmptyState({ title, action }) {
  return (
    <div className="muted-surface rounded-lg px-4 py-8 text-center">
      <Inbox className="mx-auto mb-3" size={28} style={{ color: 'var(--muted)' }} />
      <p className="font-bold">{title}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  )
}
