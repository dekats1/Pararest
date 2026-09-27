import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export interface AnalyticsDTO {
    totalRevenue: number;
    totalExpenses: number;
    netProfit: number;
    totalOrders: number;
    averageCheck: number;
    topDishName: string;
    topDishCount: number;
}

export const analyticsService = {
    async getMonthlyAnalytics(): Promise<AnalyticsDTO> {
        try {
            const response = await api.get<AnalyticsDTO>('/analytics/monthly');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении аналитики:', error);
            throw error;
        }
    },
};