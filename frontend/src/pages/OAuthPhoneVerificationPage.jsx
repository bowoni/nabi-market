// OAuth 로그인 콜백 - 신규 사용자 케이스 (폰번호 인증으로 가입 완료)
// 백엔드 OAuth2SuccessHandler가 신규 사용자에게는
//   ${frontend}/oauth/phone-verification?tempToken=...
// 로 리다이렉트시킴 → 이 페이지에서 tempToken으로 폰 인증 진행
//
// 흐름:
//   1) 페이지 진입 → tempToken 추출
//   2) 사용자가 폰번호 입력 → /send-code 호출 → 코드 발송
//   3) 사용자가 6자리 코드 입력 → /verify 호출 → 토큰 받음
//   4) 토큰 저장 + 사용자 정보 조회 → 홈으로 이동
import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { sendOAuthVerificationCode, verifyOAuthPhone } from '../api/auth'

// 단계 상수 - 매직 스트링 방지
const STEP = {
    PHONE: 'phone',   // 폰번호 입력 + 코드 발송 단계
    CODE: 'code',     // 인증 코드 입력 + 검증 단계
}

function OAuthPhoneVerificationPage() {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const { setAuthFromTokens } = useAuth()

    const tempToken = searchParams.get('tempToken')

    const [step, setStep] = useState(STEP.PHONE)
    const [phoneNumber, setPhoneNumber] = useState('')
    const [code, setCode] = useState('')
    const [errorMessage, setErrorMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    // tempToken 없이 진입한 경우 - 잘못된 접근
    useEffect(() => {
        if (!tempToken) {
            setErrorMessage('잘못된 접근입니다. 다시 로그인해주세요.')
            setTimeout(() => navigate('/login'), 2000)
        }
    }, [tempToken, navigate])

    // 1단계: 폰번호 제출 → 인증 코드 발송 요청
    const handleSendCode = async (e) => {
        e.preventDefault()
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            await sendOAuthVerificationCode(tempToken, phoneNumber)
            setStep(STEP.CODE)
        } catch (err) {
            const message = err.response?.data?.message || '인증 코드 발송에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    // 2단계: 인증 코드 검증 → 토큰 받아서 로그인 완료
    const handleVerify = async (e) => {
        e.preventDefault()
        setErrorMessage('')
        setIsSubmitting(true)

        try {
            const res = await verifyOAuthPhone(tempToken, phoneNumber, code)
            const { accessToken, refreshToken } = res.data
            // 토큰 저장 + /api/user/me 조회 + user 상태 업데이트
            await setAuthFromTokens(accessToken, refreshToken)
            navigate('/')
        } catch (err) {
            const message = err.response?.data?.message || '인증에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    if (!tempToken) {
        return (
            <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow text-center">
                <p className="text-red-600">{errorMessage}</p>
            </div>
        )
    }

    return (
        <div className="max-w-md mx-auto bg-white p-8 rounded-lg shadow">
            <h1 className="text-2xl font-bold mb-2">전화번호 인증</h1>
            <p className="text-sm text-gray-600 mb-6">
                나비마켓은 1번호 1계정 정책으로 운영됩니다.
            </p>

            {step === STEP.PHONE ? (
                <PhoneStep
                    phoneNumber={phoneNumber}
                    setPhoneNumber={setPhoneNumber}
                    onSubmit={handleSendCode}
                    isSubmitting={isSubmitting}
                />
            ) : (
                <CodeStep
                    code={code}
                    setCode={setCode}
                    phoneNumber={phoneNumber}
                    onSubmit={handleVerify}
                    onBack={() => { setStep(STEP.PHONE); setCode(''); setErrorMessage('') }}
                    isSubmitting={isSubmitting}
                />
            )}

            {errorMessage && (
                <p className="text-red-600 text-sm mt-4">{errorMessage}</p>
            )}
        </div>
    )
}

// 폰번호 입력 단계 - 별도 컴포넌트로 분리하면 JSX 가독성↑
function PhoneStep({ phoneNumber, setPhoneNumber, onSubmit, isSubmitting }) {
    return (
        <form onSubmit={onSubmit} className="space-y-4">
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    전화번호
                </label>
                <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    required
                    placeholder="010-1234-5678"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                />
            </div>
            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-400"
            >
                {isSubmitting ? '발송 중...' : '인증 코드 발송'}
            </button>
        </form>
    )
}

// 인증 코드 입력 단계
function CodeStep({ code, setCode, phoneNumber, onSubmit, onBack, isSubmitting }) {
    return (
        <form onSubmit={onSubmit} className="space-y-4">
            <p className="text-sm text-gray-700">
                <span className="font-medium">{phoneNumber}</span>로 인증 코드를 발송했습니다.
            </p>
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                    인증 코드 (6자리)
                </label>
                <input
                    type="text"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    required
                    maxLength={6}
                    placeholder="123456"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                />
            </div>
            <button
                type="submit"
                disabled={isSubmitting}
                className="w-full bg-primary text-white py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-400"
            >
                {isSubmitting ? '인증 중...' : '인증 완료'}
            </button>
            <button
                type="button"
                onClick={onBack}
                className="w-full text-sm text-gray-600 hover:text-primary"
            >
                전화번호 다시 입력
            </button>
        </form>
    )
}

export default OAuthPhoneVerificationPage
