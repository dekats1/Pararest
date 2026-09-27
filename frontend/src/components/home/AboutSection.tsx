import React from 'react';
import '../../styles/home/AboutSection.css';

const AboutSection: React.FC = () => {
    return (
        <section id="about" className="about-section">
            <div className="container">
                <div className="about-grid">

                    <div className="about-image">
                        <img
                            className="about-image-photo"
                            loading="lazy"
                            src="https://img.freepik.com/premium-photo/ultimate-fine-dining-experience-large-luxury-restaurant-generative-ai_634358-1407.jpg?semt=ais_hybrid"
                            alt="Интерьер ресторана с элегантной атмосферой"
                        />
                        <div className="image-badge">
                            <span className="badge-icon">🏆</span>
                            <span className="badge-text">Лучший ресторан 2025</span>
                        </div>
                    </div>

                    <div className="about-content">
                        <span className="section-label">О нас</span>
                        <h2 className="section-title">
                            Традиции и современность <br />
                            в каждом блюде
                        </h2>

                        <p className="about-text">
                            Наш ресторан при отеле ParaHotel — это место, где встречаются
                            изысканный вкус и безупречный сервис. Мы создаем незабываемые
                            кулинарные впечатления для наших гостей вот уже более 15 лет.
                        </p>

                        <div className="features-grid">
                            <div className="feature-item">
                                <div className="feature-icon">👨‍🍳</div>
                                <h3>Шеф-повар мирового класса</h3>
                                <p>Авторская кухня от признанных мастеров</p>
                            </div>

                            <div className="feature-item">
                                <div className="feature-icon">🌿</div>
                                <h3>Свежие ингредиенты</h3>
                                <p>Только качественные продукты от проверенных поставщиков</p>
                            </div>

                            <div className="feature-item">
                                <div className="feature-icon">🍷</div>
                                <h3>Винная карта</h3>
                                <p>Более 200 позиций отборных вин</p>
                            </div>

                            <div className="feature-item">
                                <div className="feature-icon">✨</div>
                                <h3>Уютная атмосфера</h3>
                                <p>Идеальное место для любого повода</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default AboutSection;