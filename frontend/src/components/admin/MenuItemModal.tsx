import React, { useState, useEffect } from 'react';
import { type MenuItemFormData } from '../../api/menuservice';
import { ingredientService } from '../../api/ingredientService';
import '../../styles/admin/MenuTableCss.css';
import type { MenuItemDTO } from "../../models/MenuItemDTO";
import {compositeservice} from "../../api/compositeservice.ts";

interface IngredientSelection {
    id: number;
    name: string;
    quantity: number;
    unit: string;
}

interface MenuItemModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (formData: MenuItemFormData) => Promise<void>;
    item: MenuItemDTO | null;
    categories: string[];
}

const MenuItemModal: React.FC<MenuItemModalProps> = ({
                                                         isOpen,
                                                         onClose,
                                                         onSave,
                                                         item,
                                                         categories
                                                     }) => {
    const [formData, setFormData] = useState<MenuItemFormData>({
        name: '',
        description: '',
        price: 0,
        category: '',
        recipe: '',
        imageUrl: '',
        isActive: true
    });

    const [ingredients, setIngredients] = useState<IngredientSelection[]>([]);
    const [availableIngredients, setAvailableIngredients] = useState<Array<{ id: number; name: string; unit: string }>>([]);
    const [selectedIngredientId, setSelectedIngredientId] = useState<number | ''>('');
    const [newCategory, setNewCategory] = useState('');
    const [isAddingCategory, setIsAddingCategory] = useState(false);
    const [saving, setSaving] = useState(false);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [loadingIngredients, setLoadingIngredients] = useState(false);

    useEffect(() => {
        if (isOpen && !item) {
            fetchIngredients();
        }
    }, [isOpen, item]);

    const fetchIngredients = async () => {
        try {
            setLoadingIngredients(true);
            // Используем существующий метод getAllIngredients вместо getIngredientsMap
            const data = await ingredientService.getAllIngredients();
            // Преобразуем в нужный формат
            const mappedIngredients = data.map(ing => ({
                id: ing.id,
                name: ing.name,
                unit: ing.unit
            }));
            setAvailableIngredients(mappedIngredients);
        } catch (error) {
            console.error('Ошибка загрузки ингредиентов:', error);
        } finally {
            setLoadingIngredients(false);
        }
    };

    useEffect(() => {
        if (item) {
            setFormData({
                name: item.name,
                description: item.description,
                price: item.price,
                category: item.category,
                recipe: item.recipe || '',
                imageUrl: item.imageUrl || '',
                isActive: item.isActive
            });
        } else {
            setFormData({
                name: '',
                description: '',
                price: 0,
                category: categories[0] || '',
                recipe: '',
                imageUrl: '',
                isActive: true
            });
            setIngredients([]);
        }
    }, [item, categories]);

    const validate = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.name.trim()) newErrors.name = 'Название обязательно';
        if (!formData.description.trim()) newErrors.description = 'Описание обязательно';
        if (formData.price <= 0) newErrors.price = 'Цена должна быть больше 0';
        if (!formData.category) newErrors.category = 'Категория обязательна';

        if (!item && ingredients.length === 0) {
            newErrors.ingredients = 'Добавьте хотя бы один ингредиент';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const addIngredient = () => {
        if (selectedIngredientId === '') {
            alert('Выберите ингредиент');
            return;
        }

        const ingredient = availableIngredients.find(i => i.id === Number(selectedIngredientId));
        if (!ingredient) return;

        if (ingredients.some(i => i.id === ingredient.id)) {
            alert('Этот ингредиент уже добавлен');
            return;
        }

        setIngredients([
            ...ingredients,
            {
                id: ingredient.id,
                name: ingredient.name,
                quantity: 0,
                unit: ingredient.unit
            }
        ]);
        setSelectedIngredientId('');
    };

    const removeIngredient = (id: number) => {
        setIngredients(ingredients.filter(i => i.id !== id));
    };

    const updateIngredientQuantity = (id: number, quantity: number) => {
        setIngredients(ingredients.map(i =>
            i.id === id ? { ...i, quantity } : i
        ));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        try {
            setSaving(true);

            if (!item) {
                const ingredientsMap: Record<number, number> = {};
                ingredients.forEach(i => {
                    if (i.quantity > 0) {
                        ingredientsMap[i.id] = i.quantity;
                    }
                });

                await compositeservice.createMenuItemWithRecipe(formData, ingredientsMap);
            }

            await onSave(formData);
            onClose();
        } catch (error) {
            console.error('Ошибка сохранения:', error);
            alert('Ошибка при сохранении');
        } finally {
            setSaving(false);
        }
    };

    const handleAddCategory = () => {
        if (newCategory.trim()) {
            setFormData({ ...formData, category: newCategory.trim() });
            setIsAddingCategory(false);
            setNewCategory('');
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content" style={{ maxWidth: '600px', maxHeight: '80vh', overflowY: 'auto' }}>
                <div className="modal-header">
                    <h3>{item ? 'Редактировать блюдо' : 'Добавить новое блюдо'}</h3>
                    <button onClick={onClose} className="close-btn">&times;</button>
                </div>

                <form onSubmit={handleSubmit} className="modal-form">
                    <div className="form-group">
                        <label>Название *</label>
                        <input
                            type="text"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className={errors.name ? 'error' : ''}
                        />
                        {errors.name && <span className="error-message">{errors.name}</span>}
                    </div>

                    <div className="form-group">
                        <label>Описание *</label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            rows={3}
                            className={errors.description ? 'error' : ''}
                        />
                        {errors.description && <span className="error-message">{errors.description}</span>}
                    </div>

                    <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
                        <div className="form-group">
                            <label>Цена *</label>
                            <input
                                type="number"
                                value={formData.price}
                                onChange={(e) => setFormData({ ...formData, price: Number(e.target.value) })}
                                min="0"
                                step="0.01"
                                className={errors.price ? 'error' : ''}
                            />
                            {errors.price && <span className="error-message">{errors.price}</span>}
                        </div>

                        <div className="form-group">
                            <label>Категория *</label>
                            {!isAddingCategory ? (
                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                    <select
                                        value={formData.category}
                                        onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                        className={errors.category ? 'error' : ''}
                                        style={{ flex: 1 }}
                                    >
                                        <option value="">Выберите категорию</option>
                                        {categories.map(cat => (
                                            <option key={cat} value={cat}>{cat}</option>
                                        ))}
                                    </select>
                                    <button
                                        type="button"
                                        onClick={() => setIsAddingCategory(true)}
                                        style={{ padding: '0.5rem 1rem' }}
                                    >
                                        +
                                    </button>
                                </div>
                            ) : (
                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                    <input
                                        type="text"
                                        value={newCategory}
                                        onChange={(e) => setNewCategory(e.target.value)}
                                        placeholder="Название категории"
                                        autoFocus
                                        style={{ flex: 1 }}
                                    />
                                    <button type="button" onClick={handleAddCategory}>✓</button>
                                    <button type="button" onClick={() => setIsAddingCategory(false)}>✗</button>
                                </div>
                            )}
                            {errors.category && <span className="error-message">{errors.category}</span>}
                        </div>
                    </div>

                    {!item && (
                        <div className="ingredients-section" style={{ marginTop: '1rem', padding: '1rem', border: '1px solid #ddd', borderRadius: '4px' }}>
                            <h4 style={{ marginTop: 0 }}>Ингредиенты для рецепта *</h4>

                            {loadingIngredients ? (
                                <div>Загрузка ингредиентов...</div>
                            ) : (
                                <>
                                    <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
                                        <select
                                            value={selectedIngredientId}
                                            onChange={(e) => setSelectedIngredientId(e.target.value ? Number(e.target.value) : '')}
                                            style={{ flex: 1 }}
                                        >
                                            <option value="">Выберите ингредиент</option>
                                            {availableIngredients.map(ing => (
                                                <option key={ing.id} value={ing.id}>
                                                    {ing.name} ({ing.unit})
                                                </option>
                                            ))}
                                        </select>
                                        <button
                                            type="button"
                                            onClick={addIngredient}
                                            style={{ padding: '0.5rem 1rem' }}
                                        >
                                            Добавить
                                        </button>
                                    </div>

                                    {ingredients.length > 0 && (
                                        <div>
                                            <h5 style={{ marginBottom: '0.5rem' }}>Добавленные ингредиенты:</h5>
                                            {ingredients.map(ing => (
                                                <div key={ing.id} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem' }}>
                                                    <span style={{ width: '120px' }}>{ing.name}</span>
                                                    <input
                                                        type="number"
                                                        value={ing.quantity}
                                                        onChange={(e) => updateIngredientQuantity(ing.id, Number(e.target.value))}
                                                        min="0"
                                                        step="0.1"
                                                        placeholder="Количество"
                                                        style={{ width: '100px' }}
                                                    />
                                                    <span style={{ width: '50px' }}>{ing.unit}</span>
                                                    <button
                                                        type="button"
                                                        onClick={() => removeIngredient(ing.id)}
                                                        style={{ padding: '0.25rem 0.5rem' }}
                                                    >
                                                        ✕
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                    {errors.ingredients && (
                                        <span className="error-message">{errors.ingredients}</span>
                                    )}
                                </>
                            )}
                        </div>
                    )}

                    <div className="form-group">
                        <label>URL изображения</label>
                        <input
                            type="text"
                            value={formData.imageUrl || ''}
                            onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                            placeholder="https://example.com/image.jpg"
                        />
                    </div>

                    <div className="form-group">
                        <label>Рецепт блюда</label>
                        <textarea
                            value={formData.recipe || ''}
                            onChange={(e) => setFormData({ ...formData, recipe: e.target.value })}
                            rows={5}
                            placeholder="Ингредиенты и способ приготовления..."
                        />
                    </div>

                    <div className="form-group checkbox">
                        <label>
                            <input
                                type="checkbox"
                                checked={formData.isActive}
                                onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                            />
                            Активно (доступно для заказа)
                        </label>
                    </div>

                    <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
                        <button type="button" onClick={onClose} className="cancel-button">
                            Отмена
                        </button>
                        <button type="submit" className="save-button" disabled={saving}>
                            {saving ? 'Сохранение...' : item ? 'Сохранить' : 'Создать'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default MenuItemModal;