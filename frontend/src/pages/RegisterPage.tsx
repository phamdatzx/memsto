import { type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { useRegister } from '../auth/useAuth'
import { AuthCard, Field, FormError, SubmitButton } from '../components/form'
import { ApiError } from '../lib/api'

export default function RegisterPage() {
  const register = useRegister()
  const navigate = useNavigate()
  const fieldErrors = register.error instanceof ApiError ? register.error.fieldErrors : {}

  function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    register.mutate(
      {
        email: String(form.get('email')),
        password: String(form.get('password')),
        displayName: String(form.get('displayName') ?? ''),
      },
      { onSuccess: () => navigate('/', { replace: true }) },
    )
  }

  return (
    <AuthCard title="Create account" subtitle="Start stashing memes, icons and quotes.">
      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <FormError message={register.error?.message} />
        <Field label="Email" name="email" type="email" autoComplete="email" autoFocus required error={fieldErrors.email} />
        <Field label="Display name (optional)" name="displayName" autoComplete="nickname" maxLength={100} error={fieldErrors.displayName} />
        <Field
          label="Password"
          name="password"
          type="password"
          autoComplete="new-password"
          minLength={8}
          required
          placeholder="At least 8 characters"
          error={fieldErrors.password}
        />
        <SubmitButton pending={register.isPending}>Create account</SubmitButton>
      </form>
      <p className="mt-5 text-center text-sm text-zinc-500 dark:text-zinc-400">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-zinc-900 underline dark:text-zinc-100">
          Sign in
        </Link>
      </p>
    </AuthCard>
  )
}
