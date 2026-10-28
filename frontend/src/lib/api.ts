export class ApiError extends Error {
  status: number
  fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']

function readCookie(name: string): string | undefined {
  return document.cookie
    .split('; ')
    .find((c) => c.startsWith(`${name}=`))
    ?.slice(name.length + 1)
}

let csrfPrimed: Promise<void> | undefined

/** The backend sets XSRF-TOKEN lazily; fetch it once before the first unsafe request. */
async function csrfToken(): Promise<string | undefined> {
  if (!readCookie('XSRF-TOKEN')) {
    csrfPrimed ??= fetch('/api/auth/csrf', { credentials: 'include' }).then(() => undefined)
    await csrfPrimed
    csrfPrimed = undefined
  }
  const token = readCookie('XSRF-TOKEN')
  return token && decodeURIComponent(token)
}

export async function api<T>(path: string, init: RequestInit & { json?: unknown } = {}): Promise<T> {
  const { json, ...rest } = init
  const method = (rest.method ?? (json === undefined ? 'GET' : 'POST')).toUpperCase()
  const headers = new Headers(rest.headers)
  if (json !== undefined) headers.set('Content-Type', 'application/json')
  if (!SAFE_METHODS.includes(method)) {
    const token = await csrfToken()
    if (token) headers.set('X-XSRF-TOKEN', token)
  }

  const res = await fetch(`/api${path}`, {
    credentials: 'include',
    ...rest,
    method,
    headers,
    body: json === undefined ? rest.body : JSON.stringify(json),
  })

  if (!res.ok) {
    const problem = await res.json().catch(() => ({}))
    throw new ApiError(res.status, problem.detail ?? `${res.status} ${res.statusText}`, problem.errors)
  }
  if (res.status === 204 || res.headers.get('Content-Length') === '0') return undefined as T
  return res.json() as Promise<T>
}
