import React, { useState, useEffect, useCallback } from 'react';
import '../styles/CartSection.css';
import { orderService } from '../api/orderservice.ts';
import { type FullOrderDTO } from '../models/types/order.types.ts';
import type { MenuItemDTO } from '../models/types/menu.types.ts';
import { compositeservice } from '../api/compositeservice.ts';
import { type CreateOrderDTO, OrderType } from '../models/types/order.types.ts';
import { PaymentType } from '../models/types/payment.types.ts';
import { userService } from '../api/userservice.ts';
import { useAuth } from '../context/AuthContext';

interface CartSectionProps {
    onOrderCreated?: () => void;
}

const CartSection: React.FC<CartSectionProps> = ({ onOrderCreated }) => {
    const { user } = useAuth();

    const [fullCart, setFullCart] = useState<FullOrderDTO | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [isActionLoading, setIsActionLoading] = useState<boolean>(false);

    const [orderType, setOrderType] = useState<OrderType>(OrderType.IN_RESTAURANT);
    const [paymentType, setPaymentType] = useState<PaymentType>(PaymentType.CARD);
    const [userRequest, setUserRequest] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [orderError, setOrderError] = useState<string | null>(null);

    const [roomNumber, setRoomNumber] = useState<string | null>(null);
    const [roomLoading, setRoomLoading] = useState(false);

    const fetchCart = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await compositeservice.getCart();
            setFullCart(data);
        } catch (err) {
            console.error(err);
            setError('Не удалось загрузить корзину');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchCart();
    }, [fetchCart]);

    useEffect(() => {
        const roomFromCtx = (user as any)?.roomNumber ?? (user as any)?.hotelRoom ?? null;
        if (roomFromCtx) {
            setRoomNumber(String(roomFromCtx));
            return;
        }
        setRoomLoading(true);
        userService
            .getMyRoomNumber()
            .then((num) => setRoomNumber(num))
            .finally(() => setRoomLoading(false));
    }, [user]);

    const refreshCart = async () => {
        await fetchCart();
    };

    const handleIncrement = async (menuItemId: number) => {
        setIsActionLoading(true);
        try {
            await orderService.addToCart(menuItemId);
            await refreshCart();
        } catch (err) {
            console.error(err);
            setError('Не удалось добавить товар');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleDecrement = async (menuItemId: number) => {
        setIsActionLoading(true);
        try {
            await orderService.removeFromCart(menuItemId);
            await refreshCart();
        } catch (err) {
            console.error(err);
            setError('Не удалось удалить товар');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleRemoveItem = async (menuItemId: number) => {
        setIsActionLoading(true);
        try {
            await orderService.removeFromCart(menuItemId);
            await refreshCart();
        } catch (err) {
            console.error(err);
            setError('Не удалось удалить товар');
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleSubmit = async () => {
        setOrderError(null);
        setIsSubmitting(true);
        const orderData: CreateOrderDTO = {
            orderType,
            totalPrice,
            paymentType,
            userRequest: userRequest.trim() || undefined,
        };
        try {
            await orderService.createOrder(orderData);
            await refreshCart();
            onOrderCreated?.();
        } catch (err) {
            console.error(err);
            setOrderError('Не удалось оформить заказ. Попробуйте ещё раз.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const menuItemMap = new Map<number, MenuItemDTO>();
    if (fullCart?.menuItem) {
        fullCart.menuItem.forEach((item) => menuItemMap.set(item.id, item));
    }

    const cartItems = fullCart?.order?.items;
    const itemsArray = Array.isArray(cartItems) ? cartItems : [];

    const totalPrice = itemsArray.reduce((sum, item) => {
        const menuItem = menuItemMap.get(item.menuItemId);
        return sum + (menuItem?.price ?? 0) * item.quantity;
    }, 0);

    const totalCount = itemsArray.reduce((sum, item) => sum + item.quantity, 0);

    if (loading && !fullCart) {
        return <div className="cart-loading">Загрузка корзины...</div>;
    }

    if (error && !fullCart) {
        return <div className="cart-error">Ошибка: {error}</div>;
    }

    const isEmpty = itemsArray.length === 0;

    return (
        <div className="cart-page-inner">
            <div className="cart-page-title">
                <h1>Корзина</h1>
                <p>Оформите ваш заказ</p>
            </div>

            {isEmpty ? (
                <div className="empty-cart">
                    <p>Корзина пуста</p>
                    <button className="continue-shopping" onClick={() => (window.location.href = '/menu')}>
                        Перейти в меню
                    </button>
                </div>
            ) : (
                <div className="cart-layout">
                    <div className="cart-items-col">
                        {error && <p className="cart-inline-error">{error}</p>}
                        <div className="cart-items">
                            {itemsArray.map((item) => {
                                const menuItem = menuItemMap.get(item.menuItemId);
                                if (!menuItem) return null;
                                const itemTotal = menuItem.price * item.quantity;
                                return (
                                    <div key={item.menuItemId} className="cart-item">
                                        <div className="item-image">
                                            <img src={menuItem.imageUrl} alt={menuItem.name} />
                                        </div>
                                        <div className="item-details">
                                            <div className="item-info">
                                                <span className="item-name">{menuItem.name}</span>
                                                <span className="item-category">{menuItem.category.name}</span>
                                            </div>
                                            <div className="item-actions">
                                                <button
                                                    className="quantity-btn"
                                                    onClick={() => handleDecrement(item.menuItemId)}
                                                    disabled={isActionLoading}
                                                >
                                                    −
                                                </button>
                                                <span className="item-quantity">{item.quantity}</span>
                                                <button
                                                    className="quantity-btn"
                                                    onClick={() => handleIncrement(item.menuItemId)}
                                                    disabled={isActionLoading}
                                                >
                                                    +
                                                </button>
                                            </div>
                                        </div>
                                        <div className="item-right">
                                            <button
                                                className="remove-btn"
                                                onClick={() => handleRemoveItem(item.menuItemId)}
                                                disabled={isActionLoading}
                                                aria-label="Удалить"
                                            >
                                                🗑
                                            </button>
                                            <span className="item-total">{itemTotal} BYN</span>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {/* RIGHT: Order panel */}
                    <div className="cart-order-col">
                        <div className="order-panel">
                            <h2 className="order-panel-title">Оформление заказа</h2>

                            <div className="order-panel-group">
                                <label className="order-panel-label">Тип заказа</label>
                                <div className="type-toggle">
                                    <button
                                        type="button"
                                        className={`toggle-btn ${orderType === OrderType.DELIVERY_TO_HOTEL_NUMBER ? 'active' : ''} ${!roomNumber ? 'disabled-btn' : ''}`}
                                        onClick={() => roomNumber && setOrderType(OrderType.DELIVERY_TO_HOTEL_NUMBER)}
                                        disabled={!roomNumber || roomLoading}
                                        title={!roomNumber && !roomLoading ? 'Укажите номер комнаты в профиле' : ''}
                                    >
                                        Доставка
                                    </button>
                                    <button
                                        type="button"
                                        className={`toggle-btn ${orderType === OrderType.IN_RESTAURANT ? 'active' : ''}`}
                                        onClick={() => setOrderType(OrderType.IN_RESTAURANT)}
                                    >
                                        В ресторане
                                    </button>
                                </div>
                            </div>

                            {orderType === OrderType.DELIVERY_TO_HOTEL_NUMBER && roomNumber && (
                                <div className="order-panel-group">
                                    <label className="order-panel-label">Номер комнаты</label>
                                    <input
                                        className="order-panel-input"
                                        value={roomNumber}
                                        readOnly
                                        placeholder="Введите номер комнаты"
                                    />
                                </div>
                            )}

                            {!roomLoading && !roomNumber && (
                                <p className="order-panel-hint">
                                    Чтобы заказать доставку в номер, укажите номер комнаты в{' '}
                                    <a href="/profile">профиле</a>.
                                </p>
                            )}

                            <div className="order-panel-group">
                                <label className="order-panel-label">Комментарий к заказу</label>
                                <textarea
                                    className="order-panel-textarea"
                                    value={userRequest}
                                    onChange={(e) => setUserRequest(e.target.value)}
                                    placeholder="Пожелания к заказу..."
                                    rows={3}
                                    maxLength={300}
                                />
                            </div>

                            <div className="order-panel-group">
                                <label className="order-panel-label">Способ оплаты</label>
                                <div className="type-toggle">
                                    <button
                                        type="button"
                                        className={`toggle-btn ${paymentType === PaymentType.CARD ? 'active' : ''}`}
                                        onClick={() => setPaymentType(PaymentType.CARD)}
                                    >
                                        💳 Карта
                                    </button>
                                    <button
                                        type="button"
                                        className={`toggle-btn ${paymentType === PaymentType.CASH ? 'active' : ''}`}
                                        onClick={() => setPaymentType(PaymentType.CASH)}
                                    >
                                        💵 Наличные
                                    </button>
                                </div>
                            </div>

                            <div className="order-panel-summary">
                                <div className="summary-row">
                                    <span>Позиций в корзине:</span>
                                    <span>{totalCount}</span>
                                </div>
                                <div className="summary-row total-row">
                                    <span>Итого:</span>
                                    <span>{totalPrice} BYN</span>
                                </div>
                            </div>

                            {orderError && <p className="order-panel-error">{orderError}</p>}

                            <button
                                className="checkout-btn"
                                onClick={handleSubmit}
                                disabled={isSubmitting || isEmpty}
                            >
                                {isSubmitting ? 'Оформляем...' : 'Оформить заказ'}
                            </button>

                            <p className="order-panel-note">После оформления заказ будет передан на кухню</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CartSection;