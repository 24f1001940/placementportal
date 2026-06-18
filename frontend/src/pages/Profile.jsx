import { Download, FileUp, Save } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import Badge from '../components/Badge.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import api, { downloadFile, errorMessage } from '../services/api.js'

export default function Profile() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [message, setMessage] = useState('')
  const { register, handleSubmit, reset, formState } = useForm()

  const endpoint = user?.role === 'COMPANY' ? '/companies/me' : '/students/me'

  const load = useCallback(async () => {
    if (user?.role === 'ADMIN') {
      setProfile({ user })
      return
    }
    const { data } = await api.get(endpoint)
    setProfile(data)
    reset(data)
  }, [endpoint, reset, user])

  useEffect(() => {
    load().catch((err) => setMessage(errorMessage(err)))
  }, [load])

  const save = async (values) => {
    setMessage('')
    try {
      const payload =
        user.role === 'COMPANY'
          ? {
              companyName: values.companyName,
              website: values.website,
              location: values.location,
              description: values.description,
            }
          : {
              phone: values.phone,
              college: values.college,
              branch: values.branch,
              graduationYear: values.graduationYear ? Number(values.graduationYear) : null,
              cgpa: values.cgpa ? Number(values.cgpa) : null,
              skills: values.skills,
            }
      const { data } = await api.put(endpoint, payload)
      setProfile(data)
      reset(data)
      setMessage('Profile saved.')
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

  if (user?.role === 'ADMIN') {
    return (
      <div className="space-y-6">
        <div>
          <p className="page-title">Account</p>
          <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            Admin accounts manage portal operations from the dashboard.
          </p>
        </div>
        <div className="surface max-w-2xl rounded-lg p-5">
          <p className="text-sm font-bold" style={{ color: 'var(--muted)' }}>
            Signed in as
          </p>
          <p className="mt-1 text-xl font-black">{profile?.user?.fullName || user.fullName}</p>
          <p className="mt-1" style={{ color: 'var(--muted)' }}>
            {user.email}
          </p>
          <div className="mt-4">
            <Badge value={user.role} />
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="page-title">{user?.role === 'COMPANY' ? 'Company profile' : 'Student profile'}</p>
          <p className="mt-2 text-sm font-semibold" style={{ color: 'var(--muted)' }}>
            Keep profile information ready for placement workflows.
          </p>
        </div>
        {user?.role === 'STUDENT' ? (
          <div className="flex flex-wrap gap-2">
            <label className="btn btn-secondary">
              <FileUp size={18} />
              Upload resume
              <input className="hidden" type="file" accept="application/pdf,.pdf" onChange={uploadResume} />
            </label>
            <button className="btn btn-secondary" type="button" onClick={() => downloadFile('/resumes/me/download', 'resume.pdf')}>
              <Download size={18} />
              Download resume
            </button>
          </div>
        ) : null}
      </div>

      {message ? <p className="muted-surface rounded-lg px-4 py-3 text-sm font-bold">{message}</p> : null}

      <form className="surface max-w-4xl rounded-lg p-5" onSubmit={handleSubmit(save)}>
        {user?.role === 'COMPANY' ? (
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="text-sm font-bold">Company name</span>
              <input className="field mt-1" {...register('companyName', { required: true })} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Website</span>
              <input className="field mt-1" {...register('website')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Location</span>
              <input className="field mt-1" {...register('location')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Approval</span>
              <div className="mt-2">
                <Badge value={profile?.approved ? 'APPROVED' : 'PENDING'} />
              </div>
            </label>
            <label className="block sm:col-span-2">
              <span className="text-sm font-bold">Description</span>
              <textarea className="field mt-1 min-h-32" {...register('description')} />
            </label>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="text-sm font-bold">Phone</span>
              <input className="field mt-1" {...register('phone')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">College</span>
              <input className="field mt-1" {...register('college')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Branch</span>
              <input className="field mt-1" {...register('branch')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Graduation year</span>
              <input className="field mt-1" type="number" {...register('graduationYear')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">CGPA</span>
              <input className="field mt-1" step="0.1" type="number" {...register('cgpa')} />
            </label>
            <label className="block">
              <span className="text-sm font-bold">Resume</span>
              <p className="mt-2 font-bold">{profile?.resume ? profile.resume.originalFileName : 'Not uploaded'}</p>
            </label>
            <label className="block sm:col-span-2">
              <span className="text-sm font-bold">Skills</span>
              <textarea className="field mt-1 min-h-28" {...register('skills')} />
            </label>
          </div>
        )}
        <button className="btn btn-primary mt-5" type="submit" disabled={formState.isSubmitting}>
          <Save size={18} />
          Save profile
        </button>
      </form>
    </div>
  )
}
