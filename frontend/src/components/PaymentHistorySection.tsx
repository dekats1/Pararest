import React, { useEffect, useState, useCallback } from "react";
import type { PaymentDTO } from "../models/types/payment.types.ts";
import "../styles/PaymentHistorySection.css";
import {paymentService} from "../api/paymentservice.ts";

interface Props {
    refreshTrigger?: number;
}

const STATUS_LABEL: Record<string, string> = {
    PENDING: "В ожидании",
    SUCCESS: "Успешно",
    FAILED: "Ошибка",
    CANCELLED: "Отменён",
};

const STATUS_CLASS: Record<string, string> = {
    PENDING: "status-pending",
    SUCCESS: "status-success",
    FAILED: "status-failed",
    CANCELLED: "status-cancelled",
};

const TYPE_LABEL: Record<string, string> = {
    CARD: "Карта",
    CRYPTO: "Крипто",
    BANK_TRANSFER: "Перевод",
};

function formatDate(dateStr: string): string {
    if (!dateStr) return "—";
    const d = new Date(dateStr);
    return d.toLocaleDateString("ru-RU", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
}

const PaymentHistorySection: React.FC<Props> = ({ refreshTrigger }) => {
    const [payments, setPayments] = useState<PaymentDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPayments = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await paymentService.getUserPayments();
            setPayments(data);
        } catch {
            setError("Не удалось загрузить историю платежей.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchPayments();
    }, [fetchPayments, refreshTrigger]);

    return (
        <section className="payment-history-section">
            <div className="history-container">
                <div className="history-header">
                    <h2 className="history-title">История платежей</h2>
                    <button
                        className="history-refresh-btn"
                        onClick={fetchPayments}
                        disabled={loading}
                        title="Обновить"
                    >
                        <svg
                            className={loading ? "spinning" : ""}
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill="none"
                        >
                            <path d="M4 4v5h5M20 20v-5h-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M4 9a8 8 0 0 1 14.93-2.5M20 15a8 8 0 0 1-14.93 2.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </button>
                </div>

                {error && (
                    <div className="history-error">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                            <circle cx="12" cy="16" r="1" fill="currentColor"/>
                        </svg>
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="history-skeleton-list">
                        {[1, 2, 3].map((i) => (
                            <div className="skeleton-row" key={i}>
                                <div className="skeleton skeleton-icon"/>
                                <div className="skeleton-info">
                                    <div className="skeleton skeleton-line wide"/>
                                    <div className="skeleton skeleton-line narrow"/>
                                </div>
                                <div className="skeleton skeleton-amount"/>
                            </div>
                        ))}
                    </div>
                ) : payments.length === 0 ? (
                    <div className="history-empty">
                        <div className="empty-icon">
                            <svg viewBox="0 0 24 24" fill="none" width="36" height="36">
                                <rect x="2" y="5" width="20" height="16" rx="3" stroke="currentColor" strokeWidth="1.8"/>
                                <path d="M7 10h10M7 14h6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
                            </svg>
                        </div>
                        <p className="empty-text">Платежей пока нет</p>
                        <p className="empty-sub">Пополните кошелёк, чтобы увидеть историю</p>
                    </div>
                ) : (
                    <div className="history-table-wrapper">
                        <table className="history-table">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Дата</th>
                                <th>Тип</th>
                                <th>Сумма</th>
                                <th>Статус</th>
                            </tr>
                            </thead>
                            <tbody>
                            {payments.map((p) => (
                                <tr key={p.id} className="history-row">
                                    <td className="cell-id">
                                        <span className="payment-id">#{p.id}</span>
                                    </td>
                                    <td className="cell-date">{formatDate(p.paymentDate)}</td>
                                    <td className="cell-type">
                                        {TYPE_LABEL[p.paymentType] ?? p.paymentType}
                                    </td>
                                    <td className="cell-amount">
                                            <span className={
                                                p.paymentStatus === "FAILED" || p.paymentStatus === "CANCELLED"
                                                    ? "amount-neutral"
                                                    : p.orderId && p.orderId !== 0 ? "amount-negative" : "amount-positive"
                                            }>
                                                {p.orderId && p.orderId !== 0 ? '-' : '+'}{Number(p.totalAmount).toLocaleString("ru-RU")} BYN
                                            </span>
                                    </td>
                                    <td className="cell-status">
                                            <span className={`status-badge ${STATUS_CLASS[p.paymentStatus] ?? "status-pending"}`}>
                                                {STATUS_LABEL[p.paymentStatus] ?? p.paymentStatus}
                                            </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </section>
    );
};

export default PaymentHistorySection;