import React, { useEffect, useState } from 'react';
import { paymentService } from '../../api/paymentservice.ts';
import type { PaymentDTO } from '../../models/types/payment.types.ts';
import '../../styles/admin/OperationsTables.css';

const paymentStatusLabels: Record<string, { label: string; className: string }> = {
    PAID:     { label: 'Оплачен',    className: 'pay-paid' },
    UNPAID:   { label: 'Не оплачен', className: 'pay-unpaid' },
    REFUNDED: { label: 'Возврат',    className: 'pay-refunded' },
};

const paymentTypeLabels: Record<string, string> = {
    CASH:   'Наличные',
    CARD:   'Заказ',
    WALLET: 'Кошелёк',
    REPLENISHMENT: 'Пополнение',
};

const PaymentsTable: React.FC = () => {
    const [payments, setPayments] = useState<PaymentDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPayments = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await paymentService.getAllPayments();
            setPayments(data);
        } catch (err) {
            setError('Не удалось загрузить платежи');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPayments();
    }, []);

    const formatDate = (dateStr: string) => {
        if (!dateStr) return '—';
        try {
            const date = new Date(dateStr);
            // Проверяем, валидна ли дата
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

    if (loading && payments.length === 0) {
        return (
            <div className="ops-section ops-section--loading">
                <div className="ops-spinner" />
                <p>Загрузка платежей...</p>
            </div>
        );
    }

    if (error && payments.length === 0) {
        return (
            <div className="ops-section ops-section--error">
                <p className="ops-error-msg">{error}</p>
                <button onClick={fetchPayments} className="ops-retry-btn">Повторить</button>
            </div>
        );
    }

    return (
        <div className="ops-section">
            <div className="ops-header">
                <div className="ops-title-group">
                    <h2 className="ops-title">💳 Платежи</h2>
                    <span className="ops-count">{payments.length} записей</span>
                </div>
                <button onClick={fetchPayments} className="ops-refresh-btn">🔄 Обновить</button>
            </div>

            {error && <div className="ops-error-inline">{error}</div>}

            <div className="table-responsive">
                <table className="ops-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Пользователь</th>
                        <th>Сумма</th>
                        <th>Тип</th>
                        <th>Статус</th>
                        <th>Заказ</th>
                        <th>Дата</th>
                    </tr>
                    </thead>
                    <tbody>
                    {payments.length === 0 ? (
                        <tr>
                            <td colSpan={7} className="ops-empty">Платежи не найдены</td>
                        </tr>
                    ) : (
                        payments.map((payment) => {
                            const status = paymentStatusLabels[payment.paymentStatus] ?? { label: payment.paymentStatus, className: '' };
                            const typeLabel = paymentTypeLabels[payment.paymentType] ?? payment.paymentType;
                            return (
                                <tr key={payment.id}>
                                    <td className="ops-id">#{payment.id}</td>
                                    <td className="ops-muted">{payment.userId}</td>
                                    <td className="ops-price">
                                        {payment.totalAmount.toLocaleString('ru-RU')} BYN
                                    </td>
                                    <td>
                                        <span className="ops-type-badge">{typeLabel}</span>
                                    </td>
                                    <td>
                                            <span className={`ops-badge ${status.className}`}>
                                                {status.label}
                                            </span>
                                    </td>
                                    <td className="ops-muted">{payment.orderId ?? '—'}</td>
                                    <td className="ops-muted">{formatDate(payment.paymentDate)}</td>
                                </tr>
                            );
                        })
                    )}
                    </tbody>
                </table>
            </div>

            <div className="ops-footer">
                Показано: {payments.length} платежей
            </div>
        </div>
    );
};

export default PaymentsTable;