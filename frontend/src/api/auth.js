// 인증 관련 백엔드 API 호출 함수들
// 컴포넌트에서 이 함수들을 import해서 사용 → URL/메서드 변경 시 한 곳만 수정
import apiClient from './axios'

// OAuth 제공자별 백엔드 진입 URL
// 사용자가 OAuth 버튼 클릭 시 이 URL로 이동 → Spring Security가 해당 OAuth 서버로 리다이렉트
const OAUTH_BASE_URL = 'http://localhost:8080/oauth2/authorization'

export const OAUTH_PROVIDERS = {
  google: { name: 'Google', url: `${OAUTH_BASE_URL}/google` },
  kakao: { name: 'Kakao', url: `${OAUTH_BASE_URL}/kakao` },
  naver: { name: 'Naver', url: `${OAUTH_BASE_URL}/naver` },
}

// 회원가입 - POST /api/auth/signup
export const signup = (data) => {
  return apiClient.post('/api/auth/signup', data)
}

// 일반 로그인 - POST /api/auth/login
export const login = (loginId, password) => {
  return apiClient.post('/api/auth/login', { loginId, password })
}

// 로그아웃 - POST /api/auth/logout (인증 필요)
export const logout = () => {
  return apiClient.post('/api/auth/logout')
}

// 본인 정보 조회 - GET /api/user/me (인증 필요)
export const getMyInfo = () => {
  return apiClient.get('/api/user/me')
}

// OAuth 폰번호 인증 코드 발송 - POST /api/auth/oauth/phone/send-code
export const sendOAuthVerificationCode = (tempToken, phoneNumber) => {
  return apiClient.post('/api/auth/oauth/phone/send-code', { tempToken, phoneNumber })
}

// OAuth 폰번호 인증 + 가입 완료 - POST /api/auth/oauth/phone/verify
export const verifyOAuthPhone = (tempToken, phoneNumber, code) => {
  return apiClient.post('/api/auth/oauth/phone/verify', { tempToken, phoneNumber, code })
}

// 본인 정보 수정 - PATCH /api/user/me (닉네임/이메일 부분 업데이트)
export const updateMyInfo = (data) => {
  return apiClient.patch('/api/user/me', data)
}

// 비밀번호 변경 - PATCH /api/user/me/password (응답으로 새 TokenResponse)
export const changePassword = (currentPassword, newPassword) => {
  return apiClient.patch('/api/user/me/password', { currentPassword, newPassword })
}

// 회원 탈퇴 - DELETE /api/user/me (LOCAL은 currentPassword 필요, OAuth는 생략)
// axios.delete의 body는 두 번째 인자의 data 옵션으로 전달
export const deleteMyAccount = (currentPassword) => {
  return apiClient.delete('/api/user/me', {
    data: currentPassword ? { currentPassword } : {},
  })
}

// 현재 비밀번호 일치 여부 확인 - POST /api/user/me/password/check-current
// response.data.valid: true면 일치
export const checkCurrentPassword = (password) => {
  return apiClient.post('/api/user/me/password/check-current', { password })
}

// 새 비밀번호가 현재와 다른지 확인 - POST /api/user/me/password/check-new
// response.data.valid: true면 사용 가능(현재와 다름)
export const checkNewPassword = (password) => {
  return apiClient.post('/api/user/me/password/check-new', { password })
}
