import React, { useState, useEffect } from 'react';
import {type IngredientDTO, type AddIngredientDTO, Metrics, metricsLabels } from '../../models/IngredientDTO';
import { ingredientService } from '../../api/ingredientService';
import '../../styles/admin/IngredientsTable.css';

const IngredientsTable: React.FC = () => {
    const [ingredients, setIngredients] = useState<IngredientDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [newIngredient, setNewIngredient] = useState<AddIngredientDTO>({
        name: '',
        unit: Metrics.KG,
        currentStock: 0,
        minStock: 0
    });

    const fetchIngredients = async () => {
        try {
            setLoading(true);
            const data = await ingredientService.getAllIngredients();
            setIngredients(data);
            setError(null);
        } catch (err) {
            setError('Не удалось загрузить ингредиенты');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchIngredients();
    }, []);

    const handleAddIngredient = async () => {
        if (!newIngredient.name.trim()) {
            alert('Введите название ингредиента');
            return;
        }

        try {
            const added = await ingredientService.addIngredient(newIngredient);
            setIngredients(prev => [...prev, added]);
            setShowAddForm(false);
            setNewIngredient({
                name: '',
                unit: Metrics.KG,
                currentStock: 0,
                minStock: 0
            });
        } catch (err) {
            alert('Ошибка при добавлении ингредиента');
            console.error(err);
        }
    };

    const handleDeleteIngredient = async (id: number) => {
        if (!window.confirm('Вы уверены, что хотите удалить этот ингредиент?')) return;

        try {
            await ingredientService.deleteIngredient(id);
            setIngredients(prev => prev.filter(ing => ing.id !== id));
        } catch (err) {
            alert('Ошибка при удалении ингредиента');
            console.error(err);
        }
    };

    const getStockStatus = (current: number, min: number) => {
        if (current <= 0) return 'out';
        if (current < min) return 'low';
        return 'normal';
    };

    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString('ru-RU');
    };

    if (loading && ingredients.length === 0) {
        return (
            <div className="ingredients-section loading">
                <div className="spinner"></div>
                <p>Загрузка ингредиентов...</p>
            </div>
        );
    }

    return (
        <div className="ingredients-section">
            <div className="ingredients-header">
                <h2>Управление ингредиентами</h2>
                <button
                    className="add-btn"
                    onClick={() => setShowAddForm(!showAddForm)}
                >
                    {showAddForm ? '✕ Отмена' : '+ Добавить ингредиент'}
                </button>
            </div>

            {error && (
                <div className="error-message">
                    {error}
                    <button onClick={fetchIngredients} className="retry-btn">
                        Повторить
                    </button>
                </div>
            )}

            {showAddForm && (
                <div className="add-ingredient-form">
                    <h3>Новый ингредиент</h3>
                    <div className="form-grid">
                        <div className="form-group">
                            <label>Название</label>
                            <input
                                type="text"
                                value={newIngredient.name}
                                onChange={(e) => setNewIngredient({...newIngredient, name: e.target.value})}
                                placeholder="Например: Мука"
                            />
                        </div>
                        <div className="form-group">
                            <label>Единица измерения</label>
                            <select
                                value={newIngredient.unit}
                                onChange={(e) => setNewIngredient({...newIngredient, unit: e.target.value as Metrics})}
                            >
                                {Object.values(Metrics).map(metric => (
                                    <option key={metric} value={metric}>
                                        {metricsLabels[metric]}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Текущий запас</label>
                            <input
                                type="number"
                                step="0.01"
                                min="0"
                                value={newIngredient.currentStock}
                                onChange={(e) => setNewIngredient({...newIngredient, currentStock: parseFloat(e.target.value) || 0})}
                            />
                        </div>
                        <div className="form-group">
                            <label>Минимальный запас</label>
                            <input
                                type="number"
                                step="0.01"
                                min="0"
                                value={newIngredient.minStock}
                                onChange={(e) => setNewIngredient({...newIngredient, minStock: parseFloat(e.target.value) || 0})}
                            />
                        </div>
                    </div>
                    <div className="form-actions">
                        <button onClick={handleAddIngredient} className="save-btn">
                            Сохранить
                        </button>
                        <button onClick={() => setShowAddForm(false)} className="cancel-btn">
                            Отмена
                        </button>
                    </div>
                </div>
            )}

            <div className="table-responsive">
                <table className="ingredients-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Название</th>
                        <th>Ед. изм.</th>
                        <th>Текущий запас</th>
                        <th>Мин. запас</th>
                        <th>Статус</th>
                        <th>Обновлено</th>
                        <th>Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {ingredients.length === 0 ? (
                        <tr>
                            <td colSpan={8} className="empty-message">
                                Ингредиенты не найдены
                            </td>
                        </tr>
                    ) : (
                        ingredients.map(ing => {
                            const status = getStockStatus(ing.currentStock, ing.minStock);
                            return (
                                <tr key={ing.id} className={`status-${status}`}>
                                    <td>{ing.id}</td>
                                    <td>{ing.name}</td>
                                    <td>{metricsLabels[ing.unit]}</td>
                                    <td className={ing.currentStock < ing.minStock ? 'low-stock' : ''}>
                                        {ing.currentStock} {metricsLabels[ing.unit]}
                                    </td>
                                    <td>{ing.minStock} {metricsLabels[ing.unit]}</td>
                                    <td>
                                            <span className={`status-badge ${status}`}>
                                                {status === 'normal' && '✅ В норме'}
                                                {status === 'low' && '⚠️ Мало'}
                                                {status === 'out' && '❌ Закончился'}
                                            </span>
                                    </td>
                                    <td>{formatDate(ing.updatedAt)}</td>
                                    <td>
                                        <button
                                            onClick={() => handleDeleteIngredient(ing.id)}
                                            className="delete-btn"
                                            title="Удалить"
                                        >
                                            🗑️
                                        </button>
                                    </td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default IngredientsTable;