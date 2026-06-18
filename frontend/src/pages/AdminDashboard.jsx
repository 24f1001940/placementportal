import { Ban, BriefcaseBusiness, Building2, Check, Download, Search, UsersRound } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import Badge from '../components/Badge.jsx'
import EmptyState from '../components/EmptyState.jsx'
import StatCard from '../components/StatCard.jsx'
import api, { downloadFile, errorMessage } from '../services/api.js'
import { date, money } from '../utils/format.js'

const chartColors = ['#2563eb', '#0f766e', '#b45309', '#b42318']

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [companies, setCompanies] = useState([])
  const [drives, setDrives] = useState([])
  const [users, setUsers] = useState([])
  const [query, setQuery] = useState('')
  const [role, setRole] = useState('')
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    const [statsRes, companiesRes, drivesRes, usersRes] = await Promise.all([
      api.get('/admin/stats'),
      api.get('/admin/companies/pending'),
      api.get('/admin/drives/pending'),
      api.get('/admin/users', { params: { query: query || undefined, role: role || undefined } }),
    ])
    setStats(statsRes.data)
    setCompanies(companiesRes.data.content)
    setDrives(drivesRes.data.content)
    setUsers(usersRes.data.content)
  }, [query, role])

  useEffect(() => {
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load])

  const chartData = useMemo(
    () => [
      { name: 'Students', value: stats?.students || 0 },
      { name: 'Companies', value: stats?.companies || 0 },
      { name: 'Drives', value: stats?.drives || 0 },
      { name: 'Applications', value: stats?.applications || 0 },
    ],
    [stats],
  )

  const approveCompany = async (id, approved = true) => {
    await api.patch(`/companies/${id}/approve`, null, { params: { approved } })
    await load()
  }

  const updateDrive = async (id, status) => {
    await api.patch(`/drives/${id}/status`, { status })
    await load()
  }

  const blacklist = async (id, blacklisted) => {
    await api.patch(`/admin/users/${id}/blacklist`, { blacklisted })
    await load()
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="page-title">Admin dashboard</p>
          <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            Approve companies and drives, search users, blacklist accounts, and export reports.
          </p>
        </div>
        <button className="btn btn-secondary" type="button" onClick={() => downloadFile('/admin/reports/applications.csv', 'placement-applications.csv')}>
          <Download size={18} />
          Export CSV
        </button>
      </div>

      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard icon={UsersRound} label="Students" value={stats?.students ?? '-'} />
        <StatCard icon={Building2} label="Companies" value={stats?.companies ?? '-'} tone="teal" />
        <StatCard icon={BriefcaseBusiness} label="Drives" value={stats?.drives ?? '-'} tone="amber" />
        <StatCard icon={Check} label="Applications" value={stats?.applications ?? '-'} tone="rose" />
      </div>

      <section className="grid gap-5 xl:grid-cols-[0.8fr_1.2fr]">
        <div className="surface rounded-lg p-4">
          <h2 className="section-title">Portal analytics</h2>
          <div className="mt-3 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={55} outerRadius={92} paddingAngle={3}>
                  {chartData.map((item, index) => (
                    <Cell key={item.name} fill={chartColors[index % chartColors.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="surface rounded-lg p-4">
          <h2 className="section-title">Pending work</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <div className="muted-surface rounded-lg p-4">
              <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
                Pending companies
              </p>
              <p className="mt-2 text-3xl font-black">{stats?.pendingCompanies ?? 0}</p>
            </div>
            <div className="muted-surface rounded-lg p-4">
              <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
                Pending drives
              </p>
              <p className="mt-2 text-3xl font-black">{stats?.pendingDrives ?? 0}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-5 xl:grid-cols-2">
        <div className="space-y-3">
          <h2 className="section-title">Company approvals</h2>
          {companies.length ? (
            companies.map((company) => (
              <article className="surface rounded-lg p-4" key={company.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="font-black">{company.companyName}</p>
                    <p className="text-sm" style={{ color: 'var(--muted)' }}>
                      {company.user.email} - {company.location || 'Location not set'}
                    </p>
                  </div>
                  <Badge value={company.approved ? 'APPROVED' : 'PENDING'} />
                </div>
                <div className="mt-4 flex gap-2">
                  <button className="btn btn-primary" type="button" onClick={() => approveCompany(company.id, true)}>
                    Approve
                  </button>
                  <button className="btn btn-danger" type="button" onClick={() => approveCompany(company.id, false)}>
                    Reject
                  </button>
                </div>
              </article>
            ))
          ) : (
            <EmptyState title="No pending companies" />
          )}
        </div>

        <div className="space-y-3">
          <h2 className="section-title">Drive approvals</h2>
          {drives.length ? (
            drives.map((drive) => (
              <article className="surface rounded-lg p-4" key={drive.id}>
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="font-black">{drive.title}</p>
                    <p className="text-sm" style={{ color: 'var(--muted)' }}>
                      {drive.company.companyName} - {money(drive.annualPackage)} - deadline {date(drive.deadline)}
                    </p>
                  </div>
                  <Badge value={drive.status} />
                </div>
                <div className="mt-4 flex gap-2">
                  <button className="btn btn-primary" type="button" onClick={() => updateDrive(drive.id, 'APPROVED')}>
                    Approve
                  </button>
                  <button className="btn btn-danger" type="button" onClick={() => updateDrive(drive.id, 'REJECTED')}>
                    Reject
                  </button>
                </div>
              </article>
            ))
          ) : (
            <EmptyState title="No pending drives" />
          )}
        </div>
      </section>

      <section className="space-y-3">
        <div className="flex flex-col justify-between gap-3 lg:flex-row lg:items-center">
          <h2 className="section-title">User search</h2>
          <form
            className="grid gap-2 sm:grid-cols-[1fr_150px_42px]"
            onSubmit={(event) => {
              event.preventDefault()
              load().catch((err) => setMessage(errorMessage(err)))
            }}
          >
            <input className="field" placeholder="Search name or email" value={query} onChange={(event) => setQuery(event.target.value)} />
            <select className="field" value={role} onChange={(event) => setRole(event.target.value)}>
              <option value="">All roles</option>
              <option value="ADMIN">Admin</option>
              <option value="COMPANY">Company</option>
              <option value="STUDENT">Student</option>
            </select>
            <button className="icon-btn" type="submit" title="Search users">
              <Search size={18} />
            </button>
          </form>
        </div>

        {users.length ? (
          <div className="table-wrap">
            <table className="portal-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>State</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <p className="font-bold">{user.fullName}</p>
                      <p className="text-sm" style={{ color: 'var(--muted)' }}>
                        {user.email}
                      </p>
                    </td>
                    <td>
                      <Badge value={user.role} />
                    </td>
                    <td>{user.blacklisted ? <Badge value="REJECTED" /> : <Badge value="APPROVED" />}</td>
                    <td>
                      <button className={user.blacklisted ? 'btn btn-secondary' : 'btn btn-danger'} type="button" onClick={() => blacklist(user.id, !user.blacklisted)}>
                        <Ban size={16} />
                        {user.blacklisted ? 'Remove blacklist' : 'Blacklist'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState title="No users found" />
        )}
      </section>
    </div>
  )
}
