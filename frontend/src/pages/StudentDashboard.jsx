import { BriefcaseBusiness, CheckCircle2, FileUp, Search } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Badge from '../components/Badge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import StatCard from '../components/StatCard.jsx'
import api, { errorMessage } from '../services/api.js'
import { date, money } from '../utils/format.js'

export default function StudentDashboard() {
  const [profile, setProfile] = useState(null)
  const [drives, setDrives] = useState([])
  const [applications, setApplications] = useState([])
  const [query, setQuery] = useState('')
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    const [profileRes, driveRes, appRes] = await Promise.all([
      api.get('/students/me'),
      api.get('/drives', { params: { query, size: 12 } }),
      api.get('/applications/me'),
    ])
    setProfile(profileRes.data)
    setDrives(driveRes.data.content)
    setApplications(appRes.data)
  }, [query])

  useEffect(() => {
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load])

  const appliedDriveIds = useMemo(() => new Set(applications.map((item) => item.drive.id)), [applications])

  const apply = async (driveId) => {
    setMessage('')
    try {
      await api.post('/applications', { driveId, coverLetter: 'Interested in this placement drive.' })
      setMessage('Application submitted.')
      await load()
    } catch (err) {
      setMessage(errorMessage(err))
    }
  }

  const uploadResume = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return
    const form = new FormData()
    form.append('file', file)
    try {
      await api.post('/resumes', form)
      setMessage('Resume uploaded.')
      await load()
    } catch (err) {
      setMessage(errorMessage(err))
    }
  }

  const shortlisted = applications.filter((item) => ['SHORTLISTED', 'SELECTED', 'OFFERED'].includes(item.status)).length

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="page-title">Student dashboard</p>
          <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            Browse approved drives, upload resume, and track applications.
          </p>
        </div>
        <label className="btn btn-secondary">
          <FileUp size={18} />
          Upload resume
          <input className="hidden" type="file" accept="application/pdf,.pdf" onChange={uploadResume} />
        </label>
      </div>

      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}

      <div className="grid gap-4 md:grid-cols-3">
        <StatCard icon={BriefcaseBusiness} label="Open drives" value={drives.length} />
        <StatCard icon={CheckCircle2} label="Applications" value={applications.length} tone="teal" />
        <StatCard icon={FileUp} label="Resume" value={profile?.resume ? 'Uploaded' : 'Missing'} tone={profile?.resume ? 'teal' : 'amber'} />
      </div>

      <section className="space-y-4">
        <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <h2 className="section-title">Approved drives</h2>
          <form
            className="flex gap-2"
            onSubmit={(event) => {
              event.preventDefault()
              load().catch((err) => setMessage(errorMessage(err)))
            }}
          >
            <input className="field min-w-0 sm:w-72" placeholder="Search drives" value={query} onChange={(event) => setQuery(event.target.value)} />
            <button className="icon-btn" title="Search" type="submit">
              <Search size={18} />
            </button>
          </form>
        </div>

        {drives.length ? (
          <div className="grid gap-4 lg:grid-cols-2">
            {drives.map((drive) => (
              <article className="surface rounded-lg p-4" key={drive.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <Link className="text-lg font-black hover:underline" to={`/drives/${drive.id}`}>
                      {drive.title}
                    </Link>
                    <p className="mt-1 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
                      {drive.company.companyName} - {drive.jobRole}
                    </p>
                  </div>
                  <Badge value={drive.status} />
                </div>
                <div className="mt-4 grid gap-3 text-sm sm:grid-cols-3">
                  <p>
                    <span className="font-black">Package</span>
                    <br />
                    {money(drive.annualPackage)}
                  </p>
                  <p>
                    <span className="font-black">Min CGPA</span>
                    <br />
                    {drive.minCgpa || 'Any'}
                  </p>
                  <p>
                    <span className="font-black">Deadline</span>
                    <br />
                    {date(drive.deadline)}
                  </p>
                </div>
                <div className="mt-4 flex flex-wrap gap-2">
                  <Link className="btn btn-secondary" to={`/drives/${drive.id}`}>
                    Details
                  </Link>
                  <button className="btn btn-primary" type="button" disabled={appliedDriveIds.has(drive.id)} onClick={() => apply(drive.id)}>
                    {appliedDriveIds.has(drive.id) ? 'Applied' : 'Apply'}
                  </button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No approved drives found" />
        )}
      </section>

      <section className="surface rounded-lg p-4">
        <h2 className="section-title">Application summary</h2>
        <p className="mt-2 text-sm" style={{ color: 'var(--muted)' }}>
          {shortlisted} active positive outcomes from {applications.length} total applications.
        </p>
      </section>
    </div>
  )
}
