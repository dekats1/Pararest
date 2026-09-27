import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/BlockedPage.css';

const BlockedPage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="blocked-page">
            <div className="blocked-bg-circles">
                <div className="blocked-circle blocked-circle--1" />
                <div className="blocked-circle blocked-circle--2" />
            </div>

            <div className="blocked-container">
                <div className="blocked-icon-wrap">
                    <div className="blocked-icon">
                        <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="24" cy="24" r="22" stroke="#dc2626" strokeWidth="2.5" />
                            <path d="M14 14L34 34M34 14L14 34" stroke="#dc2626" strokeWidth="2.5" strokeLinecap="round"/>
                        </svg>
                    </div>
                </div>

                <div className="blocked-content">
                    <span className="blocked-label">Доступ ограничен</span>
                    <h1 className="blocked-title">Ваш аккаунт заблокирован</h1>
                    <p className="blocked-text">
                        Администратор ограничил доступ к вашей учётной записи.
                        Если вы считаете, что это ошибка — обратитесь на стойку reception.
                    </p>

                    <div className="blocked-info-card">
                        <div className="blocked-info-row">
                            <span className="blocked-info-label">Причина</span>
                            <span className="blocked-info-value">Нарушение правил пользования</span>
                        </div>
                        <div className="blocked-info-divider" />
                        <div className="blocked-info-row">
                            <span className="blocked-info-label">Поддержка</span>
                            <span className="blocked-info-value">reception@hotel.ru</span>
                        </div>
                    </div>

                    <button
                        className="blocked-btn"
                        onClick={() => navigate('/login')}
                    >
                        Выйти из аккаунта
                    </button>
                </div>
            </div>
        </div>
    );
};

export default BlockedPage;