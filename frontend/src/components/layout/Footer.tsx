import React from 'react';
import { Link } from 'react-router-dom';
import '../../styles/layout/Footer.css';

const Footer: React.FC = () => {
    return (
        <footer className="footer">
            <div className="container">
                <div className="footer-grid">

                    <div className="footer-column">
                        <h4 className="footer-heading">Навигация</h4>
                        <ul className="footer-links">
                            <li><Link to="/">Главная</Link></li>
                            <li><Link to="/menu">Меню</Link></li>
                            <li><Link to="/reservation">Бронирование</Link></li>
                            <li><Link to="/about">О нас</Link></li>
                            <li><Link to="/contact">Контакты</Link></li>
                        </ul>
                    </div>

                    <div className="footer-column">
                        <h3 className="footer-title">Actisys Restaurant</h3>
                        <p className="footer-text">
                            Ресторан при отеле, где каждое блюдо — это произведение искусства,
                            а каждый гость — особенный.
                        </p>
                        <div className="social-links">
                            <a href="#" className="social-link" aria-label="Instagram">📷</a>
                            <a href="#" className="social-link" aria-label="Facebook">📘</a>
                            <a href="#" className="social-link" aria-label="VK">🔵</a>
                            <a href="#" className="social-link" aria-label="Telegram">✈️</a>
                        </div>
                    </div>

                    <div className="footer-column">
                        <h4 className="footer-heading">Контакты</h4>
                        <ul className="footer-contacts">
                            <li>
                                <span className="icon">📞</span>
                                <span>+375 (44) 123-45-67</span>
                            </li>
                            <li>
                                <span className="icon">✉️</span>
                                <span>info@actisys.com</span>
                            </li>
                            <li>
                                <span className="icon">📍</span>
                                <span>Минск, ул. Примерная, 123</span>
                            </li>
                        </ul>
                    </div>

                    <div className="footer-column">
                        <h4 className="footer-heading">Время работы</h4>
                        <ul className="footer-schedule">
                            <li>
                                <span>Понедельник - Пятница</span>
                                <span>10:00 - 23:00</span>
                            </li>
                            <li>
                                <span>Суббота - Воскресенье</span>
                                <span>09:00 - 00:00</span>
                            </li>
                        </ul>
                    </div>

                    <div className="footer-column footer-column-map">
                        <h4 className="footer-heading">Мы на карте</h4>
                        <div className="footer-map">
                            <iframe
                                src="https://yandex.ru/map-widget/v1/?um=constructor%3Ab9b4c967e38a083349afd9b5c89eb606961c908edc9aebac3b3f811dc849a39f&amp;source=constructor"
                                width="100%"
                                height="300"
                                frameBorder="0"
                                title="Карта Actisys Restaurant"
                                loading="lazy"
                            ></iframe>
                        </div>
                    </div>
                </div>

                <div className="footer-bottom">
                    <p>&copy; 2026 Para Restaurant. Все права защищены.</p>
                    <div className="footer-legal">
                        <Link to="/privacy">Политика конфиденциальности</Link>
                        <Link to="/terms">Условия использования</Link>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;