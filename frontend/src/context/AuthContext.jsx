import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import api from '../services/api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('placement_token'))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('placement_user')
    return raw ? JSON.parse(raw) : null
  })
  const [loading, setLoading] = useState(Boolean(token))

  useEffect(() => {
    const clear = () => {
      localStorage.removeItem('placement_token')
      localStorage.removeItem('placement_user')
      setToken(null)
      setUser(null)
    }
    window.addEventListener('placement:unauthorized', clear)
    return () => window.removeEventListener('placement:unauthorized', clear)
  }, [])

  useEffect(() => {
    if (!token) {
      setLoading(false)
      return
    }
    api
      .get('/auth/me')
      .then(({ data }) => {
        setUser(data.user)
        localStorage.setItem('placement_user', JSON.stringify(data.user))
      })
      .finally(() => setLoading(false))
  }, [token])

  const saveSession = (data) => {
    localStorage.setItem('placement_token', data.token)
    localStorage.setItem('placement_user', JSON.stringify(data.user))
    setToken(data.token)
    setUser(data.user)
  }

  const value = useMemo(
    () => ({
      token,
      user,
      loading,
      login: async (payload) => {
        const { data } = await api.post('/auth/login', payload)
        saveSession(data)
        return data.user
      },
      register: async (payload) => {
        const { data } = await api.post('/auth/register', payload)
        saveSession(data)
        return data.user
      },
      logout: () => {
        localStorage.removeItem('placement_token')
        localStorage.removeItem('placement_user')
        setToken(null)
        setUser(null)
      },
    }),
    [loading, token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}
