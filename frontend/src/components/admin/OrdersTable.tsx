import React, { useEffect, useState } from 'react';
import { orderService } from '../../api/orderservice.ts';
import { type OrderDTO } from '../../models/types/order.types.ts';
import '../../styles/admin/OperationsTables.css';

const statusLabels: Record<string, { label: string; className: string }> = {
    PENDING:    { label: 'Ожидает',    className: 'status-pending' },
    PROCESSING: { label: 'В обработке', className: 'status-processing' },
    COMPLETED:  { label: 'Выполнен',   className: 'status-completed' },
    CANCELLED:  { label: 'Отменён',    className: 'status-cancelled' },
};

const paymentStatusLabels: Record<string, { label: string; className: string }> = {
    PAID:    { label: 'Оплачен',  className: 'pay-paid' },
    UNPAID:  { label: 'Не оплачен', className: 'pay-unpaid' },
    REFUNDED:{ label: 'Возврат',  className: 'pay-refunded' },
};

const OrdersTable: React.FC = () => {
    const [orders, setOrders] = useState<OrderDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchOrders = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await orderService.getAllOrders();
            setOrders(data);
        } catch (err) {
            setError('Не удалось загрузить заказы');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const formatDate = (dateStr?: string) => {
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

    if (loading && orders.length === 0) {
        return (
            <div className="ops-section ops-section--loading">
                <div className="ops-spinner" />
                <p>Загрузка заказов...</p>
            </div>
        );
    }

    if (error && orders.length === 0) {
        return (
            <div className="ops-section ops-section--error">
                <p className="ops-error-msg">{error}</p>
                <button onClick={fetchOrders} className="ops-retry-btn">Повторить</button>
            </div>
        );
    }

    return (
        <div className="ops-section">
            <div className="ops-header">
                <div className="ops-title-group">
                    <h2 className="ops-title">🧾 Заказы</h2>
                    <span className="ops-count">{orders.length} записей</span>
                </div>
                <button onClick={fetchOrders} className="ops-refresh-btn">🔄 Обновить</button>
            </div>

            {error && <div className="ops-error-inline">{error}</div>}

            <div className="table-responsive">
                <table className="ops-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Пользователь</th>
                        <th>Статус</th>
                        <th>Тип</th>
                        <th>Сумма</th>
                        <th>Оплата</th>
                        <th>Позиций</th>
                        <th>Время заказа</th>
                    </tr>
                    </thead>
                    <tbody>
                    {orders.length === 0 ? (
                        <tr>
                            <td colSpan={8} className="ops-empty">Заказы не найдены</td>
                        </tr>
                    ) : (
                        orders.map((order) => {
                            const status = statusLabels[order.status] ?? { label: order.status, className: '' };
                            const payStatus = paymentStatusLabels[order.paymentStatus] ?? { label: order.paymentStatus, className: '' };
                            return (
                                <tr key={order.id}>
                                    <td className="ops-id">#{order.id}</td>
                                    <td className="ops-muted">{order.userId}</td>
                                    <td>
                                            <span className={`ops-badge ${status.className}`}>
                                                {status.label}
                                            </span>
                                    </td>
                                    <td className="ops-muted">{order.type ?? '—'}</td>
                                    <td className="ops-price">
                                        {order.totalPrice != null
                                            ? order.totalPrice.toLocaleString('ru-RU') + ' BYN'
                                            : '—'}
                                    </td>
                                    <td>
                                        <div className="ops-pay-cell">
                                            <span className="ops-muted">{order.paymentType}</span>
                                            <span className={`ops-badge ${payStatus.className}`}>
                                                    {payStatus.label}
                                                </span>
                                        </div>
                                    </td>
                                    <td className="ops-center">{order.items?.length ?? 0}</td>
                                    <td className="ops-muted">{formatDate(order.orderTime)}</td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>

            <div className="ops-footer">
                Показано: {orders.length} заказов
            </div>
        </div>
    );
};

export default OrdersTable;