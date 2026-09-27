import React from 'react';
import { type MenuItemDTO } from '../../models/MenuItemDTO';
import '../../styles/admin/RecipeModal.css';

interface RecipeModalProps {
    isOpen: boolean;
    onClose: () => void;
    item: MenuItemDTO | null;
}

const RecipeModal: React.FC<RecipeModalProps> = ({ isOpen, onClose, item }) => {
    if (!isOpen || !item) return null;

    return (
        <div className="recipe-modal-overlay">
            <div className="recipe-modal-content">
                <div className="recipe-modal-header">
                    <h3>Рецепт: {item.name}</h3>
                    <button onClick={onClose} className="close-btn">&times;</button>
                </div>

                <div className="recipe-modal-body">
                    {item.recipe ? (
                        <div className="recipe-content">
                            <div className="recipe-section">
                                <h4>📝 Ингредиенты и приготовление</h4>
                                <p className="recipe-text">{item.recipe}</p>
                            </div>

                            <div className="recipe-meta">
                                <p><strong>Добавил:</strong> {item.addedByManagerName}</p>
                                <p><strong>Дата добавления:</strong> {new Date(item.createdAt).toLocaleDateString('ru-RU')}</p>
                                {item.updatedAt && (
                                    <p><strong>Последнее обновление:</strong> {new Date(item.updatedAt).toLocaleDateString('ru-RU')}</p>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="no-recipe">
                            <p>Рецепт не добавлен</p>
                        </div>
                    )}
                </div>

                <div className="recipe-modal-footer">
                    <button onClick={onClose} className="close-button">Закрыть</button>
                </div>
            </div>
        </div>
    );
};

export default RecipeModal;