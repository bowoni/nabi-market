// JWT 토큰을 localStorage에 저장/조회/삭제하는 헬퍼 함수들
// TODO: localStorage는 XSS 공격에 취약. 추후 HttpOnly Secure 쿠키로 변경 - 브라우저 history/JS 접근으로부터 토큰 보호
const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

// 토큰 저장 (로그인/회원가입/리프레시 성공 시 호출)
export const saveTokens = (accessToken, refreshToken) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

// Access Token 조회 (axios 요청 인터셉터에서 헤더에 자동 첨부할 때 사용)
export const getAccessToken = () => {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

// Refresh Token 조회 (Access Token 만료 시 갱신 요청에 사용)
export const getRefreshToken = () => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

// 모든 토큰 삭제 (로그아웃 시 호출)
export const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}
