import { type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { useLogin } from '../auth/useAuth'
import { AuthCard, Field, FormError, SubmitButton } from '../components/form'
import { ApiError } from '../lib/api'

export default function LoginPage() {
  const login = useLogin()
  const navigate = useNavigate()
  const from = (useLocation().state as { from?: string } | null)?.from ?? '/'
  const fieldErrors = login.error instanceof ApiError ? login.error.fieldErrors : {}

  function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    login.mutate(
      { email: String(form.get('email')), password: String(form.get('password')) },
      { onSuccess: () => navigate(from, { replace: true }) },
    )
  }

  return (
    <AuthCard title="Sign in" subtitle="Welcome back. Your memes missed you.">
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <FormError message={login.error?.message} />
        <Field label="Email" name="email" type="email" autoComplete="email" autoFocus required error={fieldErrors.email} />
        <Field label="Password" name="password" type="password" autoComplete="current-password" required error={fieldErrors.password} />
        <SubmitButton pending={login.isPending}>Sign in</SubmitButton>
      </form>
      <p className="mt-5 text-center text-sm text-zinc-500 dark:text-zinc-400">
        No account?{' '}
        <Link to="/register" className="font-medium text-zinc-900 underline dark:text-zinc-100">
          Create one
        </Link>
      </p>
    </AuthCard>
  )
}
