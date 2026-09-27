import axios from 'axios';
import type {CategoryDTO, MenuItemDTO as PublicMenuItemDTO} from "../models/types/menu.types.ts";
import type {MenuItemDTO as AdminMenuItemDTO} from "../models/MenuItemDTO.ts";

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export interface MenuCategoryDTO{
    id: number,
    name: string
}

export interface AddMenuItemDTO {
    name: string;
    description: string;
    price: number;
    category: MenuCategoryDTO;
    imageUrl?: string | null;
    preparationTime?: number | null
}

export interface MenuItemFormData {
    name: string;
    description: string;
    price: number;
    category: string;
    recipe?: string | null;
    imageUrl?: string | null;
    active: boolean;
}

const toAddMenuItemDto = (data: MenuItemFormData): AddMenuItemDTO => ({
    name: data.name,
    description: data.description,
    price: data.price,
    category: { id: 0, name: data.category },
    imageUrl: data.imageUrl ?? null,
    preparationTime: null
});

const toAdminMenuItem = (item: PublicMenuItemDTO): AdminMenuItemDTO => ({
    id: item.id,
    name: item.name,
    description: item.description,
    price: item.price,
    category: item.category.name?? '',
    imageUrl: item.imageUrl,
    active: item.active,
    adderName: item.adderName,
    createdAt: new Date().toISOString()
});

export const menuService = {
    async getAllMenuItems(): Promise<PublicMenuItemDTO[]> {
        try {
            const response = await api.get<PublicMenuItemDTO[]>('/menu/getAll');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении меню:', error);
            throw error;
        }
    },

    async getAllMenuItemsForAdmin(): Promise<AdminMenuItemDTO[]> {
        const items = await this.getAllMenuItems();
        return items.map(toAdminMenuItem);
    },

    async createMenuItem(data: MenuItemFormData): Promise<AdminMenuItemDTO> {
        try {
            const response = await api.post<PublicMenuItemDTO>('/menu/addNewMenuItemNoRecipe', toAddMenuItemDto(data));
            return toAdminMenuItem(response.data);
        } catch (error) {
            console.error('Ошибка при создании позиции меню:', error);
            throw error;
        }
    },

    async updateMenuItem(id: number, data: MenuItemFormData): Promise<AdminMenuItemDTO> {
        try {
            const response = await api.put<PublicMenuItemDTO>(`/menu/editMenuItem/${id}`, toAddMenuItemDto(data));
            return toAdminMenuItem(response.data);
        } catch (error) {
            console.error('Ошибка при обновлении позиции меню:', error);
            throw error;
        }
    },

    async deleteMenuItem(id: number): Promise<void> {
        try {
            await api.delete(`/menu/deleteMenuItem/${id}`);
        } catch (error) {
            console.error('Ошибка при удалении позиции меню:', error);
            throw error;
        }
    },

    async toggleActive(id: number): Promise<AdminMenuItemDTO> {
        try {
            const response = await api.put<PublicMenuItemDTO>(`/menu/editAvailability/${id}`);
            return toAdminMenuItem(response.data);
        } catch (error) {
            console.error('Ошибка при изменении статуса активности:', error);
            throw error;
        }
    },

    async getAllCategories(): Promise<CategoryDTO[]>{
        try{
            const response = await api.get<CategoryDTO[]>(`/categories/getAll`);
            return response.data;
        }catch (error) {
            console.error('Ошибка при изменении статуса активности:', error);
            throw error;
        }
    },

    async getMenuItemsByIds(ids: number[]): Promise<PublicMenuItemDTO[]> {
        try {
            const response = await api.request<PublicMenuItemDTO[]>({
                method: 'GET',
                url: '/menu/getMenuItemsByIds',
                data: ids
            });
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении позиций по ids:', error);
            throw error;
        }
    }
};