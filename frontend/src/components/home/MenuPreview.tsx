import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/home/MenuPreview.css';
import type {MenuItemDTO} from "../../models/types/menu.types.ts";
import {menuService} from "../../api/menuservice.ts";


const MenuPreview: React.FC = () => {
    const navigate = useNavigate();
    const [activeCategory, setActiveCategory] = useState('Все');
    const [menuItems, setMenuItems] = useState<MenuItemDTO[]>([]);

    useEffect(()=>{
        const loadMenu = async ()=>{
            try{
                const data = await menuService.getAllMenuItems();
                setMenuItems(data);
            }catch (error){
                console.error("Не удалось загрузить меню:", error);
            }
        };
        loadMenu().catch(console.error);
    }, []);

    const categories = ['Все', 'Основные блюда', 'Салаты', 'Супы', 'Десерты', 'Напитки'];

    const filteredItems = activeCategory === 'Все'
        ? menuItems
        : menuItems.filter(item => item.category.name === activeCategory);

    return (
        <section id="menu" className="menu-preview-section">
            <div className="container">
                <div className="section-header">
                    <span className="section-label">Меню</span>
                    <h2 className="section-title">Наши фирменные блюда</h2>
                    <p className="section-subtitle">
                        Попробуйте самые популярные позиции из нашего меню
                    </p>
                </div>

                <div className="category-filter">
                    {categories.map(category => (
                        <button
                            key={category}
                            className={`category-btn ${activeCategory === category ? 'active' : ''}`}
                            onClick={() => setActiveCategory(category)}
                        >
                            {category}
                        </button>
                    ))}
                </div>

                <div className="menu-grid">
                    {filteredItems.map(item => (
                        <div key={item.id} className="menu-card">
                            <div className="menu-card-image">
                                <div className="image-placeholder">
                                    <img alt={"Dish photo"} src={item.imageUrl}/>
                                </div>
                            </div>

                            <div className="menu-card-content">
                                <h3 className="menu-card-title">{item.name}</h3>
                                <p className="menu-card-description">{item.description}</p>

                                <div className="menu-card-footer">
                                    <span className="menu-card-price">{item.price} BYN</span>
                                    <button className="add-to-cart-btn">
                                        Добавить
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                <div className="menu-cta">
                    <button
                        className="btn btn-primary"
                        onClick={() => navigate('/menu')}
                    >
                        Посмотреть полное меню
                    </button>
                </div>
            </div>
        </section>
    );
};

export default MenuPreview;