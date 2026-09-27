import React, { useEffect, useState } from 'react';
import '../../styles/admin/AnalyticsSection.css';
import {type AnalyticsDTO, analyticsService} from "../../api/analyticsservice.ts";

const AnalyticsSection: React.FC = () => {
    const [data, setData] = useState<AnalyticsDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        analyticsService.getMonthlyAnalytics()
            .then(setData)
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    const fmt = (n: number) =>
        new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'BYN', maximumFractionDigits: 0 }).format(n);

    if (loading) {
        return (
            <div className="analytics-skeleton">
                {[...Array(5)].map((_, i) => (
                    <div key={i} className="skeleton-card" style={{ animationDelay: `${i * 0.1}s` }} />
                ))}
            </div>
        );
    }

    if (error) {
        return (
            <div className="analytics-error">
                <span className="error-icon">⚠</span>
                <p>Не удалось загрузить аналитику</p>
                <span className="error-detail">{error}</span>
            </div>
        );
    }

    if (!data) return null;

    const profitPositive = data.netProfit >= 0;

    return (
        <div className="analytics-root">
            <div className="analytics-header">
                <span className="analytics-label">АНАЛИТИКА</span>
                <span className="analytics-period">текущий месяц</span>
            </div>

            <div className="analytics-grid">
                <div className="metric-card metric-revenue">
                    <div className="metric-icon">↑</div>
                    <div className="metric-body">
                        <span className="metric-title">Выручка</span>
                        <span className="metric-value">{fmt(data.totalRevenue)}</span>
                    </div>
                </div>

                <div className="metric-card metric-expenses">
                    <div className="metric-icon">↓</div>
                    <div className="metric-body">
                        <span className="metric-title">Затраты на поставки</span>
                        <span className="metric-value">{fmt(data.totalExpenses)}</span>
                    </div>
                </div>

                <div className={`metric-card metric-profit ${profitPositive ? 'profit-pos' : 'profit-neg'}`}>
                    <div className="metric-icon">{profitPositive ? '=' : '!'}</div>
                    <div className="metric-body">
                        <span className="metric-title">Чистая прибыль</span>
                        <span className="metric-value">{fmt(data.netProfit)}</span>
                    </div>
                </div>

                <div className="metric-card metric-orders">
                    <div className="metric-icon">#</div>
                    <div className="metric-body">
                        <span className="metric-title">Заказов</span>
                        <span className="metric-value">{data.totalOrders.toLocaleString('ru-RU')}</span>
                    </div>
                </div>

                <div className="metric-card metric-avg">
                    <div className="metric-icon">∅</div>
                    <div className="metric-body">
                        <span className="metric-title">Средний чек</span>
                        <span className="metric-value">{fmt(data.averageCheck)}</span>
                    </div>
                </div>

                <div className="metric-card metric-top-dish">
                    <div className="metric-icon">★</div>
                    <div className="metric-body">
                        <span className="metric-title">Популярное блюдо</span>
                        <span className="metric-value">{data.topDishName || '—'}</span>
                        <span className="metric-subvalue">{(data.topDishCount ?? 0).toLocaleString('ru-RU')} порц.</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AnalyticsSection;