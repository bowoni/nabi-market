// 인증 상태(로그인한 사용자 정보)를 앱 전체에서 공유하기 위한 Context
// useAuth() 훅으로 어느 컴포넌트에서든 user, login, logout 접근 가능
import { createContext, useContext, useState, useEffect } from 'react'
import { getAccessToken, saveTokens, clearTokens } from '../utils/token'
import * as authApi from '../api/auth'

const AuthContext = createContext(null)

// Provider 컴포넌트 - 앱 최상단에서 감싸면 하위 컴포넌트들이 인증 상태 공유
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)  // 로그인된 사용자 정보 (UserResponse)
  const [loading, setLoading] = useState(true)  // 초기 사용자 정보 로드 중 여부

  // 앱 시작 시 - localStorage에 토큰이 있으면 사용자 정보 자동 조회
  useEffect(() => {
    const token = getAccessToken()
    if (!token) {
      setLoading(false)
      return
    }

    // 토큰 있으면 /api/user/me로 본인 정보 조회 (페이지 새로고침 후에도 로그인 유지)
    authApi.getMyInfo()
      .then((res) => setUser(res.data))
      .catch(() => {
        // 토큰이 무효한 상태 - 정리
        clearTokens()
        setUser(null)
      })
      .finally(() => setLoading(false))
  }, [])

  // 로그인 - 토큰 저장 + 사용자 정보 조회 + 상태 업데이트
  const login = async (loginId, password) => {
    const tokenRes = await authApi.login(loginId, password)
    saveTokens(tokenRes.data.accessToken, tokenRes.data.refreshToken)

    const userRes = await authApi.getMyInfo()
    setUser(userRes.data)
  }

  // 로그아웃 - 백엔드 호출 + 로컬 토큰 정리 + 상태 초기화
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (e) {
      // 로그아웃 실패해도 클라이언트 상태는 무조건 정리
    }
    clearTokens()
    setUser(null)
  }

  // OAuth 로그인 후 토큰을 받았을 때 호출 (OAuth 콜백 페이지에서 사용 예정)
  const setAuthFromTokens = async (accessToken, refreshToken) => {
    saveTokens(accessToken, refreshToken)
    const userRes = await authApi.getMyInfo()
    setUser(userRes.data)
  }

  // 사용자 정보를 다시 가져와서 상태 갱신 - 마이페이지에서 정보 수정 후 호출
  const refreshMyInfo = async () => {
    const res = await authApi.getMyInfo()
    setUser(res.data)
  }

  // 탈퇴 후 정리 - 백엔드 호출 없이 로컬 상태만 정리
  // (logout과 달리 백엔드 /logout 안 부름. 이미 탈퇴 처리된 상태라 불필요)
  const clearAuth = () => {
    clearTokens()
    setUser(null)
  }

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    logout,
    setAuthFromTokens,
    refreshMyInfo,
    clearAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// 커스텀 훅 - 어느 컴포넌트에서든 useAuth()로 인증 상태 접근
export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth는 반드시 AuthProvider 안에서 사용해야 합니다.')
  }
  return context
}
