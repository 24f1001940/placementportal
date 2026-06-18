import { BriefcaseBusiness, ClipboardList, Plus, UsersRound } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import Badge from '../components/Badge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import StatCard from '../components/StatCard.jsx'
import api, { downloadFile, errorMessage } from '../services/api.js'
import { applicationStatuses, date, money, sentence } from '../utils/format.js'

export default function CompanyDashboard() {
  const [company, setCompany] = useState(null)
  const [drives, setDrives] = useState([])
  const [applications, setApplications] = useState([])
  const [message, setMessage] = useState('')
  const { register, handleSubmit, reset, formState } = useForm()

  const load = useCallback(async () => {
    const [companyRes, driveRes, appRes] = await Promise.all([
      api.get('/companies/me'),
      api.get('/drives/mine'),
      api.get('/applications/company'),
    ])
    setCompany(companyRes.data)
    setDrives(driveRes.data)
    setApplications(appRes.data)
  }, [])

  useEffect(() => {
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load])

  const createDrive = async (values) => {
    setMessage('')
    try {
      await api.post('/drives', values)
      reset()
      setMessage('Drive submitted for admin approval.')
      await load()
    } catch (err) {
      setMessage(errorMessage(err))
    }
  }

  const updateStatus = async (id, status) => {
    await api.patch(`/applications/${id}/status`, { status })
    await load()
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="page-title">Company dashboard</p>
        <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
          Create drives, view applicants, shortlist students, and update application status.
        </p>
      </div>

      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}
      {company && !company.approved ? <p className="rounded-lg bg-amber-50 px-4 py-3 text-sm font-bold text-amber-800">Your company is pending admin approval.</p> : null}

      <div className="grid gap-4 md:grid-cols-3">
        <StatCard icon={BriefcaseBusiness} label="Drives" value={drives.length} />
        <StatCard icon={UsersRound} label="Applicants" value={applications.length} tone="teal" />
        <StatCard icon={ClipboardList} label="Approved drives" value={drives.filter((drive) => drive.status === 'APPROVED').length} tone="amber" />
      </div>

      <section className="grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
        <form className="surface rounded-lg p-4" onSubmit={handleSubmit(createDrive)}>
          <h2 className="section-title mb-4 flex items-center gap-2">
            <Plus size={19} /> Create placement drive
          </h2>
          <div className="grid gap-3 sm:grid-cols-2">
            <label className="block">
              <span className="text-sm font-bold">Title</span>
              <input className="field mt-1" {...register('title', { required: true })} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Job role</span>
              <input className="field mt-1" {...register('jobRole', { required: true })} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Location</span>
              <input className="field mt-1" {...register('location')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Package</span>
              <input className="field mt-1" type="number" {...register('annualPackage')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Min CGPA</span>
              <input className="field mt-1" step="0.1" type="number" {...register('minCgpa')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Deadline</span>
              <input className="field mt-1" type="date" {...register('deadline')} />
            </label>
          </div>
          <label className="mt-3 block">
            <span className="text-sm font-bold">Eligible branches</span>
            <input className="field mt-1" {...register('eligibleBranches')} />
          </label>
          <label className="mt-3 block">
            <span className="text-sm font-bold">Description</span>
            <textarea className="field mt-1 min-h-28" {...register('description')} />
          </label>
          <button className="btn btn-primary mt-4" type="submit" disabled={formState.isSubmitting || company?.approved === false}>
            Submit drive
          </button>
        </form>

        <section className="space-y-3">
          <h2 className="section-title">Company drives</h2>
          {drives.length ? (
            drives.map((drive) => (
              <article className="surface rounded-lg p-4" key={drive.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <Link className="font-black hover:underline" to={`/drives/${drive.id}`}>
                      {drive.title}
                    </Link>
                    <p className="text-sm" style={{ color: 'var(--muted)' }}>
                      {drive.jobRole} - {money(drive.annualPackage)} - deadline {date(drive.deadline)}
                    </p>
                  </div>
                  <Badge value={drive.status} />
                </div>
              </article>
            ))
          ) : (
            <EmptyState title="No drives created yet" />
          )}
        </section>
      </section>

      <section className="space-y-3">
        <h2 className="section-title">Applicants</h2>
        {applications.length ? (
          <div className="table-wrap">
            <table className="portal-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Drive</th>
                  <th>Status</th>
                  <th>Resume</th>
                  <th>Update</th>
                </tr>
              </thead>
              <tbody>
                {applications.map((application) => (
                  <tr key={application.id}>
                    <td>
                      <p className="font-bold">{application.student.user.fullName}</p>
                      <p className="text-sm" style={{ color: 'var(--muted)' }}>
                        {application.student.branch || 'Branch not set'} - CGPA {application.student.cgpa || 'NA'}
                      </p>
                    </td>
                    <td>{application.drive.title}</td>
                    <td>
                      <Badge value={application.status} />
                    </td>
                    <td>
                      <button className="font-bold" style={{ color: 'var(--primary)' }} type="button" onClick={() => downloadFile(`/resumes/student/${application.student.id}/download`, `${application.student.user.fullName}-resume.pdf`)}>
                        Open
                      </button>
                    </td>
                    <td>
                      <select className="field" value={application.status} onChange={(event) => updateStatus(application.id, event.target.value)}>
                        {applicationStatuses.map((status) => (
                          <option key={status} value={status}>
                            {sentence(status)}
                          </option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState title="No applicants yet" />
        )}
      </section>
    </div>
  )
}
