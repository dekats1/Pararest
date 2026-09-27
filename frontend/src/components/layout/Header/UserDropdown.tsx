import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import '../../../styles/layout/Header/UserDropdown.css';

const UserDropdown: React.FC = () => {
    const { user, logout } = useAuth();
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setDropdownOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = async () => {
        await logout();
        navigate('/');
        setDropdownOpen(false);
    };

    const handleItemClick = () => {
        setDropdownOpen(false);
    };

    return (
        <div className="user-dropdown" ref={dropdownRef}>
            <button
                className="dropdown-toggle"
                onClick={() => setDropdownOpen(!dropdownOpen)}
            >
                <div className="user-avatar">
                    {user?.fullName?.charAt(0) || user?.login?.charAt(0) || 'U'}
                </div>
                <span className="user-name">
                    {user?.fullName || user?.login}
                </span>
                <span className="dropdown-arrow">
                    {dropdownOpen ? '▲' : '▼'}
                </span>
            </button>

            {dropdownOpen && (
                <div className="dropdown-menu">
                    <div className="dropdown-header">
                        <div className="dropdown-avatar">
                            {user?.fullName?.charAt(0) || user?.login?.charAt(0) || 'U'}
                        </div>
                        <div className="dropdown-user-info">
                            <div className="dropdown-user-name">
                                {user?.fullName || user?.login}
                            </div>
                            <div className="dropdown-user-email">
                                {user?.email}
                            </div>
                        </div>
                    </div>

                    <div className="dropdown-divider"></div>
                    <Link
                        to="/profile"
                        className="dropdown-item"
                        onClick={handleItemClick}
                    >
                        <span className="item-icon">👤</span>
                        Профиль
                    </Link>

                    <Link
                        to="/orders"
                        className="dropdown-item"
                        onClick={handleItemClick}
                    >
                        <span className="item-icon">📦</span>
                        Мои заказы
                    </Link>

                    <Link
                        to="/cart"
                        className="dropdown-item"
                        onClick={handleItemClick}
                    >
                        <span className="item-icon">📦</span>
                        Корзина
                    </Link>

                    <Link
                        to="/wallet"
                        className="dropdown-item"
                        onClick={handleItemClick}
                    >
                        <span className="item-icon">💰</span>
                        Кошелёк
                    </Link>

                    <div className="dropdown-divider"></div>

                    <button
                        className="dropdown-item logout"
                        onClick={handleLogout}
                    >
                        <span className="item-icon">🚪</span>
                        Выйти
                    </button>
                </div>
            )}
        </div>
    );
};

export default UserDropdown;