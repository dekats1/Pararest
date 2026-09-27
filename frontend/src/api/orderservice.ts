import axios from 'axios';
import type {CreateOrderDTO, OrderDTO} from "../models/types/order.types.ts";

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});


export const orderService = {
    /**
     * Получить все заказы (админ)
     */
    async getAllOrders(): Promise<OrderDTO[]> {
        try {
            const response = await api.get<OrderDTO[]>('/orders');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении всех заказов:', error);
            throw error;
        }
    },

    /**
     * Получить заказ по ID
     */
    async getOrderById(orderId: number): Promise<OrderDTO> {
        try {
            const response = await api.get<OrderDTO>(`/orders/${orderId}`);
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении заказа:', error);
            throw error;
        }
    },

    /**
     * Получить все заказы пользователя
     */
    async getUserOrders(): Promise<OrderDTO[]> {
        const response = await api.get<OrderDTO[]>('/orders/user');
        return response.data;
    },

    /**
     * Создать новый заказ (из корзины)
     */
    async createOrder(data: CreateOrderDTO): Promise<OrderDTO> {
        const response = await api.post<OrderDTO>('/orders', data);
        return response.data;
    },

    /**
     * Добавить блюдо в корзину
     */
    async addToCart(menuItemId: number): Promise<void> {
        try {
            await api.post(`/orders/cart/items/${menuItemId}`);
        } catch (error) {
            console.error('Ошибка при добавлении в корзину:', error);
            throw error;
        }
    },

    /**
     * Удалить блюдо из корзины
     */
    async removeFromCart(menuItemId: number): Promise<void> {
        try {
            await api.delete(`/orders/cart/items/${menuItemId}`);
        } catch (error) {
            console.error('Ошибка при удалении из корзины:', error);
            throw error;
        }
    },

    /**
     * Очистить корзину
     */
    async clearCart(): Promise<void> {
        try {
            await api.delete('/orders/cart');
        } catch (error) {
            console.error('Ошибка при очистке корзины:', error);
            throw error;
        }
    },

    /**
     * Удалить заказ
     */
    async deleteOrder(orderId: number): Promise<void> {
        try {
            await api.delete(`/orders/${orderId}`);
        } catch (error) {
            console.error('Ошибка при удалении заказа:', error);
            throw error;
        }
    },

    /**
     * Отменить заказ
     */
    async cancelOrder(orderId: number): Promise<void> {
        try {
            await api.put(`/orders/cancelOrder/${orderId}`);
        } catch (error) {
            console.error('Ошибка при отмене заказа:', error);
            throw error;
        }
    }
};