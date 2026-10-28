import { useLogout, useMe } from '../auth/useAuth'

export default function HomePage() {
  const { data: user } = useMe()
  const logout = useLogout()

  return (
    <div className="min-h-dvh">
      <header className="border-b border-zinc-200 dark:border-zinc-800">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
          <span className="text-lg font-bold tracking-tight">memsto</span>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-zinc-500 dark:text-zinc-400">{user?.displayName ?? user?.email}</span>
            <button
              onClick={() => logout.mutate()}
              disabled={logout.isPending}
              className="rounded-lg border border-zinc-300 px-3 py-1.5 hover:bg-zinc-100 dark:border-zinc-700 dark:hover:bg-zinc-800"
            >
              Sign out
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-10">
        <h1 className="text-xl font-semibold">Your collections</h1>
        <p className="mt-2 text-zinc-500 dark:text-zinc-400">Nothing here yet. Collections are coming next.</p>
      </main>
    </div>
  )
}
