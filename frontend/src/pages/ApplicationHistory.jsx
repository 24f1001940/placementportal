import { Download } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import Badge from '../components/Badge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import api, { downloadFile, errorMessage } from '../services/api.js'
import { applicationStatuses, date, money, sentence } from '../utils/format.js'

export default function ApplicationHistory() {
  const { user } = useAuth()
  const [applications, setApplications] = useState([])
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    const endpoint = user?.role === 'COMPANY' ? '/applications/company' : '/applications/me'
    const { data } = await api.get(endpoint)
    setApplications(data)
  }, [user?.role])

  useEffect(() => {
    if (user?.role === 'ADMIN') return
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load, user?.role])

  const updateStatus = async (id, status) => {
    try {
      await api.patch(`/applications/${id}/status`, { status })
      await load()
    } catch (err) {
      setMessage(errorMessage(err))
    }
  }

  if (user?.role === 'ADMIN') {
    return (
      <div className="surface rounded-lg p-5">
        <p className="section-title">Application reports</p>
        <p className="mt-2 text-sm" style={{ color: 'var(--muted)' }}>
          Export the placement application report from the admin dashboard.
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <p className="page-title">{user?.role === 'COMPANY' ? 'Applicants' : 'Application history'}</p>
        <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
          {user?.role === 'COMPANY' ? 'Review applications and update student status.' : 'Track every placement drive application.'}
        </p>
      </div>
      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}

      {applications.length ? (
        <div className="table-wrap">
          <table className="portal-table">
            <thead>
              <tr>
                <th>{user?.role === 'COMPANY' ? 'Student' : 'Company'}</th>
                <th>Drive</th>
                <th>Package</th>
                <th>Applied</th>
                <th>Status</th>
                {user?.role === 'COMPANY' ? <th>Resume</th> : null}
                {user?.role === 'COMPANY' ? <th>Update</th> : null}
              </tr>
            </thead>
            <tbody>
              {applications.map((application) => (
                <tr key={application.id}>
                  <td>
                    <p className="font-bold">{user?.role === 'COMPANY' ? application.student.user.fullName : application.drive.company.companyName}</p>
                    <p className="text-sm" style={{ color: 'var(--muted)' }}>
                      {user?.role === 'COMPANY' ? application.student.user.email : application.drive.company.location || 'Location not set'}
                    </p>
                  </td>
                  <td>{application.drive.title}</td>
                  <td>{money(application.drive.annualPackage)}</td>
                  <td>{date(application.appliedAt)}</td>
                  <td>
                    <Badge value={application.status} />
                  </td>
                  {user?.role === 'COMPANY' ? (
                    <td>
                      <button className="btn btn-secondary" type="button" onClick={() => downloadFile(`/resumes/student/${application.student.id}/download`, `${application.student.user.fullName}-resume.pdf`)}>
                        <Download size={16} />
                        Resume
                      </button>
                    </td>
                  ) : null}
                  {user?.role === 'COMPANY' ? (
                    <td>
                      <select className="field" value={application.status} onChange={(event) => updateStatus(application.id, event.target.value)}>
                        {applicationStatuses.map((status) => (
                          <option key={status} value={status}>
                            {sentence(status)}
                          </option>
                        ))}
                      </select>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState title={user?.role === 'COMPANY' ? 'No applicants yet' : 'No applications yet'} />
      )}
    </div>
  )
}
