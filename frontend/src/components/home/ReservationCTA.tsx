import React from 'react';
import '../../styles/home/ReservationCTA.css';

const ReservationCTA: React.FC = () => {
    return (
        <section id= "reservation" className="reservation-cta-section">
            <div className="cta-overlay"></div>

            <div className="container">
                <div className="cta-content">
                    <h2 className="cta-title">
                        Готовы насладиться <br />
                        незабываемым ужином?
                    </h2>

                    <p className="cta-subtitle">
                        Забронируйте столик прямо сейчас
                    </p>

                    <div className="contact-info">
                        <div className="contact-item">
                            <span className="icon">📞</span>
                            <div>
                                <span className="label">Телефон</span>
                                <span className="value">+375 (44) 123-45-67</span>
                            </div>
                        </div>

                        <div className="contact-item">
                            <span className="icon">🕐</span>
                            <div>
                                <span className="label">Время работы</span>
                                <span className="value">Ежедневно 10:00 - 23:00</span>
                            </div>
                        </div>

                        <div className="contact-item">
                            <span className="icon">📍</span>
                            <div>
                                <span className="label">Адрес</span>
                                <span className="value">Минск, ул. Примерная, 123</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default ReservationCTA;