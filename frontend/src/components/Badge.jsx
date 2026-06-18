import { sentence } from '../utils/format.js'

const styles = {
  APPROVED: 'border-emerald-300 bg-emerald-50 text-emerald-700',
  PENDING: 'border-amber-300 bg-amber-50 text-amber-700',
  REJECTED: 'border-red-300 bg-red-50 text-red-700',
  APPLIED: 'border-blue-300 bg-blue-50 text-blue-700',
  SHORTLISTED: 'border-cyan-300 bg-cyan-50 text-cyan-700',
  SELECTED: 'border-emerald-300 bg-emerald-50 text-emerald-700',
  OFFERED: 'border-teal-300 bg-teal-50 text-teal-700',
  ADMIN: 'border-slate-300 bg-slate-50 text-slate-700',
  COMPANY: 'border-indigo-300 bg-indigo-50 text-indigo-700',
  STUDENT: 'border-orange-300 bg-orange-50 text-orange-700',
}

export default function Badge({ value }) {
  return <span className={`status-pill ${styles[value] || ''}`}>{sentence(value)}</span>
}
