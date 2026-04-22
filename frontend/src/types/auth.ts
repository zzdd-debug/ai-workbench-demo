export interface AuthUser {
  userId: string
  username: string
  displayName: string
}

export interface AuthLoginResponse {
  token: string
  user: AuthUser
}

export interface AuthLoginPayload {
  username: string
  password: string
}
