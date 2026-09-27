import React from 'react';
import '../../styles/home/HeroSection.css';

const HeroSection: React.FC = () => {

    return (
        <section id="hero" className="hero-section">
            <div className="hero-overlay"></div>

            <div className="hero-content">
                <h1 className="hero-title">
                    Изысканная кухня <br />
                    <span className="gradient-text">в сердце города</span>
                </h1>

                <p className="hero-subtitle">
                    Откройте для себя уникальное сочетание традиций и инноваций
                </p>

                <div className="hero-buttons">
                    <button
                        className="btn btn-primary"
                        onClick={() => document.getElementById('reservation')?.scrollIntoView({ behavior: 'smooth' })}
                    >
                        Забронировать столик
                    </button>
                    <button
                        className="btn btn-secondary"
                        onClick={() => document.getElementById('menu')?.scrollIntoView({ behavior: 'smooth' })}
                    >
                        Посмотреть меню
                    </button>
                </div>

                <div className="hero-stats">
                    <div className="stat-item">
                        <span className="stat-number">15+</span>
                        <span className="stat-label">Лет опыта</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-number">5★</span>
                        <span className="stat-label">Рейтинг</span>
                    </div>
                    <div className="stat-item">
                        <span className="stat-number">100+</span>
                        <span className="stat-label">Блюд в меню</span>
                    </div>
                </div>
            </div>

            <div className="scroll-indicator">
                <div className="mouse"></div>
            </div>
        </section>
    );
};

export default HeroSection;