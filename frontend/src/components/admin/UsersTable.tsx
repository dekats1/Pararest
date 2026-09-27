import React, { useState, useEffect } from 'react';
import { type UserDTO, UserRole } from '../../models/UserDTO';
import '../../styles/admin/UsersTable.css';
import {userService} from "../../api/userservice.ts";

const roleLabels: Record<UserRole, string> = {
    [UserRole.USER]: 'Клиент',
    [UserRole.ADMIN]: 'Админ',
    [UserRole.MANAGER]: 'Менеджер',
    [UserRole.EMPLOYEE]: 'Сотрудник'
};

const UsersTable: React.FC = () => {
    const [users, setUsers] = useState<UserDTO[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [roleFilter, setRoleFilter] = useState<UserRole | 'all'>('all');

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await userService.getUsers();
            setUsers(data);
        } catch (err) {
            setError('Не удалось загрузить пользователей. Попробуйте позже.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const filteredUsers = users.filter(user =>
        roleFilter === 'all' ? true : user.role === roleFilter
    );

    const handleDelete = async (id: string) => {
        if (!window.confirm('Вы уверены, что хотите удалить этого пользователя?')) return;
        try {
            await userService.deleteUser(id);
            setUsers(prev => prev.filter(user => user.id !== id));
        } catch (err) {
            alert('Ошибка при удалении пользователя');
            console.error(err);
        }
    };

    const handleRoleChange = async (id: string, newRole: UserRole) => {
        try {
            const updatedUser = await userService.changeUserRole(id, newRole);
            setUsers(prev => prev.map(user =>
                user.id === id ? updatedUser : user
            ));
        } catch (err) {
            alert('Ошибка при изменении роли');
            console.error(err);
        }
    };

    const handleToggleBlock = async (id: string) => {
        try {
            const updatedUser = await userService.toggleUserBlock(id);
            setUsers(prev => prev.map(user =>
                user.id === id ? updatedUser : user
            ));
        } catch (err) {
            alert('Ошибка при изменении статуса блокировки');
            console.error(err);
        }
    };

    const formatDate = (dateStr: string) => {
        const [year, month, day] = dateStr.split('-');
        return `${day}.${month}.${year}`;
    };

    if (loading && users.length === 0) {
        return (
            <div className="users-section loading">
                <div className="spinner"></div>
                <p>Загрузка пользователей...</p>
            </div>
        );
    }

    if (error && users.length === 0) {
        return (
            <div className="users-section error">
                <p className="error-message">{error}</p>
                <button onClick={fetchUsers} className="retry-btn">
                    Повторить
                </button>
            </div>
        );
    }

    return (
        <div className="users-section">
            <div className="users-header">
                <h2>Управление пользователями</h2>
                <button onClick={fetchUsers} className="refresh-btn">
                    🔄 Обновить
                </button>
            </div>

            <div className="role-tabs">
                <button
                    className={roleFilter === 'all' ? 'active' : ''}
                    onClick={() => setRoleFilter('all')}
                >
                    Все ({users.length})
                </button>
                {Object.values(UserRole).map(role => {
                    const count = users.filter(u => u.role === role).length;
                    return (
                        <button
                            key={role}
                            className={roleFilter === role ? 'active' : ''}
                            onClick={() => setRoleFilter(role)}
                        >
                            {roleLabels[role]} ({count})
                        </button>
                    );
                })}
            </div>

            <div className="table-responsive">
                <table className="users-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Логин</th>
                        <th>ФИО</th>
                        <th>Email</th>
                        <th>Телефон</th>
                        <th>Дата рожд.</th>
                        <th>Кошелёк</th>
                        <th>Роль</th>
                        <th>Комната</th>
                        <th>Статус</th>
                        <th>Действия</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filteredUsers.length === 0 ? (
                        <tr>
                            <td colSpan={11} className="empty-message">
                                Пользователи не найдены
                            </td>
                        </tr>
                    ) : (
                        filteredUsers.map(user => (
                            <tr key={user.id} className={user.blocked ? 'blocked-row' : ''}>
                                <td>{user.id}</td>
                                <td>{user.login}</td>
                                <td>{user.fullName}</td>
                                <td>{user.email}</td>
                                <td>{user.phone}</td>
                                <td>{formatDate(user.birthday)}</td>
                                <td className={user.wallet === 0 ? 'zero-wallet' : ''}>
                                    {user.wallet} BYN
                                </td>
                                <td>
                                    <select
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value as UserRole)}
                                        className="role-select"
                                    >
                                        {Object.values(UserRole).map(role => (
                                            <option key={role} value={role}>
                                                {roleLabels[role]}
                                            </option>
                                        ))}
                                    </select>
                                </td>
                                <td>{user.roomNumber ?? '—'}</td>
                                <td>
                                        <span className={`status-badge ${user.blocked ? 'blocked' : 'active'}`}>
                                            {user.blocked ? '🔒 Заблокирован' : 'Активен'}
                                        </span>
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button
                                            onClick={() => handleToggleBlock(user.id)}
                                            className={`block-btn ${user.blocked ? 'unblock' : 'block'}`}
                                        >
                                            {user.blocked ? '🔓 Разблокировать' : '🔒 Заблокировать'}
                                        </button>
                                        <button
                                            onClick={() => handleDelete(user.id)}
                                            className="delete-btn"
                                        >
                                            🗑️ Удалить
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            <div className="table-footer">
                Показано: {filteredUsers.length} из {users.length} пользователей
            </div>
        </div>
    );
};

export default UsersTable;