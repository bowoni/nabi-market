// 마이페이지 - 기본 정보 수정 + 비밀번호 변경(LOCAL만) + 회원 탈퇴
// 한 파일에 3개 섹션을 내부 컴포넌트로 분리 - 흐름 한눈에 보이도록
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import {
    updateMyInfo,
    changePassword,
    deleteMyAccount,
    checkCurrentPassword,
    checkNewPassword,
} from '../api/auth'
import { useDebounce } from '../hooks/useDebounce'

// 가입 수단 상수 - 비밀번호 변경 노출 조건 판단에 사용
const PROVIDER_LOCAL = 'LOCAL'

// 비밀번호 정책 - 백엔드 SignupRequest의 정규식과 동일하게 유지
// TODO: 백엔드와 정규식 이중 관리 중. 추후 정책 상수 동기화 필요
const PASSWORD_POLICY = /^(?=.*[A-Za-z])(?=.*\d).{8,30}$/

// 디바운스 딜레이 - 사용자 타이핑이 멈춘 후 검증 API 호출까지 대기 시간
const DEBOUNCE_DELAY_MS = 500

function MyPage() {
    const { user, loading, isAuthenticated, refreshMyInfo, setAuthFromTokens, clearAuth } = useAuth()
    const navigate = useNavigate()

    // 인증 보호 - 비로그인 상태로 직접 진입 시 로그인 페이지로
    // (별도 ProtectedRoute 컴포넌트를 안 만들고 페이지에서 가드)
    useEffect(() => {
        if (!loading && !isAuthenticated) {
            navigate('/login')
        }
    }, [loading, isAuthenticated, navigate])

    if (loading || !user) {
        return <p className="text-gray-500">로딩 중...</p>
    }

    // LOCAL 가입자만 비밀번호 변경 섹션 노출 - OAuth는 password가 null이라 변경 불가
    const isLocalUser = user.provider === PROVIDER_LOCAL

    // 탈퇴 완료 후 처리 - 토큰/상태 정리 + 홈으로 이동
    const handleDeleted = () => {
        clearAuth()
        navigate('/')
    }

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <h1 className="text-2xl font-bold">마이페이지</h1>

            <BasicInfoSection user={user} onUpdated={refreshMyInfo} />

            {isLocalUser && <PasswordSection onChanged={setAuthFromTokens} />}

            <DeleteAccountSection isLocalUser={isLocalUser} onDeleted={handleDeleted} />
        </div>
    )
}

// ───────────────────────────────────────────────────────
// 기본 정보 수정 섹션 (닉네임/이메일)
// ───────────────────────────────────────────────────────
function BasicInfoSection({ user, onUpdated }) {
    // 초기값을 별도 저장해두고 현재 form 값과 비교해 dirty 체크
    const initialForm = {
        nickname: user.nickname ?? '',
        email: user.email ?? '',
    }

    const [form, setForm] = useState(initialForm)
    const [errorMessage, setErrorMessage] = useState('')
    const [successMessage, setSuccessMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    // 변경 사항이 있을 때만 저장 버튼 활성화 - "본인 현재 값 그대로 보내기" 차단
    const isDirty =
        form.nickname !== initialForm.nickname || form.email !== initialForm.email

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm((prev) => ({ ...prev, [name]: value }))
        setSuccessMessage('')  // 다시 수정 시작하면 이전 성공 메시지 사라지게
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setErrorMessage('')
        setSuccessMessage('')
        setIsSubmitting(true)

        try {
            // 변경된 필드만 PATCH 페이로드에 포함 - 의도 명확하게
            const payload = {}
            if (form.nickname !== initialForm.nickname) payload.nickname = form.nickname
            if (form.email !== initialForm.email) payload.email = form.email

            await updateMyInfo(payload)
            await onUpdated()  // AuthContext의 user 상태 갱신 (initialForm도 다음 렌더에 재계산)
            setSuccessMessage('변경되었습니다.')
        } catch (err) {
            const message = err.response?.data?.message || '변경에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <section className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-lg font-semibold mb-4">기본 정보</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
                {/* 읽기 전용 필드 - 수정 불가하지만 정보 노출 */}
                <ReadOnlyField label="아이디" value={user.loginId} />
                <ReadOnlyField label="가입 수단" value={user.provider} />
                <ReadOnlyField
                    label="전화번호"
                    value={user.phoneNumber}
                    hint="전화번호 변경은 추후 별도 인증을 통해 지원 예정입니다."
                />

                {/* 수정 가능 필드 */}
                <EditableField
                    label="닉네임" name="nickname" value={form.nickname}
                    onChange={handleChange} required maxLength={50}
                />
                <EditableField
                    label="이메일" name="email" type="email" value={form.email}
                    onChange={handleChange} placeholder="example@email.com"
                />

                {errorMessage && <p className="text-red-600 text-sm">{errorMessage}</p>}
                {successMessage && <p className="text-green-600 text-sm">{successMessage}</p>}

                <button
                    type="submit"
                    disabled={!isDirty || isSubmitting}
                    className="bg-primary text-white px-4 py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                    {isSubmitting ? '저장 중...' : '저장'}
                </button>
            </form>
        </section>
    )
}

// ───────────────────────────────────────────────────────
// 비밀번호 변경 섹션 (LOCAL만 노출)
// 4종 검증을 실시간 표시:
//   1) 현재 비밀번호 일치 - 백엔드 API, 디바운스
//   2) 새 비밀번호 정책 - 프론트 정규식, 실시간
//   3) 새 비밀번호 != 현재 - 백엔드 API, 디바운스
//   4) 새 비밀번호 == 확인 - 프론트 비교, 실시간
// 모든 검증 통과 시에만 "비밀번호 변경" 버튼 활성화
// ───────────────────────────────────────────────────────
function PasswordSection({ onChanged }) {
    const [form, setForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: '',
    })
    const [errorMessage, setErrorMessage] = useState('')
    const [successMessage, setSuccessMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    // 비동기 검증 상태: null = 미검증/입력 전, 'checking' = API 호출 중, true/false = 결과
    const [currentValid, setCurrentValid] = useState(null)
    const [newDifferentValid, setNewDifferentValid] = useState(null)

    // 사용자가 입력을 멈춘 후 500ms 지나야 debounced 값이 갱신됨 → API 호출은 그때부터
    const debouncedCurrent = useDebounce(form.currentPassword, DEBOUNCE_DELAY_MS)
    const debouncedNew = useDebounce(form.newPassword, DEBOUNCE_DELAY_MS)

    // 정책 검증 (실시간, 정규식)
    const policyValid = PASSWORD_POLICY.test(form.newPassword)

    // 새 비밀번호 == 확인 비밀번호 (실시간) - 확인 칸 비어있으면 null
    const confirmValid = form.confirmNewPassword
        ? form.newPassword === form.confirmNewPassword
        : null

    // 현재 비밀번호 비동기 검증
    useEffect(() => {
        if (!debouncedCurrent) {
            setCurrentValid(null)
            return
        }

        // cancelled 플래그 - effect 재실행 시 이전 응답을 무시하기 위함 (race condition 방지)
        // 예: 사용자가 빠르게 입력 → effect 두 번 실행 → 첫 응답이 늦게 와서 두번째 상태를 덮어쓰는 일 방지
        let cancelled = false
        setCurrentValid('checking')

        checkCurrentPassword(debouncedCurrent)
            .then((res) => {
                if (!cancelled) setCurrentValid(res.data.valid)
            })
            .catch(() => {
                if (!cancelled) setCurrentValid(false)
            })

        return () => { cancelled = true }
    }, [debouncedCurrent])

    // 새 비밀번호 비동기 검증 - 정책 통과 시에만 API 호출 (불필요한 호출 차단)
    useEffect(() => {
        if (!debouncedNew || !PASSWORD_POLICY.test(debouncedNew)) {
            setNewDifferentValid(null)
            return
        }

        let cancelled = false
        setNewDifferentValid('checking')

        checkNewPassword(debouncedNew)
            .then((res) => {
                if (!cancelled) setNewDifferentValid(res.data.valid)
            })
            .catch(() => {
                if (!cancelled) setNewDifferentValid(false)
            })

        return () => { cancelled = true }
    }, [debouncedNew])

    // 4종 검증 모두 통과해야 제출 가능
    const canSubmit =
        currentValid === true &&
        policyValid &&
        newDifferentValid === true &&
        confirmValid === true

    const handleChange = (e) => {
        const { name, value } = e.target
        setForm((prev) => ({ ...prev, [name]: value }))
        setSuccessMessage('')
        // 입력 변경 즉시 비동기 검증 상태 초기화
        // (디바운스 동안 이전 ✓ 표시가 유지되면 사용자가 헷갈리니까)
        if (name === 'currentPassword') setCurrentValid(null)
        if (name === 'newPassword') setNewDifferentValid(null)
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!canSubmit) return

        setErrorMessage('')
        setSuccessMessage('')
        setIsSubmitting(true)
        try {
            const res = await changePassword(form.currentPassword, form.newPassword)
            // 응답으로 새 Access/Refresh 토큰 - AuthContext에 반영해서 이후 요청도 새 토큰 사용
            await onChanged(res.data.accessToken, res.data.refreshToken)
            setSuccessMessage('비밀번호가 변경되었습니다.')
            setForm({ currentPassword: '', newPassword: '', confirmNewPassword: '' })
            setCurrentValid(null)
            setNewDifferentValid(null)
        } catch (err) {
            const message = err.response?.data?.message || '비밀번호 변경에 실패했습니다.'
            setErrorMessage(message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <section className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-lg font-semibold mb-4">비밀번호 변경</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
                <FieldWithStatus
                    label="현재 비밀번호" name="currentPassword" type="password"
                    value={form.currentPassword} onChange={handleChange} required
                    status={currentValid}
                    successText="현재 비밀번호가 일치합니다."
                    failureText="현재 비밀번호가 일치하지 않습니다."
                />

                <FieldWithStatus
                    label="새 비밀번호" name="newPassword" type="password"
                    value={form.newPassword} onChange={handleChange} required
                    placeholder="8~30자 영문+숫자 포함"
                    status={resolveNewPasswordStatus(form.newPassword, policyValid, newDifferentValid)}
                    successText="사용 가능한 비밀번호입니다."
                    failureText={resolveNewPasswordFailureText(policyValid, newDifferentValid)}
                />

                <FieldWithStatus
                    label="새 비밀번호 확인" name="confirmNewPassword" type="password"
                    value={form.confirmNewPassword} onChange={handleChange} required
                    status={confirmValid}
                    successText="새 비밀번호와 일치합니다."
                    failureText="새 비밀번호와 일치하지 않습니다."
                />

                {errorMessage && <p className="text-red-600 text-sm">{errorMessage}</p>}
                {successMessage && <p className="text-green-600 text-sm">{successMessage}</p>}

                <button
                    type="submit"
                    disabled={!canSubmit || isSubmitting}
                    className="bg-primary text-white px-4 py-2 rounded-md font-medium hover:bg-primary-dark disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                    {isSubmitting ? '변경 중...' : '비밀번호 변경'}
                </button>
            </form>
        </section>
    )
}

// 새 비밀번호 필드의 종합 status를 계산
// - 비어있음 → null
// - 정책 위반 → false (즉시 실패, API 호출 안 함)
// - 정책 통과 + API 검증 진행/결과 반영
function resolveNewPasswordStatus(newPassword, policyValid, differentValid) {
    if (!newPassword) return null
    if (!policyValid) return false
    return differentValid  // 'checking' / true / false / null 그대로 전달
}

function resolveNewPasswordFailureText(policyValid, differentValid) {
    if (!policyValid) return '8~30자 영문+숫자 포함이어야 합니다.'
    if (differentValid === false) return '현재 비밀번호와 동일합니다.'
    return ''
}

// ───────────────────────────────────────────────────────
// 회원 탈퇴 섹션 - 버튼 클릭 → 모달 다이얼로그
// ───────────────────────────────────────────────────────
function DeleteAccountSection({ isLocalUser, onDeleted }) {
    const [isModalOpen, setIsModalOpen] = useState(false)

    return (
        <section className="bg-white p-6 rounded-lg shadow border border-red-200">
            <h2 className="text-lg font-semibold mb-2 text-red-600">회원 탈퇴</h2>
            <p className="text-sm text-gray-600 mb-4">
                탈퇴 시 계정 정보는 영구히 비활성화됩니다. 동일 닉네임/전화번호로 재가입할 수 없습니다.
            </p>
            <button
                onClick={() => setIsModalOpen(true)}
                className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700"
            >
                탈퇴하기
            </button>

            {isModalOpen && (
                <DeleteAccountModal
                    isLocalUser={isLocalUser}
                    onClose={() => setIsModalOpen(false)}
                    onConfirmed={onDeleted}
                />
            )}
        </section>
    )
}

// 탈퇴 확인 모달 - 다이얼로그 + LOCAL은 비밀번호 입력
function DeleteAccountModal({ isLocalUser, onClose, onConfirmed }) {
    const [password, setPassword] = useState('')
    const [errorMessage, setErrorMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    const handleConfirm = async () => {
        setErrorMessage('')
        setIsSubmitting(true)
        try {
            // LOCAL이면 비밀번호 전달, OAuth면 undefined → API 함수에서 빈 body 전송
            await deleteMyAccount(isLocalUser ? password : undefined)
            onConfirmed()
        } catch (err) {
            const message = err.response?.data?.message || '탈퇴에 실패했습니다.'
            setErrorMessage(message)
            setIsSubmitting(false)
        }
    }

    return (
        // 백드롭 - 클릭하면 모달 닫힘
        <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={onClose}
        >
            {/* 모달 본문 - 백드롭 클릭 이벤트가 모달 본문까지 전파되지 않도록 차단 */}
            <div
                className="bg-white p-6 rounded-lg shadow-lg max-w-md w-full mx-4"
                onClick={(e) => e.stopPropagation()}
            >
                <h3 className="text-lg font-semibold mb-2">정말 탈퇴하시겠습니까?</h3>
                <p className="text-sm text-gray-600 mb-4">
                    이 작업은 복구할 수 없습니다.
                </p>

                {/* LOCAL 가입자만 비밀번호 입력 칸 노출 */}
                {isLocalUser && (
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            현재 비밀번호 확인
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호 입력"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
                        />
                    </div>
                )}

                {errorMessage && <p className="text-red-600 text-sm mb-3">{errorMessage}</p>}

                <div className="flex justify-end gap-2">
                    <button
                        onClick={onClose}
                        disabled={isSubmitting}
                        className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
                    >
                        취소
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={isSubmitting || (isLocalUser && !password)}
                        className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 disabled:bg-gray-300"
                    >
                        {isSubmitting ? '탈퇴 중...' : '탈퇴'}
                    </button>
                </div>
            </div>
        </div>
    )
}

// ───────────────────────────────────────────────────────
// 작은 헬퍼 컴포넌트들 - 반복되는 input 모양 추출
// ───────────────────────────────────────────────────────
function ReadOnlyField({ label, value, hint }) {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
            <input
                value={value || ''} disabled
                className="w-full px-3 py-2 border border-gray-200 rounded-md bg-gray-50 text-gray-500"
            />
            {hint && <p className="text-xs text-gray-500 mt-1">{hint}</p>}
        </div>
    )
}

function EditableField({ label, ...rest }) {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
            <input
                {...rest}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            />
        </div>
    )
}

// 검증 status를 같이 표시하는 input 래퍼
// status 값: null = 표시 안 함, 'checking' = 검증 중, true = 성공, false = 실패
function FieldWithStatus({ label, status, successText, failureText, ...inputProps }) {
    return (
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
            <input
                {...inputProps}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            />
            <StatusMessage status={status} successText={successText} failureText={failureText} />
        </div>
    )
}

function StatusMessage({ status, successText, failureText }) {
    if (status === null || status === undefined) return null
    if (status === 'checking') {
        return <p className="text-xs text-gray-500 mt-1">확인 중...</p>
    }
    if (status === true) {
        return <p className="text-xs text-green-600 mt-1">✓ {successText}</p>
    }
    return <p className="text-xs text-red-600 mt-1">✗ {failureText}</p>
}

export default MyPage
