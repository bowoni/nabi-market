// 일반 회원가입 페이지 - 폼 입력 + 백엔드 호출 + 성공 시 로그인 페이지로 이동
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { signup, OAUTH_PROVIDERS } from '../api/auth'

// OAuth 회원가입 - LoginPage와 동일 패턴
// 별도 utils로 추출하지 않은 이유: 두 페이지만 사용하고 패턴이 단순해서 약간의 중복 허용
const redirectToOAuth = (url) => {
    window.location.href = url
}

const PROVIDER_STYLES = {
    google: 'bg-white border border-gray-300 text-gray-700 hover:bg-gray-50',
    kakao: 'bg-yellow-300 text-gray-900 hover:bg-yellow-400',
    naver: 'bg-green-500 text-white hover:bg-green-600',
}

function SignupPage() {
    // 백엔드 SignupRequest에 맞춰 필드 구성
    const [form, setForm] = useState({
        loginId: '',
        password: '',
        nickname: '',
        phoneNumber: '',
        email: '',
    })
    const [errorMessage, setErrorMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    const navigate = useNavigate()

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm((prev) => ({ ...prev, [name]: value }))
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            await signup(form)
            // 가입 성공 → 로그인 페이지로 (자동 로그인까지 가려면 AuthContext.login 추가 호출)
            navigate('/login')
        } catch (err) {
            // 백엔드 검증 실패 메시지 또는 중복 에러
            const message = err.response?.data?.message || '회원가입에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow">
            <h1 className="text-2xl font-bold mb-6">회원가입</h1>

            <form onSubmit={handleSubmit} className="space-y-4">
                <Field label="아이디" name="loginId" value={form.loginId}
                       onChange={handleChange} placeholder="4~30자 영문/숫자" />

                <Field label="비밀번호" name="password" type="password"
                       value={form.password}
                       onChange={handleChange} placeholder="8~30자 영문+숫자 포함" />

                <Field label="닉네임" name="nickname" value={form.nickname}
                       onChange={handleChange} placeholder="2~50자" />

                <Field label="전화번호" name="phoneNumber" value={form.phoneNumber}
                       onChange={handleChange} placeholder="010-1234-5678" />

                <Field label="이메일 (선택)" name="email" type="email"
                       value={form.email}
                       onChange={handleChange} required={false}
                       placeholder="example@email.com" />

                {errorMessage && <p className="text-red-600 text-sm">{errorMessage}</p>}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-400"
                >
                    {isSubmitting ? '가입 중...' : '회원가입'}
                </button>
            </form>

            {/* 구분선 - 일반 회원가입과 소셜 회원가입 분리 */}
            <div className="relative my-6">
                <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-gray-300"></div>
                </div>
                <div className="relative flex justify-center text-xs">
                    <span className="bg-white px-2 text-gray-500">또는</span>
                </div>
            </div>

            {/* 소셜 회원가입 버튼들 */}
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
                이미 계정이 있으신가요?{' '}
                <Link to="/login" className="text-primary hover:underline">
                    로그인
                </Link>
            </p>
        </div>
    )
}

// 같은 형태가 반복되어 작은 컴포넌트로 추출 - JSX 가독성 ↑
// (별도 파일로 빼도 좋지만, 이 페이지에서만 쓰는 정도면 같이 두는 것도 OK)
function Field({ label, name, type = 'text', value, onChange, required = true,
                   placeholder }) {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
            <input
                type={type}
                name={name}
                value={value}
                onChange={onChange}
                required={required}
                placeholder={placeholder}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            />
        </div>
    )
}

export default SignupPage