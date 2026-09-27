import axios from 'axios';
import type { AddIngredientDTO, IngredientDTO } from '../models/IngredientDTO';
import type { AddSupplyDTO, SupplierDTO, SupplyDTO } from '../models/SupplyDTO';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const ingredientService = {
    async getAllIngredients(): Promise<IngredientDTO[]> {
        try {
            const response = await api.get<IngredientDTO[]>('/ingredients/getAll');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении ингредиентов:', error);
            throw error;
        }
    },

    async addIngredient(data: AddIngredientDTO): Promise<IngredientDTO> {
        try {
            const response = await api.post<IngredientDTO>('/ingredients/addIngredient', data);
            return response.data;
        } catch (error) {
            console.error('Ошибка при добавлении ингредиента:', error);
            throw error;
        }
    },

    async deleteIngredient(id: number): Promise<void> {
        try {
            await api.delete(`/ingredients/deleteIngredient/${id}`);
        } catch (error) {
            console.error('Ошибка при удалении ингредиента:', error);
            throw error;
        }
    },

    async getAllSupplies(): Promise<SupplyDTO[]> {
        try {
            const response = await api.get<SupplyDTO[]>('/supplies/getAll');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении поставок:', error);
            throw error;
        }
    },

    async createSupplyRequest(request: AddSupplyDTO): Promise<SupplyDTO> {
        try {
            const response = await api.post<SupplyDTO>('/supplies/addNew', request);
            return response.data;
        } catch (error) {
            console.error('Ошибка при создании запроса на поставку:', error);
            throw error;
        }
    },

    async getSuppliers(): Promise<SupplierDTO[]> {
        try {
            const response = await api.get<SupplierDTO[]>('/suppliers/getAll');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении поставщиков:', error);
            throw error;
        }
    }
};