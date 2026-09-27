import React, { useState, useEffect } from 'react';
import { menuService, type MenuItemFormData } from '../../api/menuservice';
import { type MenuItemDTO, type CategoryDTO } from '../../models/types/menu.types.ts';
import MenuItemModal from '../admin/MenuItemModal.tsx';
import RecipeModal from '../admin/RecipeModal';
import '../../styles/admin/MenuTableCss.css';

const MenuTable: React.FC = () => {
    const [menuItems, setMenuItems] = useState<MenuItemDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [categoryFilter, setCategoryFilter] = useState<string>('all');
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [categories, setCategories] = useState<string[]>([]);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isRecipeModalOpen, setIsRecipeModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState<MenuItemDTO | null>(null);
    const [searchQuery, setSearchQuery] = useState('');

    // Функция нормализации данных
    const normalizeMenuItem = (item: any): MenuItemDTO => {
        // Логируем сырые данные
        console.log('Raw item from server:', item);
        console.log('Category field:', item.category, typeof item.category);
        console.log('AdderName field:', item.adderName);
        console.log('Active field:', item.active, typeof item.active);

        // Нормализуем категорию
        let category: CategoryDTO;
        if (typeof item.category === 'string') {
            category = { name: item.category };
        } else if (item.category && typeof item.category === 'object') {
            category = {
                name: item.category.name || item.category.categoryName || 'Без категории'
            };
        } else {
            category = { name: 'Без категории' };
        }

        // Нормализуем active
        const active = item.active === true ||
            item.active === 'true' ||
            item.active === 1 ||
            item.isActive === true ||
            item.isActive === 'true' ||
            item.isActive === 1;

        return {
            id: item.id,
            name: item.name || '',
            description: item.description || '',
            price: Number(item.price) || 0,
            category: category,
            imageUrl: item.imageUrl || null,
            active: active,
            adderName: item.adderName ||
                item.addedByManagerName ||
                item.managerName ||
                item.addedBy?.name ||
                item.manager?.name ||
                'Не указан',
            createdAt: item.createdAt || new Date().toISOString(),
            updatedAt: item.updatedAt
        };
    };

    const fetchMenu = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await menuService.getAllMenuItemsForAdmin();

            console.log('Raw data from server:', data);

            // Нормализуем все элементы
            const normalizedData = data.map(normalizeMenuItem);

            console.log('Normalized data:', normalizedData);

            setMenuItems(normalizedData);

            // Извлекаем имена категорий
            const uniqueCategories = Array.from(
                new Set(normalizedData.map(item => item.category.name))
            );
            setCategories(uniqueCategories.sort());
        } catch (err) {
            setError('Не удалось загрузить меню. Попробуйте позже.');
            console.error('Error fetching menu:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMenu();
    }, []);

    const filteredItems = menuItems.filter(item => {
        if (searchQuery) {
            const query = searchQuery.toLowerCase();
            const matchesSearch =
                item.name.toLowerCase().includes(query) ||
                item.description.toLowerCase().includes(query) ||
                item.category.name.toLowerCase().includes(query);
            if (!matchesSearch) return false;
        }

        if (categoryFilter !== 'all' && item.category.name !== categoryFilter) {
            return false;
        }

        if (statusFilter === 'active' && !item.active) return false;
        if (statusFilter === 'inactive' && item.active) return false;

        return true;
    });

    const handleAdd = () => {
        setSelectedItem(null);
        setIsModalOpen(true);
    };

    const handleEdit = (item: MenuItemDTO) => {
        setSelectedItem(item);
        setIsModalOpen(true);
    };

    const handleSave = async (formData: MenuItemFormData) => {
        try {
            if (selectedItem) {
                const updated = await menuService.updateMenuItem(selectedItem.id, formData);
                const normalizedUpdated = normalizeMenuItem(updated);
                setMenuItems(prev => prev.map(item =>
                    item.id === normalizedUpdated.id ? normalizedUpdated : item
                ));
            } else {
                const created = await menuService.createMenuItem(formData);
                const normalizedCreated = normalizeMenuItem(created);
                setMenuItems(prev => [...prev, normalizedCreated]);

                if (!categories.includes(normalizedCreated.category.name)) {
                    setCategories(prev => [...prev, normalizedCreated.category.name].sort());
                }
            }
        } catch (err) {
            console.error('Ошибка сохранения:', err);
            throw err;
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Вы уверены, что хотите удалить эту позицию из меню?')) return;
        try {
            await menuService.deleteMenuItem(id);
            setMenuItems(prev => prev.filter(item => item.id !== id));

            const remainingCategories = Array.from(new Set(
                menuItems
                    .filter(item => item.id !== id)
                    .map(item => item.category.name)
            ));
            setCategories(remainingCategories.sort());
        } catch (err) {
            alert('Ошибка при удалении позиции');
            console.error(err);
        }
    };

    const handleToggleActive = async (id: number) => {
        try {
            const updated = await menuService.toggleActive(id);
            const normalizedUpdated = normalizeMenuItem(updated);
            setMenuItems(prev => prev.map(item =>
                item.id === id ? normalizedUpdated : item
            ));
        } catch (err) {
            alert('Ошибка при изменении статуса');
            console.error(err);
        }
    };

    const handleViewRecipe = (item: MenuItemDTO) => {
        setSelectedItem(item);
        setIsRecipeModalOpen(true);
    };

    const formatPrice = (price: number) => {
        return `${price.toLocaleString('ru-RU')} BYN`;
    };

    const formatDate = (dateStr: string) => {
        if (!dateStr) return '—';
        try {
            return new Date(dateStr).toLocaleDateString('ru-RU');
        } catch {
            return '—';
        }
    };

    if (loading && menuItems.length === 0) {
        return (
            <div className="menu-section loading">
                <div className="spinner"></div>
                <p>Загрузка меню...</p>
            </div>
        );
    }

    if (error && menuItems.length === 0) {
        return (
            <div className="menu-section error">
                <p className="error-message">{error}</p>
                <button onClick={fetchMenu} className="retry-btn">
                    Повторить
                </button>
            </div>
        );
    }

    return (
        <>
            <div className="menu-section">
                <div className="menu-header">
                    <h2>🍽️ Управление меню</h2>
                    <button onClick={fetchMenu} className="refresh-btn" title="Обновить">
                        🔄
                    </button>
                </div>

                <div className="menu-toolbar">
                    <button onClick={handleAdd} className="add-btn">
                        ➕ Добавить блюдо
                    </button>

                    <div className="search-wrapper">
                        <input
                            type="text"
                            placeholder="🔍 Поиск по названию, описанию..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="search-input"
                        />
                    </div>
                </div>

                <div className="filters-bar">
                    <div className="filter-group">
                        <label>Категория:</label>
                        <select
                            value={categoryFilter}
                            onChange={(e) => setCategoryFilter(e.target.value)}
                            className="filter-select"
                        >
                            <option value="all">Все категории</option>
                            {categories.map(cat => (
                                <option key={cat} value={cat}>{cat}</option>
                            ))}
                        </select>
                    </div>

                    <div className="filter-group">
                        <label>Статус:</label>
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="filter-select"
                        >
                            <option value="all">Все</option>
                            <option value="active">Активные</option>
                            <option value="inactive">Неактивные</option>
                        </select>
                    </div>

                    <div className="results-info">
                        Найдено: {filteredItems.length} из {menuItems.length}
                    </div>
                </div>

                <div className="table-responsive">
                    <table className="menu-table">
                        <thead>
                        <tr>
                            <th>Фото</th>
                            <th>Название</th>
                            <th>Категория</th>
                            <th>Цена</th>
                            <th>Менеджер</th>
                            <th>Дата</th>
                            <th>Статус</th>
                            <th>Действия</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredItems.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="empty-message">
                                    {menuItems.length === 0
                                        ? 'Меню пустое. Добавьте первую позицию!'
                                        : 'По вашему запросу ничего не найдено'}
                                </td>
                            </tr>
                        ) : (
                            filteredItems.map(item => (
                                <tr key={item.id} className={!item.active ? 'inactive-row' : ''}>
                                    <td>
                                        {item.imageUrl ? (
                                            <img
                                                src={item.imageUrl}
                                                alt={item.name}
                                                className="menu-thumbnail"
                                                onError={(e) => {
                                                    const target = e.target as HTMLImageElement;
                                                    target.style.display = 'none';
                                                    const parent = target.parentElement;
                                                    if (parent) {
                                                        const span = document.createElement('span');
                                                        span.className = 'no-image';
                                                        span.innerHTML = '📷';
                                                        parent.appendChild(span);
                                                    }
                                                }}
                                            />
                                        ) : (
                                            <span className="no-image">📷</span>
                                        )}
                                    </td>
                                    <td>
                                        <div className="item-name">{item.name || '—'}</div>
                                        <div className="item-description">{item.description || '—'}</div>
                                    </td>
                                    <td>
                                        <span className="category-badge">
                                            {item.category?.name || '—'}
                                        </span>
                                    </td>
                                    <td className="price-cell">{formatPrice(item.price)}</td>
                                    <td>
                                        <div className="manager-info">
                                            <span className="manager-name">
                                                {item.adderName || '—'}
                                            </span>
                                        </div>
                                    </td>
                                    <td>{formatDate(item.createdAt)}</td>
                                    <td>
                                        <span className={`status-badge ${item.active ? 'active' : 'inactive'}`}>
                                            {item.active ? '✅ Активен' : '⛔ Неактивен'}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                onClick={() => handleViewRecipe(item)}
                                                className="recipe-btn"
                                                title="Посмотреть рецепт"
                                            >
                                                📖
                                            </button>
                                            <button
                                                onClick={() => handleEdit(item)}
                                                className="edit-btn"
                                                title="Редактировать"
                                            >
                                                ✏️
                                            </button>
                                            <button
                                                onClick={() => handleToggleActive(item.id)}
                                                className={`toggle-btn ${item.active ? 'deactivate' : 'activate'}`}
                                                title={item.active ? 'Деактивировать' : 'Активировать'}
                                            >
                                                {item.active ? '🔴' : '🟢'}
                                            </button>
                                            <button
                                                onClick={() => handleDelete(item.id)}
                                                className="delete-btn"
                                                title="Удалить"
                                            >
                                                🗑️
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            </div>

            <MenuItemModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleSave}
                item={selectedItem}
                categories={categories}
            />

            <RecipeModal
                isOpen={isRecipeModalOpen}
                onClose={() => setIsRecipeModalOpen(false)}
                item={selectedItem}
            />
        </>
    );
};

export default MenuTable;