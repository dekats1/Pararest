import axios from "axios";
import type {RecipeDTO} from "../models/types/recipe.types.ts";
const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const inventoryservice = {
    async getMenuItemRecipe(id: number): Promise<RecipeDTO[]> {
        try {
            const response = await api.get<RecipeDTO[]>(`/recipes/getRecipeByMenuId/${id}`);
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении рецепта:', error);
            throw error;
        }
    },
};