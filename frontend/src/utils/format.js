export function money(value) {
  if (value === null || value === undefined || value === '') return 'Not disclosed'
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(Number(value))
}

export function date(value) {
  if (!value) return 'No deadline'
  return new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value))
}

export function sentence(value) {
  if (!value) return ''
  return value
    .toString()
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/^\w/, (letter) => letter.toUpperCase())
}

export const applicationStatuses = ['APPLIED', 'SHORTLISTED', 'REJECTED', 'SELECTED', 'OFFERED']
