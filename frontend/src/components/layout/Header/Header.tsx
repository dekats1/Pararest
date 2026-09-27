import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext.tsx";
import UserDropdown from "../Header/UserDropdown.tsx";
import "../../../styles/layout/Header/Header.css";

const Header: React.FC = () => {
    const [isScrolled, setIsScrolled] = useState(false);
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const handleScroll = () => setIsScrolled(window.scrollY > 20);
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    useEffect(() => {
        if (location.hash) {
            const id = location.hash.replace('#', '');
            const element = document.getElementById(id);
            if (element) {
                setTimeout(() => {
                    element.scrollIntoView({ behavior: 'smooth' });
                }, 0);
            }
        }
    }, [location]);

    const isHomePage = location.pathname === '/' || location.pathname==='/home';

    const handleNavClick = (e: React.MouseEvent, sectionId: string) => {
        e.preventDefault();
        if (isHomePage) {
            const element = document.getElementById(sectionId);
            element?.scrollIntoView({ behavior: 'smooth' });
            window.history.pushState(null, '', `/#${sectionId}`);
        } else {
            navigate(`/home#${sectionId}`);
        }
    };

    return (
        <header className={`header ${isScrolled ? 'scrolled' : ''} ${!isHomePage ? 'always-solid' : ''}`}>
            <div className="header-container">
                <Link to="/" className="logo-section">
                    <span className="logo-text">PARAREST</span>
                    <span className="logo-dot">.</span>
                </Link>

                <nav className="nav-menu">
                    <button onClick={(e) => handleNavClick(e, 'hero')} className="nav-item">Главная</button>
                    <button onClick={(e) => handleNavClick(e, 'menu')} className="nav-item">Меню</button>
                    <button onClick={(e) => handleNavClick(e, 'about')} className="nav-item">О нас</button>
                    <button onClick={(e) => handleNavClick(e, 'reservation')} className="nav-item">Контакты</button>
                </nav>

                <div className="header-actions">
                {isAuthenticated ? (
                        <UserDropdown />
                    ) : (
                        <div className="auth-group">
                            <button className="link-btn" onClick={() => navigate('/login')}>Войти</button>
                            <button className="primary-btn" onClick={() => navigate('/register')}>Присоединиться</button>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;