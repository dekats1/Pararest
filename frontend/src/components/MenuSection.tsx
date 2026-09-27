import React, { useState, useEffect, useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import {menuService} from "../api/menuservice.ts";
import {inventoryservice} from "../api/inventoryservice.ts";
import type { MenuItemDTO, CategoryDTO } from '../models/types/menu.types';
import '../styles/MenuSection.css';
import type {RecipeDTO} from "../models/types/recipe.types.ts";
import {orderService} from "../api/orderservice.ts";

const MenuSection: React.FC = () => {
    const { isAuthenticated } = useAuth();
    const [menuItems, setMenuItems] = useState<MenuItemDTO[]>([]);
    const [categories, setCategories] = useState<CategoryDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState<string>('all');
    const [selectedItem, setSelectedItem] = useState<MenuItemDTO | null>(null);
    const [showComposition, setShowComposition] = useState(false);
    const [compositionLoading, setCompositionLoading] = useState(false);
    const [recipe, setRecipe] = useState<RecipeDTO[]>([]);
    const [notification, setNotification] = useState<{ show: boolean; message: string }>({ show: false, message: '' });

    useEffect(() => {
        fetchMenuData();
    }, []);

    const fetchMenuData = async () => {
        try {
            setLoading(true);
            const [menuData, categoriesData] = await Promise.all([
                menuService.getAllMenuItems(),
                menuService.getAllCategories()
            ]);
            setMenuItems(menuData);
            if (Array.isArray(categoriesData)) {
                setCategories(categoriesData);
            } else if (categoriesData && typeof categoriesData === 'object') {
                setCategories([categoriesData]);
            } else {
                setCategories([]);
            }
        } catch (err) {
            setError('Не удалось загрузить меню');
            console.error('Error fetching menu:', err);
        } finally {
            setLoading(false);
        }
    };

    const fetchRecipe = async (itemId: number) => {
        try {
            setCompositionLoading(true);
            const recipeData = await inventoryservice.getMenuItemRecipe(itemId);
            if (Array.isArray(recipeData)) {
                setRecipe(recipeData);
            } else {
                setRecipe([]);
            }
        } catch (err) {
            console.error('Error fetching recipe:', err);
            setRecipe([]);
        } finally {
            setCompositionLoading(false);
        }
    };

    const handleViewComposition = (item: MenuItemDTO) => {
        setSelectedItem(item);
        setShowComposition(true);
        fetchRecipe(item.id);
    };

    const handleAddToCart = async (item: MenuItemDTO) => {
        if (!isAuthenticated) {
            showNotification('Пожалуйста, войдите в систему для добавления товаров в корзину');
            return;
        }

        try {
            await orderService.addToCart(item.id);
            window.dispatchEvent(new Event('cartUpdated'));
            showNotification(`${item.name} добавлен в корзину`);
        } catch (err) {
            showNotification('Не удалось добавить товар в корзину');
        }
    };

    const showNotification = (message: string) => {
        setNotification({ show: true, message });
        setTimeout(() => {
            setNotification({ show: false, message: '' });
        }, 3000);
    };

    const formatQuantity = (quantity: number, unit: string): string => {
        if (unit === 'TH') {
            return `${quantity} шт.`;
        } else if (unit === 'G') {
            if (quantity >= 1000) {
                return `${quantity / 1000} кг`;
            }
            return `${quantity} г`;
        } else if (unit === 'KG') {
            return `${quantity} кг`;
        } else if (unit === 'L') {
            return `${quantity} л`;
        } else if (unit === 'ML') {
            if (quantity >= 1000) {
                return `${quantity / 1000} л`;
            }
            return `${quantity} мл`;
        }
        return `${quantity} ${unit}`;
    };


    const filteredItems = useMemo(() => {
        let filtered = menuItems;

        if (selectedCategory !== 'all') {
            filtered = filtered.filter(item => item.category?.name === selectedCategory);
        }

        if (searchTerm.trim()) {
            filtered = filtered.filter(item =>
                item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                item.description.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        return filtered;
    }, [menuItems, selectedCategory, searchTerm]);

    if (loading) {
        return (
            <div className="menu-section">
                <div className="container">
                    <div className="loading-spinner">
                        <div className="spinner"></div>
                        <p>Загрузка меню...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="menu-section">
                <div className="container">
                    <div className="error-message">
                        <p>{error}</p>
                        <button onClick={fetchMenuData} className="retry-button">
                            Попробовать снова
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <section className="menu-section">
            <div className="container">
                <div className="menu-header">
                    <h1 className="menu-title">Наше меню</h1>
                    <p className="menu-subtitle">
                        Выберите блюда из нашего разнообразного меню, приготовленные с любовью из свежих ингредиентов
                    </p>
                </div>

                <div className="menu-controls">
                    <div className="search-container">
                        <input
                            type="text"
                            placeholder="Поиск блюд..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                        <svg className="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                        </svg>
                    </div>

                    <div className="categories-filter">
                        <button
                            className={`category-chip ${selectedCategory === 'all' ? 'active' : ''}`}
                            onClick={() => setSelectedCategory('all')}
                        >
                            Все
                        </button>
                        {categories.map((category, index) => (
                            <button
                                key={index}
                                className={`category-chip ${selectedCategory === category.name ? 'active' : ''}`}
                                onClick={() => setSelectedCategory(category.name)}
                            >
                                {category.name}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="menu-grid">
                    {filteredItems.map((item) => (
                        <div key={item.id} className="menu-card">
                            <div className="card-image-container">
                                <img
                                    src={item.imageUrl || '/api/placeholder/400/300'}
                                    alt={item.name}
                                    className="card-image"
                                    onError={(e) => {
                                        (e.target as HTMLImageElement).src = '/api/placeholder/400/300';
                                    }}
                                />
                                {item.preparationTime && (
                                    <span className="preparation-time">
                    ⏱️ {item.preparationTime} мин
                  </span>
                                )}
                            </div>
                            <div className="card-content">
                                <div className="card-header">
                                    <h3 className="card-title">{item.name}</h3>
                                    <span className="card-price">{item.price} BYN</span>
                                </div>
                                <p className="card-description">{item.description}</p>
                                <div className="card-footer">
                                    <button
                                        className="btn-composition"
                                        onClick={() => handleViewComposition(item)}
                                    >
                                        Состав
                                    </button>
                                    {isAuthenticated ? (
                                        <button
                                            className="btn-cart"
                                            onClick={() => handleAddToCart(item)}
                                        >
                                            В корзину
                                        </button>
                                    ) : (
                                        <button
                                            className="btn-cart-disabled"
                                            disabled
                                            title="Войдите в систему для добавления в корзину"
                                        >
                                            В корзину
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                {filteredItems.length === 0 && (
                    <div className="no-results">
                        <p>По вашему запросу ничего не найдено</p>
                        <button onClick={() => {
                            setSearchTerm('');
                            setSelectedCategory('all');
                        }} className="reset-button">
                            Сбросить фильтры
                        </button>
                    </div>
                )}
            </div>

            {showComposition && selectedItem && (
                <div className="modal-overlay" onClick={() => setShowComposition(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close" onClick={() => setShowComposition(false)}>×</button>
                        <h2 className="modal-title">{selectedItem.name}</h2>
                        {compositionLoading ? (
                            <div className="composition-loading">
                                <div className="spinner-small"></div>
                                <p>Загрузка состава...</p>
                            </div>
                        ) : recipe && recipe.length > 0 ? (
                            <div className="composition-details">
                                <div className="ingredients-list">
                                    <h4>Ингредиенты:</h4>
                                    <ul>
                                        {recipe.map((recipeItem, index) => (
                                            <li key={index}>
                                                <span className="ingredient-name">{recipeItem.ingredient.name}</span>
                                                <span className="ingredient-quantity">
                          {formatQuantity(recipeItem.quantity, recipeItem.ingredient.unit)}
                        </span>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                                <div className="recipe-note">
                                    <p>* Состав может незначительно отличаться в зависимости от сезона и наличия продуктов</p>
                                </div>
                            </div>
                        ) : (
                            <p className="composition-error">Мы не можем раскрыть секрет состава данного блюда😘</p>
                        )}
                    </div>
                </div>
            )}

            {notification.show && (
                <div className="notification">
                    {notification.message}
                </div>
            )}
        </section>
    );
};

export default MenuSection;