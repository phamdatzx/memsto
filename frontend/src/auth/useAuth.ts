import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ApiError } from '../lib/api'

export type User = { id: number; email: string; displayName: string | null }
export type LoginInput = { email: string; password: string }
export type RegisterInput = LoginInput & { displayName?: string }

const ME = ['auth', 'me'] as const

/** Current user, or null when signed out. */
export function useMe() {
  return useQuery({
    queryKey: ME,
    queryFn: async () => {
      try {
        return await api<User>('/auth/me')
      } catch (e) {
        if (e instanceof ApiError && e.status === 401) return null
        throw e
      }
    },
    staleTime: Infinity,
    retry: false,
  })
}

export function useLogin() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: LoginInput) => api<User>('/auth/login', { json: input }),
    onSuccess: (user) => qc.setQueryData(ME, user),
  })
}

export function useRegister() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: RegisterInput) => api<User>('/auth/register', { json: input }),
    onSuccess: (user) => qc.setQueryData(ME, user),
  })
}

export function useLogout() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: () => api<void>('/auth/logout', { method: 'POST' }),
    onSuccess: () => {
      qc.clear()
      qc.setQueryData(ME, null)
    },
  })
}
