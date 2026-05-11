// OAuth 로그인 콜백 - 기존 사용자 케이스
// 백엔드 OAuth2SuccessHandler가 기존 사용자에게는
//   ${frontend}/oauth/success?accessToken=...&refreshToken=...
// 형태로 리다이렉트시킴 → 이 페이지에서 쿼리 토큰을 받아 저장하고 홈으로 이동
import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

function OAuthSuccessPage() {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const { setAuthFromTokens } = useAuth()
    const [errorMessage, setErrorMessage] = useState('')

    // 마운트 시 한 번만 실행 - 토큰 저장 + 사용자 정보 조회 후 홈으로
    useEffect(() => {
        const accessToken = searchParams.get('accessToken')
        const refreshToken = searchParams.get('refreshToken')

        // 토큰이 없으면 잘못된 진입 - 로그인 페이지로
        if (!accessToken || !refreshToken) {
            setErrorMessage('토큰 정보가 없습니다.')
            setTimeout(() => navigate('/login'), 2000)
            return
        }

        // AuthContext에서 토큰 저장 + /api/user/me 조회 + user 상태 업데이트
        setAuthFromTokens(accessToken, refreshToken)
            .then(() => navigate('/'))
            .catch(() => {
                setErrorMessage('사용자 정보 조회에 실패했습니다.')
                setTimeout(() => navigate('/login'), 2000)
            })
        // setAuthFromTokens는 매 렌더마다 새로 생성되지만 마운트 시 한 번만 실행하면 충분
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    return (
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow text-center">
            {errorMessage ? (
                <>
                    <p className="text-red-600">{errorMessage}</p>
                    <p className="text-sm text-gray-500 mt-2">잠시 후 로그인 페이지로 이동합니다...</p>
                </>
            ) : (
                <p className="text-gray-700">로그인 처리 중...</p>
            )}
        </div>
    )
}

export default OAuthSuccessPage
