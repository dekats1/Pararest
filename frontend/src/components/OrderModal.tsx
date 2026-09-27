import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { orderService } from '../api/orderservice.ts';
import { userService } from '../api/userservice.ts';
import { type CreateOrderDTO, OrderType } from '../models/types/order.types.ts';
import { PaymentType } from '../models/types/payment.types.ts';
import '../styles/OrderModal.css';

interface OrderModalProps {
    isOpen: boolean;
    onClose: () => void;
    totalPrice: number;
    onOrderCreated?: () => void;
}

const OrderModal: React.FC<OrderModalProps> = ({ isOpen, onClose, totalPrice, onOrderCreated }) => {
    const { user } = useAuth();

    const [orderType, setOrderType] = useState<OrderType>(OrderType.IN_RESTAURANT);
    const [paymentType, setPaymentType] = useState<PaymentType>(PaymentType.CARD);
    const [userRequest, setUserRequest] = useState<string>('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [roomNumber, setRoomNumber] = useState<string | null>(null);
    const [roomLoading, setRoomLoading] = useState(false);

    useEffect(() => {
        if (!isOpen) return;

        setOrderType(OrderType.IN_RESTAURANT);
        setPaymentType(PaymentType.CARD);
        setUserRequest('');
        setError(null);

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
    }, [isOpen, user]);

    useEffect(() => {
        if (!roomNumber && orderType === OrderType.DELIVERY_TO_HOTEL_NUMBER) {
            setOrderType(OrderType.IN_RESTAURANT);
        }
    }, [roomNumber]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        const orderData: CreateOrderDTO = {
            orderType,
            totalPrice,
            paymentType,
            userRequest: userRequest.trim() || undefined,
        };

        try {
            await orderService.createOrder(orderData);
            onOrderCreated?.();
            onClose();
        } catch (err) {
            console.error(err);
            setError('Не удалось оформить заказ. Попробуйте ещё раз.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if (e.target === e.currentTarget) onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="modal-backdrop" onClick={handleBackdropClick}>
            <div className="modal-container" role="dialog" aria-modal="true" aria-labelledby="modal-title">
                <div className="modal-header">
                    <h2 id="modal-title">Оформление заказа</h2>
                    <button className="modal-close" onClick={onClose} aria-label="Закрыть">
                        ✕
                    </button>
                </div>

                <div className="modal-price-summary">
                    <span>Сумма заказа</span>
                    <span className="modal-total-price">{totalPrice.toFixed(2)} BYN</span>
                </div>

                <form className="modal-form" onSubmit={handleSubmit}>
                    <div className="modal-form-group">
                        <label className="modal-label">Способ получения</label>
                        <div className="order-type-toggle">
                            <button
                                type="button"
                                className={`toggle-btn ${orderType === OrderType.IN_RESTAURANT ? 'active' : ''}`}
                                onClick={() => setOrderType(OrderType.IN_RESTAURANT)}
                                disabled={isSubmitting}
                            >
                                🍽 В ресторане
                            </button>

                            <button
                                type="button"
                                className={`toggle-btn ${orderType === OrderType.DELIVERY_TO_HOTEL_NUMBER ? 'active' : ''} ${!roomNumber ? 'disabled-btn' : ''}`}
                                onClick={() => roomNumber && setOrderType(OrderType.DELIVERY_TO_HOTEL_NUMBER)}
                                disabled={isSubmitting || roomLoading || !roomNumber}
                                title={!roomNumber && !roomLoading ? 'Укажите номер комнаты в профиле' : ''}
                            >
                                🏨 В номер
                                {roomLoading && <span className="btn-spinner" />}
                            </button>
                        </div>

                        {!roomLoading && !roomNumber && (
                            <p className="modal-hint">
                                Чтобы заказать доставку в номер, укажите номер комнаты в&nbsp;
                                <a href="/profile">профиле</a>.
                            </p>
                        )}

                        {orderType === OrderType.DELIVERY_TO_HOTEL_NUMBER && roomNumber && (
                            <div className="room-badge">
                                Номер комнаты: <strong>{roomNumber}</strong>
                            </div>
                        )}
                    </div>

                    <div className="modal-form-group">
                        <label className="modal-label">Способ оплаты</label>
                        <div className="payment-type-toggle">
                            <button
                                type="button"
                                className={`toggle-btn ${paymentType === PaymentType.CARD ? 'active' : ''}`}
                                onClick={() => setPaymentType(PaymentType.CARD)}
                                disabled={isSubmitting}
                            >
                                💳 Карта
                            </button>
                            <button
                                type="button"
                                className={`toggle-btn ${paymentType === PaymentType.CASH ? 'active' : ''}`}
                                onClick={() => setPaymentType(PaymentType.CASH)}
                                disabled={isSubmitting}
                            >
                                💵 Наличные
                            </button>
                        </div>
                    </div>

                    <div className="modal-form-group">
                        <label className="modal-label" htmlFor="userRequest">
                            Пожелания к заказу <span className="optional">(необязательно)</span>
                        </label>
                        <textarea
                            id="userRequest"
                            className="modal-textarea"
                            value={userRequest}
                            onChange={(e) => setUserRequest(e.target.value)}
                            placeholder="Аллергии, пожелания по подаче..."
                            rows={3}
                            disabled={isSubmitting}
                            maxLength={300}
                        />
                        <span className="char-count">{userRequest.length}/300</span>
                    </div>

                    {error && <p className="modal-error">{error}</p>}

                    <div className="modal-actions">
                        <button
                            type="button"
                            className="btn-cancel"
                            onClick={onClose}
                            disabled={isSubmitting}
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            className="btn-confirm"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <>
                                    <span className="btn-spinner" />
                                    Оформляем...
                                </>
                            ) : (
                                `Подтвердить — ${totalPrice.toFixed(2)} BYN`
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default OrderModal;