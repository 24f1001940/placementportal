import { BriefcaseBusiness, GraduationCap, ShieldCheck } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { errorMessage } from '../services/api.js'

const demos = [
  ['Admin', 'admin@placement.local', 'Admin@123', ShieldCheck],
  ['Company', 'hr@novacore.local', 'Company@123', BriefcaseBusiness],
  ['Student', 'student@placement.local', 'Student@123', GraduationCap],
]

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  const { register, handleSubmit, setValue, formState } = useForm({
    defaultValues: { email: 'admin@placement.local', password: 'Admin@123' },
  })

  const submit = async (values) => {
    setError('')
    try {
      await login(values)
      navigate(location.state?.from?.pathname || '/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="app-bg grid min-h-screen place-items-center px-4 py-8">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-lg border lg:grid-cols-[0.95fr_1.05fr]" style={{ borderColor: 'var(--border)', background: 'var(--surface)' }}>
        <section className="hidden border-r p-8 lg:block" style={{ borderColor: 'var(--border)' }}>
          <div className="mb-12 flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-lg bg-blue-600 text-white">
              <GraduationCap size={24} />
            </div>
            <div>
              <p className="text-xl font-black">Placement Portal</p>
              <p className="text-sm font-semibold" style={{ color: 'var(--muted)' }}>
                Admin, company, and student workspace
              </p>
            </div>
          </div>
          <div className="space-y-4">
            {demos.map(([label, email, , Icon]) => (
              <div className="muted-surface rounded-lg p-4" key={email}>
                <div className="flex items-center gap-3">
                  <Icon size={22} style={{ color: 'var(--primary)' }} />
                  <div>
                    <p className="font-black">{label} module</p>
                    <p className="text-sm" style={{ color: 'var(--muted)' }}>
                      {email}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="p-6 sm:p-10">
          <p className="page-title">Sign in</p>
          <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            Use a seeded demo account or your registered user.
          </p>

          <div className="mt-6 grid gap-2 sm:grid-cols-3">
            {demos.map(([label, email, password, Icon]) => (
              <button
                className="btn btn-secondary justify-start"
                type="button"
                key={email}
                onClick={() => {
                  setValue('email', email)
                  setValue('password', password)
                }}
              >
                <Icon size={17} />
                {label}
              </button>
            ))}
          </div>

          <form className="mt-7 space-y-4" onSubmit={handleSubmit(submit)}>
            <label className="block">
              <span className="text-sm font-bold">Email</span>
              <input className="field mt-1" type="email" {...register('email', { required: true })} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Password</span>
              <input className="field mt-1" type="password" {...register('password', { required: true })} />
            </label>
            {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">{error}</p> : null}
            <button className="btn btn-primary w-full" type="submit" disabled={formState.isSubmitting}>
              Sign in
            </button>
          </form>

          <p className="mt-5 text-sm" style={{ color: 'var(--muted)' }}>
            Need an account?{' '}
            <Link className="font-black" style={{ color: 'var(--primary)' }} to="/register">
              Register here
            </Link>
          </p>
        </section>
      </div>
    </div>
  )
}
