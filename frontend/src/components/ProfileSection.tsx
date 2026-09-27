import React, { useEffect, useState } from 'react';
import '../styles/ProfileSection.css';
import { userService } from '../api/userservice';
import type {UserDTO} from "../models/UserDTO.ts";
import type {UpdateUserProfileDTO} from "../models/types/user.types.ts";

const ProfileSection: React.FC = () => {
    const [user, setUser] = useState<UserDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [roomNumber, setRoomNumber] = useState('');

    const [saving, setSaving] = useState(false);
    const [saveSuccess, setSaveSuccess] = useState(false);
    const [saveError, setSaveError] = useState<string | null>(null);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const data = await userService.getMyInfo();
                const u = Array.isArray(data) ? data[0] : data;
                setUser(u);
                setEmail(u.email ?? '');
                setPhone(u.phone ?? '');
                setRoomNumber(u.roomNumber != null ? String(u.roomNumber) : '');
            } catch {
                setError('Не удалось загрузить данные профиля');
            } finally {
                setLoading(false);
            }
        };
        fetchUser();
    }, []);

    const handleSave = async () => {
        setSaving(true);
        setSaveSuccess(false);
        setSaveError(null);
        try {
            const payload: UpdateUserProfileDTO = {
                email,
                phone,
                roomNumber: roomNumber !== '' ? Number(roomNumber) : null,
            };

            const updated = await userService.updateProfile(payload);

            setUser(updated);
            setEmail(updated.email ?? '');
            setPhone(updated.phone ?? '');
            setRoomNumber(updated.roomNumber != null ? String(updated.roomNumber) : '');
            setSaveSuccess(true);
        } catch {
            setSaveError('Ошибка при сохранении данных');
        } finally {
            setSaving(false);
        }
    };

    const getInitials = (fullName: string) => {
        return fullName
            .split(' ')
            .slice(0, 2)
            .map(n => n[0])
            .join('')
            .toUpperCase();
    };

    const formatDate = (dateStr: string) => {
        if (!dateStr) return '—';
        const d = new Date(dateStr);
        return d.toLocaleDateString('ru-RU', { day: '2-digit', month: 'long', year: 'numeric' });
    };

    const formatWallet = (amount: number) => {
        return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'BYN', maximumFractionDigits: 0 }).format(amount);
    };

    const roleLabels: Record<string, string> = {
        ADMIN: 'Администратор',
        USER: 'Гость',
        MANAGER: 'Менеджер',
    };

    if (loading) {
        return (
            <section className="profile-section">
                <div className="profile-loading">
                    <span className="profile-loading__spinner" />
                    <p>Загружаем профиль…</p>
                </div>
            </section>
        );
    }

    if (error || !user) {
        return (
            <section className="profile-section">
                <div className="profile-error">{error ?? 'Пользователь не найден'}</div>
            </section>
        );
    }

    return (
        <section className="profile-section">
            <div className="profile-section__inner">

                {/* ── Left card ── */}
                <aside className="profile-card">
                    <div className="profile-card__avatar">
                        {getInitials(user.fullName)}
                    </div>

                    <h2 className="profile-card__name">{user.fullName}</h2>
                    <span className={`profile-card__role ${user.blocked ? 'profile-card__role--blocked' : ''}`}>
                        {user.blocked ? 'Заблокирован' : (roleLabels[user.role] ?? user.role)}
                    </span>

                    <ul className="profile-card__meta">
                        <li>
                            <span className="profile-card__meta-label">Логин</span>
                            <span className="profile-card__meta-value">{user.login}</span>
                        </li>
                        <li>
                            <span className="profile-card__meta-label">Дата рождения</span>
                            <span className="profile-card__meta-value">{formatDate(user.birthday)}</span>
                        </li>
                        <li>
                            <span className="profile-card__meta-label">Баланс</span>
                            <span className="profile-card__meta-value profile-card__meta-value--accent">
                                {formatWallet(user.wallet)}
                            </span>
                        </li>
                    </ul>
                </aside>

                {/* ── Right form ── */}
                <div className="profile-form-wrap">
                    <header className="profile-form-wrap__header">
                        <h1 className="profile-form-wrap__title">Мой профиль</h1>
                        <p className="profile-form-wrap__subtitle">Обновите контактную информацию</p>
                    </header>

                    <div className="profile-form">
                        <div className="profile-form__group">
                            <label className="profile-form__label" htmlFor="pf-email">
                                Электронная почта
                            </label>
                            <input
                                id="pf-email"
                                className="profile-form__input"
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="example@mail.ru"
                            />
                        </div>

                        <div className="profile-form__group">
                            <label className="profile-form__label" htmlFor="pf-phone">
                                Телефон
                            </label>
                            <input
                                id="pf-phone"
                                className="profile-form__input"
                                type="tel"
                                value={phone}
                                onChange={e => setPhone(e.target.value)}
                                placeholder="+7 (___) ___-__-__"
                            />
                        </div>

                        <div className="profile-form__group">
                            <label className="profile-form__label" htmlFor="pf-room">
                                Номер комнаты
                            </label>
                            <input
                                id="pf-room"
                                className="profile-form__input"
                                type="number"
                                min={1}
                                value={roomNumber}
                                onChange={e => setRoomNumber(e.target.value)}
                                placeholder="Номер в отеле"
                            />
                        </div>

                        {saveSuccess && (
                            <p className="profile-form__feedback profile-form__feedback--success">
                                ✓ Данные успешно сохранены
                            </p>
                        )}
                        {saveError && (
                            <p className="profile-form__feedback profile-form__feedback--error">
                                {saveError}
                            </p>
                        )}

                        <button
                            className="profile-form__submit"
                            onClick={handleSave}
                            disabled={saving}
                        >
                            {saving ? 'Сохраняем…' : 'Сохранить изменения'}
                        </button>
                    </div>
                </div>

            </div>
        </section>
    );
};

export default ProfileSection;