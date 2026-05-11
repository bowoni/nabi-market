import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
    <StrictMode>
        {/* BrowserRouter - URL과 React 라우팅을 연결 */}
        <BrowserRouter>
            {/* AuthProvider - 모든 페이지에서 인증 상태(user, login, logout) 접근 가능하게 함 */}
            <AuthProvider>
                <App />
            </AuthProvider>
        </BrowserRouter>
    </StrictMode>,
)