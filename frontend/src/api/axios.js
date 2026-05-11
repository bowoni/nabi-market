// axios 인스턴스 + 인터셉터 설정
// - 요청 인터셉터: 모든 요청에 Authorization 헤더 자동 첨부
// - 응답 인터셉터: 401 응답 시 refresh token으로 갱신 + 원래 요청 재시도
import axios from 'axios'
import { getAccessToken, getRefreshToken, saveTokens, clearTokens } from '../utils/token'

// 기본 설정을 갖춘 axios 인스턴스 (앞으로 모든 API 호출은 이걸로)
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// === 요청 인터셉터: 모든 요청 직전에 실행 ===
// localStorage의 access token을 Authorization 헤더에 자동 첨부
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// === 응답 인터셉터: 응답을 받은 후 실행 ===
// 401(INVALID_TOKEN) 응답 시 refresh 시도 → 성공하면 원래 요청 재시도
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // 401 + 아직 재시도 안 한 요청 + 인증 API가 아닐 때
    // /api/auth/* 는 login/signup/logout/refresh/oauth 등 인증 흐름 자체라
    // 401 받아도 refresh 시도하면 안 됨 (로그인 실패가 redirect로 이어지는 버그 방지)
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url.startsWith('/api/auth/')
    ) {
      originalRequest._retry = true  // 무한 재시도 방지 플래그

      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          throw new Error('No refresh token')
        }

        // refresh 호출 (인터셉터 적용 안 받게 raw axios 사용)
        const response = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken }
        )

        const { accessToken, refreshToken: newRefreshToken } = response.data
        saveTokens(accessToken, newRefreshToken)

        // 새 토큰으로 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return apiClient(originalRequest)
      } catch (refreshError) {
        // refresh 실패 - 토큰 정리 후 로그인 페이지로
        clearTokens()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
