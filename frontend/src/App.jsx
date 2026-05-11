import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import { useAuth } from './contexts/AuthContext'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import OAuthSuccessPage from './pages/OAuthSuccessPage'
import OAuthPhoneVerificationPage from './pages/OAuthPhoneVerificationPage'
import MyPage from './pages/MyPage'

function App() {
    return (
        <div className="min-h-screen bg-gray-50">
            <Navigation />
            <main className="max-w-4xl mx-auto p-6">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    {/* OAuth 콜백 - 백엔드 OAuth2SuccessHandler가 리다이렉트해주는 경로 */}
                    <Route path="/oauth/success" element={<OAuthSuccessPage />} />
                    <Route path="/oauth/phone-verification" element={<OAuthPhoneVerificationPage />} />
                    <Route path="/mypage" element={<MyPage />} />
                </Routes>
            </main>
        </div>
    )
}

// 네비게이션 - 로그인 여부에 따라 다른 메뉴 표시
// 별도 함수로 분리한 이유: useAuth 훅을 쓰려면 AuthProvider 안의 컴포넌트여야 하는데,
// App 자체에서 쓰는 것보다 분리하는 게 관심사 분리 측면에서 깔끔
function Navigation() {
    const { isAuthenticated, user, logout } = useAuth()
    const navigate = useNavigate()

    const handleLogout = async () => {
        await logout()
        navigate('/')
    }

    return (
        <nav className="bg-white shadow px-6 py-4 flex items-center gap-4">
            <Link to="/" className="text-primary font-bold text-lg">
                나비마켓
            </Link>

            <div className="ml-auto flex items-center gap-4">
                {isAuthenticated ? (
                    <>
                      <span className="text-sm text-gray-700">
                        {user.nickname}님
                      </span>
                        <Link
                            to="/mypage"
                            className="text-sm text-gray-700 hover:text-primary"
                        >
                            마이페이지
                        </Link>
                        <button
                            onClick={handleLogout}
                            className="text-sm text-gray-600 hover:text-red-600"
                        >
                            로그아웃
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" className="text-sm text-gray-700 hover:text-primary">
                            로그인
                        </Link>
                        <Link to="/signup" className="text-sm text-gray-700 hover:text-primary">
                            회원가입
                        </Link>
                    </>
                )}
            </div>
        </nav>
    )
}

export default App