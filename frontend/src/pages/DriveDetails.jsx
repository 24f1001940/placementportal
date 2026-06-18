import { ArrowLeft, Send } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router-dom'
import Badge from '../components/Badge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import api, { downloadFile, errorMessage } from '../services/api.js'
import { applicationStatuses, date, money, sentence } from '../utils/format.js'

export default function DriveDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const [drive, setDrive] = useState(null)
  const [applications, setApplications] = useState([])
  const [message, setMessage] = useState('')
  const { register, handleSubmit, reset, formState } = useForm()

  const load = useCallback(async () => {
    const { data } = await api.get(`/drives/${id}`)
    setDrive(data)
    if (user?.role === 'COMPANY' || user?.role === 'ADMIN') {
      const appRes = await api.get(`/applications/drive/${id}`)
      setApplications(appRes.data)
    }
  }, [id, user?.role])

  useEffect(() => {
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load])

  const apply = async (values) => {
    try {
      await api.post('/applications', { driveId: Number(id), coverLetter: values.coverLetter })
      reset()
      setMessage('Application submitted.')
    } catch (err) {
      setMessage(errorMessage(err))
    }
  }

  const updateStatus = async (applicationId, status) => {
    await api.patch(`/applications/${applicationId}/status`, { status })
    await load()
  }

  if (!drive) {
    return (
      <div className="space-y-4">
        <Link className="btn btn-secondary" to="/">
          <ArrowLeft size={18} />
          Back
        </Link>
        <EmptyState title={message || 'Loading drive details'} />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <Link className="btn btn-secondary" to="/">
        <ArrowLeft size={18} />
        Back
      </Link>

      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}

      <section className="surface rounded-lg p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="page-title">{drive.title}</p>
            <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
              {drive.company.companyName} - {drive.jobRole}
            </p>
          </div>
          <Badge value={drive.status} />
        </div>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="muted-surface rounded-lg p-4">
            <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
              Package
            </p>
            <p className="mt-1 font-black">{money(drive.annualPackage)}</p>
          </div>
          <div className="muted-surface rounded-lg p-4">
            <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
              Deadline
            </p>
            <p className="mt-1 font-black">{date(drive.deadline)}</p>
          </div>
          <div className="muted-surface rounded-lg p-4">
            <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
              Min CGPA
            </p>
            <p className="mt-1 font-black">{drive.minCgpa || 'Any'}</p>
          </div>
          <div className="muted-surface rounded-lg p-4">
            <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
              Applicants
            </p>
            <p className="mt-1 font-black">{drive.applicantCount}</p>
          </div>
        </div>

        <div className="mt-6 grid gap-5 lg:grid-cols-[1fr_0.8fr]">
          <div>
            <h2 className="section-title">Description</h2>
            <p className="mt-2 whitespace-pre-line leading-7" style={{ color: 'var(--muted)' }}>
              {drive.description || 'No description provided.'}
            </p>
          </div>
          <div className="muted-surface rounded-lg p-4">
            <h2 className="section-title">Eligibility</h2>
            <p className="mt-2 text-sm" style={{ color: 'var(--muted)' }}>
              Branches: {drive.eligibleBranches || 'All branches'}
            </p>
            <p className="mt-2 text-sm" style={{ color: 'var(--muted)' }}>
              Location: {drive.location || 'Not specified'}
            </p>
          </div>
        </div>
      </section>

      {user?.role === 'STUDENT' ? (
        <form className="surface rounded-lg p-5" onSubmit={handleSubmit(apply)}>
          <h2 className="section-title">Apply for this drive</h2>
          <textarea className="field mt-3 min-h-28" placeholder="Cover letter" {...register('coverLetter')} />
          <button className="btn btn-primary mt-4" type="submit" disabled={formState.isSubmitting || drive.status !== 'APPROVED'}>
            <Send size={18} />
            Submit application
          </button>
        </form>
      ) : null}

      {user?.role === 'COMPANY' || user?.role === 'ADMIN' ? (
        <section className="space-y-3">
          <h2 className="section-title">Drive applicants</h2>
          {applications.length ? (
            <div className="table-wrap">
              <table className="portal-table">
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Profile</th>
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
                          {application.student.user.email}
                        </p>
                      </td>
                      <td>
                        {application.student.branch || 'Branch not set'} - CGPA {application.student.cgpa || 'NA'}
                      </td>
                      <td>
                        <Badge value={application.status} />
                      </td>
                      <td>
                        <button className="btn btn-secondary" type="button" onClick={() => downloadFile(`/resumes/student/${application.student.id}/download`, `${application.student.user.fullName}-resume.pdf`)}>
                          Resume
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
            <EmptyState title="No applicants for this drive" />
          )}
        </section>
      ) : null}
    </div>
  )
}
