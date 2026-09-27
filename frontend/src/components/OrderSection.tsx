import React, {useEffect, useState} from 'react';
import '../styles/OrderSection.css';
import {type OrderDTO, OrderStatus, OrderType,} from '../models/types/order.types';
import {type MenuItemDTO} from '../models/types/menu.types';
import {menuService} from "../api/menuservice.ts";
import {orderService} from "../api/orderservice.ts";
import {PaymentStatus, PaymentType} from "../models/types/payment.types.ts";

const ACTIVE_STATUSES: OrderStatus[] = [
    OrderStatus.PENDING,
    OrderStatus.PREPARING,
];

const STATUS_LABEL: Record<OrderStatus, string> = {
    [OrderStatus.PENDING]:    'Ожидает',
    [OrderStatus.PREPARING]:  'Готовится',
    [OrderStatus.COMPLETED]:  'Выполнен',
    [OrderStatus.CANCELED]:   'Отменён',
    [OrderStatus.DISCARDED]:  'Отклонён',
    [OrderStatus.IN_CART]:    'В корзине',
};

const TYPE_LABEL: Record<OrderType, string> = {
    [OrderType.DELIVERY_TO_HOTEL_NUMBER]: 'Доставка в номер',
    [OrderType.IN_RESTAURANT]:            'В ресторане',
};

const PAYMENT_STATUS_LABEL: Record<PaymentStatus, string> = {
    [PaymentStatus.PAID]:     'Оплачен',
    [PaymentStatus.NOT_PAID]: 'Не оплачен',
    [PaymentStatus.ERROR]:    'Ошибка оплаты',
};

const PAYMENT_TYPE_LABEL: Record<PaymentType, string> = {
    [PaymentType.CASH]:          'Наличные',
    [PaymentType.CARD]:          'Карта',
    [PaymentType.REPLENISHMENT]: 'Счёт',
};

const formatDateTime = (str?: string) => {
    if (!str) return '—';
    try {
        const date = new Date(str);
        if (isNaN(date.getTime())) {
            return '—';
        }
        // Отображаем только дату (без времени)
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit', month: 'long', year: 'numeric'
        });
    } catch {
        return '—';
    }
};

const formatPrice = (n: number) =>
    new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'BYN', maximumFractionDigits: 0 }).format(n);

interface DetailModalProps {
    order: OrderDTO;
    onClose: () => void;
}

const DetailModal: React.FC<DetailModalProps> = ({ order, onClose }) => {
    const [items, setItems] = useState<MenuItemDTO[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const ids = order.items.map(i => i.menuItemId);
        menuService.getMenuItemsByIds(ids)
            .then(setItems)
            .catch(() => setItems([]))
            .finally(() => setLoading(false));
    }, [order.id]);

    const getMenuItem = (menuItemId: number) =>
        items.find(m => m.id === menuItemId);

    return (
        <div className="orders-modal-overlay" onClick={onClose}>
            <div className="orders-modal" onClick={e => e.stopPropagation()}>
                <button className="orders-modal__close" onClick={onClose} aria-label="Закрыть">✕</button>

                <header className="orders-modal__header">
                    <span className="orders-modal__order-num">Заказ #{order.id}</span>
                    <span className={`orders-modal__status orders-modal__status--${order.status.toLowerCase()}`}>
                        {STATUS_LABEL[order.status]}
                    </span>
                </header>

                <div className="orders-modal__meta">
                    <div>
                        <span className="orders-modal__meta-label">Тип</span>
                        <span>{TYPE_LABEL[order.type]}</span>
                    </div>
                    <div>
                        <span className="orders-modal__meta-label">Оплата</span>
                        <span>{PAYMENT_TYPE_LABEL[order.paymentType]}</span>
                    </div>
                    <div>
                        <span className="orders-modal__meta-label">Статус оплаты</span>
                        <span className={order.paymentStatus === PaymentStatus.PAID ? 'orders-modal__paid' : ''}>
                            {PAYMENT_STATUS_LABEL[order.paymentStatus]}
                        </span>
                    </div>
                    <div>
                        <span className="orders-modal__meta-label">Время заказа</span>
                        <span>{formatDateTime(order.orderTime)}</span>
                    </div>
                    {order.deliveryTime && (
                        <div>
                            <span className="orders-modal__meta-label">Время доставки</span>
                            <span>{formatDateTime(order.deliveryTime)}</span>
                        </div>
                    )}
                    {order.userRequest && (
                        <div className="orders-modal__meta--full">
                            <span className="orders-modal__meta-label">Пожелание</span>
                            <span>{order.userRequest}</span>
                        </div>
                    )}
                </div>

                <div className="orders-modal__items">
                    <h3 className="orders-modal__items-title">Состав заказа</h3>
                    {loading ? (
                        <div className="orders-modal__loading">
                            <span className="orders-spinner" />
                        </div>
                    ) : (
                        <ul className="orders-modal__item-list">
                            {order.items.map(oi => {
                                const menu = getMenuItem(oi.menuItemId);
                                return (
                                    <li key={oi.id} className="orders-modal__item">
                                        {menu?.imageUrl && (
                                            <img
                                                className="orders-modal__item-img"
                                                src={menu.imageUrl}
                                                alt={menu.name}
                                            />
                                        )}
                                        <div className="orders-modal__item-info">
                                            <span className="orders-modal__item-name">
                                                {menu?.name ?? `Позиция #${oi.menuItemId}`}
                                            </span>
                                            {menu?.category && (
                                                <span className="orders-modal__item-cat">{menu.category.name}</span>
                                            )}
                                        </div>
                                        <div className="orders-modal__item-right">
                                            <span className="orders-modal__item-qty">× {oi.quantity}</span>
                                            {menu && (
                                                <span className="orders-modal__item-price">
                                                    {formatPrice(menu.price * oi.quantity)}
                                                </span>
                                            )}
                                        </div>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>

                <div className="orders-modal__total">
                    <span>Итого</span>
                    <span className="orders-modal__total-price">{formatPrice(order.totalPrice)}</span>
                </div>
            </div>
        </div>
    );
};

interface OrderCardProps {
    order: OrderDTO;
    onDetails: (order: OrderDTO) => void;
}

const OrderCard: React.FC<OrderCardProps> = ({ order, onDetails }) => {
    const isActive = ACTIVE_STATUSES.includes(order.status);

    return (
        <article className={`order-card ${isActive ? 'order-card--active' : ''}`}>
            <div className="order-card__top">
                <span className="order-card__id">#{order.id}</span>
                <span className={`order-card__badge order-card__badge--${order.status.toLowerCase()}`}>
                    {STATUS_LABEL[order.status]}
                </span>
            </div>

            <div className="order-card__body">
                <div className="order-card__row">
                    <span className="order-card__label">Тип</span>
                    <span>{TYPE_LABEL[order.type]}</span>
                </div>
                <div className="order-card__row">
                    <span className="order-card__label">Позиций</span>
                    <span>{order.items.reduce((s, i) => s + i.quantity, 0)}</span>
                </div>
                <div className="order-card__row">
                    <span className="order-card__label">Оплата</span>
                    <span className={order.paymentStatus === PaymentStatus.PAID ? 'order-card__paid' : 'order-card__unpaid'}>
                        {PAYMENT_STATUS_LABEL[order.paymentStatus]}
                    </span>
                </div>
                {order.orderTime && (
                    <div className="order-card__row">
                        <span className="order-card__label">Дата</span>
                        <span>{formatDateTime(order.orderTime)}</span>
                    </div>
                )}
            </div>

            <div className="order-card__footer">
                <span className="order-card__total">{formatPrice(order.totalPrice)}</span>
                <button className="order-card__details-btn" onClick={() => onDetails(order)}>
                    Подробнее
                </button>
            </div>
        </article>
    );
};

const OrdersSection: React.FC = () => {
    const [orders, setOrders] = useState<OrderDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [selected, setSelected] = useState<OrderDTO | null>(null);
    const [tab, setTab] = useState<'active' | 'history'>('active');

    useEffect(() => {
        orderService.getUserOrders()
            .then(setOrders)
            .catch(() => setError('Не удалось загрузить заказы'))
            .finally(() => setLoading(false));
    }, []);

    const activeOrders  = orders.filter(o => ACTIVE_STATUSES.includes(o.status));
    const historyOrders = orders.filter(o => !ACTIVE_STATUSES.includes(o.status)
        && o.status!==OrderStatus.IN_CART);
    const displayed     = tab === 'active' ? activeOrders : historyOrders;

    return (
        <section className="orders-section">
            <div className="orders-section__inner">

                <aside className="orders-sidebar">
                    <div className="orders-sidebar__icon">🍽</div>
                    <h2 className="orders-sidebar__title">Мои заказы</h2>
                    <p className="orders-sidebar__sub">История и активные заявки</p>

                    <nav className="orders-sidebar__nav">
                        <button
                            className={`orders-sidebar__nav-btn ${tab === 'active' ? 'orders-sidebar__nav-btn--active' : ''}`}
                            onClick={() => setTab('active')}
                        >
                            <span className="orders-sidebar__nav-dot" />
                            Активные
                            {activeOrders.length > 0 && (
                                <span className="orders-sidebar__count">{activeOrders.length}</span>
                            )}
                        </button>
                        <button
                            className={`orders-sidebar__nav-btn ${tab === 'history' ? 'orders-sidebar__nav-btn--active' : ''}`}
                            onClick={() => setTab('history')}
                        >
                            <span className="orders-sidebar__nav-dot orders-sidebar__nav-dot--muted" />
                            История
                            {historyOrders.length > 0 && (
                                <span className="orders-sidebar__count orders-sidebar__count--muted">{historyOrders.length}</span>
                            )}
                        </button>
                    </nav>
                </aside>

                {/* ── Content ── */}
                <div className="orders-content">
                    <header className="orders-content__header">
                        <h1 className="orders-content__title">
                            {tab === 'active' ? 'Активные заказы' : 'История заказов'}
                        </h1>
                        <p className="orders-content__sub">
                            {tab === 'active'
                                ? 'Заказы в обработке и на кухне'
                                : 'Выполненные, отменённые и отклонённые заказы'}
                        </p>
                    </header>

                    {loading && (
                        <div className="orders-state">
                            <span className="orders-spinner" />
                            <p>Загружаем заказы…</p>
                        </div>
                    )}

                    {!loading && error && (
                        <div className="orders-state orders-state--error">{error}</div>
                    )}

                    {!loading && !error && displayed.length === 0 && (
                        <div className="orders-state orders-state--empty">
                            <span className="orders-state__icon">{tab === 'active' ? '⏳' : '📋'}</span>
                            <p>{tab === 'active' ? 'Нет активных заказов' : 'История пуста'}</p>
                        </div>
                    )}

                    {!loading && !error && displayed.length > 0 && (
                        <div className="orders-grid">
                            {displayed.map(order => (
                                <OrderCard key={order.id} order={order} onDetails={setSelected} />
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {selected && (
                <DetailModal order={selected} onClose={() => setSelected(null)} />
            )}
        </section>
    );
};

export default OrdersSection;