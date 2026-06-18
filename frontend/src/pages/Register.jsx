import { GraduationCap } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { errorMessage } from '../services/api.js'

export default function Register() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState } = useForm({
    defaultValues: { role: 'STUDENT' },
  })

  const submit = async (values) => {
    setError('')
    try {
      await registerUser(values)
      navigate('/', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="app-bg grid min-h-screen place-items-center px-4 py-8">
      <div className="surface w-full max-w-xl rounded-lg p-6 sm:p-8">
        <div className="mb-6 flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-lg bg-blue-600 text-white">
            <GraduationCap size={24} />
          </div>
          <div>
            <p className="page-title">Create account</p>
            <p className="text-sm font-semibold" style={{ color: 'var(--muted)' }}>
              Companies require admin approval before creating drives.
            </p>
          </div>
        </div>

        <form className="space-y-4" onSubmit={handleSubmit(submit)}>
          <label className="block">
            <span className="text-sm font-bold">Full name</span>
            <input className="field mt-1" {...register('fullName', { required: true })} />
          </label>
          <label className="block">
            <span className="text-sm font-bold">Email</span>
            <input className="field mt-1" type="email" {...register('email', { required: true })} />
          </label>
          <label className="block">
            <span className="text-sm font-bold">Password</span>
            <input className="field mt-1" type="password" {...register('password', { required: true, minLength: 6 })} />
          </label>
          <label className="block">
            <span className="text-sm font-bold">Role</span>
            <select className="field mt-1" {...register('role', { required: true })}>
              <option value="STUDENT">Student</option>
              <option value="COMPANY">Company</option>
            </select>
          </label>
          {error ? <p className="rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">{error}</p> : null}
          <button className="btn btn-primary w-full" type="submit" disabled={formState.isSubmitting}>
            Register
          </button>
        </form>

        <p className="mt-5 text-sm" style={{ color: 'var(--muted)' }}>
          Already registered?{' '}
          <Link className="font-black" style={{ color: 'var(--primary)' }} to="/login">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
