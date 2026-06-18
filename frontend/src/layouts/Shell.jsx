import {
  Bell,
  BriefcaseBusiness,
  ClipboardList,
  GraduationCap,
  LayoutDashboard,
  LogOut,
  Moon,
  ShieldCheck,
  Sun,
  UserRound,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import api from '../services/api.js'
import Badge from '../components/Badge.jsx'

export default function Shell() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [dark, setDark] = useState(() => localStorage.getItem('placement_theme') === 'dark')
  const [notifications, setNotifications] = useState([])
  const [open, setOpen] = useState(false)

  useEffect(() => {
    document.documentElement.classList.toggle('dark', dark)
    localStorage.setItem('placement_theme', dark ? 'dark' : 'light')
  }, [dark])

  useEffect(() => {
    api.get('/notifications/me').then(({ data }) => setNotifications(data)).catch(() => setNotifications([]))
  }, [])

  const navItems = useMemo(() => {
    const base = [{ to: '/', label: 'Dashboard', icon: LayoutDashboard }]
    if (user?.role === 'STUDENT') {
      return [...base, { to: '/applications', label: 'Applications', icon: ClipboardList }, { to: '/profile', label: 'Profile', icon: UserRound }]
    }
    if (user?.role === 'COMPANY') {
      return [...base, { to: '/applications', label: 'Applicants', icon: ClipboardList }, { to: '/profile', label: 'Company', icon: BriefcaseBusiness }]
    }
    return [...base, { to: '/profile', label: 'Account', icon: ShieldCheck }]
  }, [user?.role])

  const unread = notifications.filter((item) => !item.read).length

  const signOut = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-bg min-h-screen">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-64 border-r px-4 py-5 lg:block" style={{ borderColor: 'var(--border)', background: 'var(--surface)' }}>
        <div className="mb-7 flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-lg bg-blue-600 text-white">
            <GraduationCap size={22} />
          </div>
          <div>
            <p className="font-black leading-tight">Placement Portal</p>
            <p className="text-xs font-semibold" style={{ color: 'var(--muted)' }}>
              2026 Full Stack
            </p>
          </div>
        </div>

        <nav className="space-y-2">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-bold ${isActive ? 'bg-blue-600 text-white' : 'hover:bg-slate-100'}`
              }
            >
              <item.icon size={18} />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="lg:pl-64">
        <header className="sticky top-0 z-10 border-b backdrop-blur" style={{ borderColor: 'var(--border)', background: 'color-mix(in srgb, var(--surface) 92%, transparent)' }}>
          <div className="flex min-h-16 items-center justify-between gap-3 px-4 sm:px-6">
            <div className="min-w-0">
              <p className="truncate text-sm font-bold">{user?.fullName}</p>
              <div className="mt-1 flex items-center gap-2">
                <Badge value={user?.role} />
                {user?.blacklisted ? <Badge value="REJECTED" /> : null}
              </div>
            </div>

            <div className="flex items-center gap-2">
              <div className="relative">
                <button className="icon-btn" type="button" title="Notifications" onClick={() => setOpen((value) => !value)}>
                  <Bell size={18} />
                  {unread ? <span className="absolute -right-1 -top-1 h-4 min-w-4 rounded-full bg-red-600 px-1 text-[10px] font-black text-white">{unread}</span> : null}
                </button>
                {open ? (
                  <div className="surface absolute right-0 mt-2 w-80 rounded-lg p-3">
                    <p className="mb-2 text-sm font-black">Notifications</p>
                    <div className="max-h-80 space-y-2 overflow-auto">
                      {notifications.length ? (
                        notifications.slice(0, 8).map((item) => (
                          <div key={item.id} className="rounded-lg border p-3 text-sm" style={{ borderColor: 'var(--border)' }}>
                            <p className="font-bold">{item.title}</p>
                            <p className="mt-1" style={{ color: 'var(--muted)' }}>
                              {item.message}
                            </p>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm" style={{ color: 'var(--muted)' }}>
                          No notifications yet.
                        </p>
                      )}
                    </div>
                  </div>
                ) : null}
              </div>
              <button className="icon-btn" type="button" title="Toggle dark mode" onClick={() => setDark((value) => !value)}>
                {dark ? <Sun size={18} /> : <Moon size={18} />}
              </button>
              <button className="icon-btn" type="button" title="Sign out" onClick={signOut}>
                <LogOut size={18} />
              </button>
            </div>
          </div>
        </header>

        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
