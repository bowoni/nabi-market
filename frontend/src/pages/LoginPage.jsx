// 일반 로그인 페이지 - 아이디/비밀번호 입력 + 백엔드 호출 + 성공 시 홈으로 이동
// useState: 폼 입력값과 에러 메시지를 컴포넌트 상태로 관리
// useNavigate: 로그인 성공 시 페이지 이동시키는 React Router 훅
// useAuth: 우리가 만든 인증 Context - login 함수와 user 상태 접근
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { OAUTH_PROVIDERS } from '../api/auth'

// OAuth 버튼 클릭 시 백엔드 진입 URL로 전체 페이지 이동
// SPA 라우터(navigate)가 아니라 window.location.href를 쓰는 이유:
//   OAuth는 외부 도메인(구글/카카오 등)으로 리다이렉트되는 흐름이라 브라우저 자체가 페이지를 떠나야 함 → React Router로는 불가
const redirectToOAuth = (url) => {
    window.location.href = url
}

// 제공자별 버튼 스타일 - 브랜드 색상 적용
const PROVIDER_STYLES = {
    google: 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50',
    kakao: 'bg-yellow-300 text-gray-900 hover:bg-yellow-400',
    naver: 'bg-green-500 text-white hover:bg-green-600',
}

function LoginPage() {
    // 폼 입력값 - 한 객체로 묶어 관리 (필드 추가 시 setState 코드 줄어듦)
    const [form, setForm] = useState({
        loginId: '',
        password: '',
    })
    const [errorMessage, setErrorMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)  // 제출 중 중복 클릭 방지

    const { login } = useAuth()
    const navigate = useNavigate()

    // input 변경 핸들러 - name 속성으로 어느 필드인지 구분
    const handleChange = (e) => {
        const { name, value } = e.target
        setForm((prev) => ({ ...prev, [name]: value }))
    }

    // 폼 제출 - 로그인 API 호출
    const handleSubmit = async (e) => {
        e.preventDefault()  // 브라우저 기본 form 제출 동작(새로고침) 차단
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            // AuthContext.login - 백엔드 호출 + 토큰 저장 + user 상태 업데이트
            await login(form.loginId, form.password)
            navigate('/')  // 로그인 성공 → 홈으로
        } catch (err) {
            // 백엔드의 ErrorResponse 형식: { code, message }
            const message = err.response?.data?.message || '로그인에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow">
            <h1 className="text-2xl font-bold mb-6">로그인</h1>

            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        아이디
                    </label>
                    <input
                        type="text"
                        name="loginId"
                        value={form.loginId}
                        onChange={handleChange}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        비밀번호
                    </label>
                    <input
                        type="password"
                        name="password"
                        value={form.password}
                        onChange={handleChange}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                    />
                </div>

                {/* 에러 메시지는 있을 때만 렌더 - 조건부 렌더링 */}
                {errorMessage && (
                    <p className="text-red-600 text-sm">{errorMessage}</p>
                )}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                    {isSubmitting ? '로그인 중...' : '로그인'}
                </button>
            </form>

            {/* 구분선 - 일반 로그인과 소셜 로그인 분리 */}
            <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-gray-300"></div>
                </div>
                <div className="relative flex justify-center text-xs">
                    <span className="bg-white px-2 text-gray-500">또는</span>
                </div>
            </div>

            {/* 소셜 로그인 버튼들 - OAUTH_PROVIDERS를 순회해서 자동 렌더링 */}
            <div className="space-y-2">
                {Object.entries(OAUTH_PROVIDERS).map(([key, provider]) => (
                    <button
                        key={key}
                        type="button"
                        onClick={() => redirectToOAuth(provider.url)}
                        className={`w-full py-2 rounded-md font-medium ${PROVIDER_STYLES[key]}`}
                    >
                        {provider.name}로 시작하기
                    </button>
                ))}
            </div>

            <p className="mt-4 text-center text-sm text-gray-600">
                계정이 없으신가요?{' '}
                <Link to="/signup" className="text-primary hover:underline">
                    회원가입
                </Link>
            </p>
        </div>
    )
}

export default LoginPage