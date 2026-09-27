import type {AddMenuItemDTO, AddMenuItemRequest} from "../models/MenuItemDTO.ts";
import axios from "axios";
import type {FullOrderDTO} from "../models/types/order.types.ts";
const API_BASE = 'http://localhost:8080/api/v1/composite';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});
export const compositeservice = {
    async createMenuItemWithRecipe(formData: AddMenuItemDTO, ingredientsMap: Record<number, number>): Promise<boolean> {
        try {
            const itemDTO: AddMenuItemDTO = {
                name: formData.name,
                description: formData.description,
                price: formData.price,
                category: formData.category,
                imageUrl: formData.imageUrl || null,
                isActive: formData.isActive
            };

            const request: AddMenuItemRequest = {
                item: itemDTO,
                ingredientsMap: ingredientsMap
            };

            const response = await api.post<boolean>('/menu/addNewDish', request);
            return response.data;
        } catch (error) {
            console.error('Ошибка при создании блюда с рецептом:', error);
            throw error;
        }
    },

    async getCart(): Promise<FullOrderDTO> {
        try {
            const response = await api.get<FullOrderDTO>('/orders/cart');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении корзины:', error);
            throw error;
        }
    }
}