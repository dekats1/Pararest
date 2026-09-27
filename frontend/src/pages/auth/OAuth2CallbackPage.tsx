import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../api/authservice.ts';

const OAuth2CallbackPage: React.FC = () => {
    const navigate = useNavigate();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;
        processed.current = true;

        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
        const refreshToken = params.get('refreshToken');

        if (!token) {
            navigate('/login?error=oauth_failed', { replace: true });
            return;
        }

        authService.setToken(token);
        if (refreshToken) {
            authService.setRefreshToken(refreshToken);
        }

        window.history.replaceState({}, document.title, '/oauth2/callback');

        navigate('/', { replace: true });
    }, [navigate]);

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            gap: '16px',
            color: '#64748b',
            fontFamily: 'sans-serif'
        }}>
            <svg
                width="40" height="40"
                viewBox="0 0 24 24"
                style={{ animation: 'spin 1s linear infinite' }}
                xmlns="http://www.w3.org/2000/svg"
            >
                <style>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
                <circle cx="12" cy="12" r="10" stroke="#e2e8f0" strokeWidth="3" fill="none"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="#24292f" strokeWidth="3" fill="none" strokeLinecap="round"/>
            </svg>
            <p>Выполняем вход через GitHub…</p>
        </div>
    );
};

export default OAuth2CallbackPage;