import React, { useState, useEffect } from 'react';
import type {
    AddSupplyDTO,
    SupplyDTO,
    SupplierDTO,
    SupplyStatus
} from '../../models/SupplyDTO';
import type {IngredientDTO} from "../../models/IngredientDTO.ts";
import {ingredientService} from "../../api/ingredientService.ts";
import '../../styles/admin/SuppliesTable.css';

const supplyStatusLabels: Record<SupplyStatus, string> = {
    IN_DELIVERY_CENTER: 'В пункте приема',
    IN_SORT_CENTER: 'На сортировке',
    IN_SHIPPED: 'В пути',
    SHIPPED: 'Доставлено',
    CANCELED: 'Отменено'
};
const SuppliesTable: React.FC = () => {
    const [supplies, setSupplies] = useState<SupplyDTO[]>([]);
    const [ingredients, setIngredients] = useState<IngredientDTO[]>([]);
    const [suppliers, setSuppliers] = useState<SupplierDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [showRequestForm, setShowRequestForm] = useState(false);
    const [newRequest, setNewRequest] = useState<AddSupplyDTO>({
        supplierId: 0,
        items: [{
            ingredientId: 0,
            quantity: 0,
            priceAtSupply: 0
        }]
    });

    const fetchData = async () => {
        try {
            setLoading(true);
            const [suppliesData, ingredientsData, suppliersData] = await Promise.all([
                ingredientService.getAllSupplies(),
                ingredientService.getAllIngredients(),
                ingredientService.getSuppliers()
            ]);
            setSupplies(suppliesData);
            setIngredients(ingredientsData);
            setSuppliers(suppliersData);
        } catch (err) {
            console.error('Ошибка загрузки данных:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const requestItem = newRequest.items[0];

    const handleCreateRequest = async () => {
        if (
            !newRequest.supplierId ||
            !requestItem ||
            !requestItem.ingredientId ||
            requestItem.quantity <= 0 ||
            requestItem.priceAtSupply <= 0
        ) {
            alert('Заполните все поля');
            return;
        }

        try {
            await ingredientService.createSupplyRequest(newRequest);
            await fetchData();
            setShowRequestForm(false);
            setNewRequest({
                supplierId: 0,
                items: [{ ingredientId: 0, quantity: 0, priceAtSupply: 0 }]
            });
        } catch (err) {
            alert('Ошибка при создании запроса');
            console.error(err);
        }
    };

    const formatDate = (dateStr: string | null) => {
        if (!dateStr) return '—';
        try {
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) {
                return '—';
            }
            // Отображаем только дату (без времени)
            return date.toLocaleDateString('ru-RU', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            });
        } catch {
            return '—';
        }
    };

    const formatPrice = (value: number) => {
        return `${Number(value || 0).toLocaleString('ru-RU')} BYN`;
    };
    if (loading) {
        return (
            <div className="supplies-section loading">
                <div className="spinner"></div>
                <p>Загрузка поставок...</p>
            </div>
        );
    }

    return (
        <div className="supplies-section">
            <div className="supplies-header">
                <h2>Управление поставками</h2>
                <button
                    className="request-btn"
                    onClick={() => setShowRequestForm(!showRequestForm)}
                >
                    {showRequestForm ? '✕ Отмена' : '+ Новый запрос поставщику'}
                </button>
            </div>

            {showRequestForm && (
                <div className="request-form">
                    <h3>Запрос поставки</h3>
                    <div className="form-grid">
                        <div className="form-group">
                            <label>Поставщик</label>
                            <select
                                value={newRequest.supplierId}
                                onChange={(e) => setNewRequest({...newRequest, supplierId: parseInt(e.target.value)})}
                            >
                                <option value={0}>Выберите поставщика</option>
                                {suppliers.map(s => (
                                    <option key={s.id} value={s.id}>
                                        {s.name} ({s.phone})
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Ингредиент</label>
                            <select
                                value={requestItem?.ingredientId ?? 0}
                                onChange={(e) =>
                                    setNewRequest({
                                        ...newRequest,
                                        items: [{
                                            ...requestItem,
                                            ingredientId: parseInt(e.target.value)
                                        }]
                                    })
                                }
                            >
                                <option value={0}>Выберите ингредиент</option>
                                {ingredients.map(i => (
                                    <option key={i.id} value={i.id}>
                                        {i.name} (Текущий запас: {i.currentStock})
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Количество</label>
                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                value={requestItem?.quantity ?? 0}
                                onChange={(e) =>
                                    setNewRequest({
                                        ...newRequest,
                                        items: [{
                                            ...requestItem,
                                            quantity: parseFloat(e.target.value) || 0
                                        }]
                                    })
                                }
                                placeholder="Введите количество"
                            />
                        </div>
                        <div className="form-group">
                            <label>Цена за единицу (BYN)</label>
                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                value={requestItem?.priceAtSupply ?? 0}
                                onChange={(e) =>
                                    setNewRequest({
                                        ...newRequest,
                                        items: [{
                                            ...requestItem,
                                            priceAtSupply: parseFloat(e.target.value) || 0
                                        }]
                                    })
                                }
                                placeholder="Введите цену"
                            />
                        </div>
                    </div>
                    <div className="form-actions">
                        <button onClick={handleCreateRequest} className="save-btn">
                            Отправить запрос
                        </button>
                        <button onClick={() => setShowRequestForm(false)} className="cancel-btn">
                            Отмена
                        </button>
                    </div>
                </div>
            )}

            <div className="table-responsive">
                <table className="supplies-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Поставщик</th>
                        <th>Позиции</th>
                        <th>Сумма</th>
                        <th>Дата запроса</th>
                        <th>Дата поставки</th>
                        <th>Статус</th>
                    </tr>
                    </thead>
                    <tbody>
                    {supplies.length === 0 ? (
                        <tr>
                                <td colSpan={7} className="empty-message">
                                Нет записей о поставках
                            </td>
                        </tr>
                    ) : (
                        supplies.map(supply => (
                            <tr key={supply.id}>
                                <td>{supply.id}</td>
                                <td>
                                    <strong>{supply.supplier.name}</strong>
                                </td>
                                <td>{supply.items.map(item => `${item.ingredient.name} (${item.quantity})`).join(', ') || '—'}</td>
                                <td>{formatPrice(supply.totalPrice)}</td>
                                <td>{formatDate(supply.dateOfRequest)}</td>
                                <td>{formatDate(supply.dateOfSupply)}</td>
                                <td>
                                    <span className={`supply-status ${supply.status === 'SHIPPED' ? 'completed' : 'pending'}`}>
                                        {supplyStatusLabels[supply.status] ?? supply.status}
                                    </span>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SuppliesTable;