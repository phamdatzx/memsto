import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useMe } from './useAuth'

function FullPageSpinner() {
  return (
    <div className="grid min-h-dvh place-items-center">
      <div className="size-6 animate-spin rounded-full border-2 border-zinc-300 border-t-zinc-900 dark:border-zinc-700 dark:border-t-zinc-100" />
    </div>
  )
}

export function RequireAuth({ children }: { children: ReactNode }) {
  const me = useMe()
  const location = useLocation()
  if (me.isPending) return <FullPageSpinner />
  if (!me.data) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}

export function GuestOnly({ children }: { children: ReactNode }) {
  const me = useMe()
  if (me.isPending) return <FullPageSpinner />
  if (me.data) return <Navigate to="/" replace />
  return children
}
