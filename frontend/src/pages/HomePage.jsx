// 홈 페이지 - 로그인 여부에 따라 다른 내용 표시
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

function HomePage() {
    const { user, isAuthenticated, loading } = useAuth()

    // 토큰 검증/사용자 정보 로딩 중 - 깜빡임 방지
    if (loading) {
        return <p className="text-gray-500">로딩 중...</p>
    }

    // 로그인 안 됨 - 안내 + 로그인/회원가입 링크
    if (!isAuthenticated) {
        return (
            <div className="text-center py-12">
                <h1 className="text-3xl font-bold mb-4">나비마켓에 오신 걸
                    환영합니다</h1>
                <p className="text-gray-600 mb-6">동네 중고거래 플랫폼</p>
                <div className="flex justify-center gap-4">
                    <Link to="/login"
                          className="bg-primary text-white px-6 py-2 rounded-md hover:bg-primary-dark">
                        로그인
                    </Link>
                    <Link to="/signup"
                          className="bg-white border border-primary text-primary px-6 py-2 rounded-md hover:bg-amber-50">
                        회원가입
                    </Link>
                </div>
            </div>
        )
    }

    // 로그인됨 - 환영 메시지
    return (
        <div>
            <h1 className="text-3xl font-bold mb-2">
                안녕하세요, {user.nickname}님!
            </h1>
            <p className="text-gray-600">
                나비마켓에 오신 걸 환영합니다.
            </p>

            {/* 디버깅용 사용자 정보 표시 - 추후 마이페이지로 분리 예정 */}
            <div className="mt-6 p-4 bg-white rounded-lg shadow">
                <h2 className="font-semibold mb-2">내 정보</h2>
                <ul className="text-sm text-gray-700 space-y-1">
                    <li>아이디: {user.loginId}</li>
                    <li>가입 수단: {user.provider}</li>
                    <li>이메일: {user.email || '미등록'}</li>
                </ul>
            </div>
        </div>
    )
}

export default HomePage