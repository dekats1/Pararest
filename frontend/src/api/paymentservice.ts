import axios from 'axios';
import type { CreateReplenishment, PaymentDTO } from '../models/types/payment.types.ts';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const paymentService = {
    getAllPayments: async (): Promise<PaymentDTO[]> => {
        const response = await api.get<PaymentDTO[]>('/payments/getAllPayments');
        return response.data;
    },

    getUserPayments: async (): Promise<PaymentDTO[]> => {
        const response = await api.get<PaymentDTO[]>('/payments/getUserPayments');
        return response.data;
    },

    newReplenishment: async (data: CreateReplenishment): Promise<PaymentDTO> => {
        const response = await api.post<PaymentDTO>('/payments/newReplenishment', data);
        return response.data;
    },
};