import axios from 'axios';
import { type UserDTO, UserRole } from '../models/UserDTO';
import type { UpdateUserProfileDTO } from '../models/types/user.types.ts';

const API_BASE = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const userService = {
    async getUsers(): Promise<UserDTO[]> {
        try {
            const response = await api.get<UserDTO[]>('/users/getAll');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении пользователей:', error);
            throw error;
        }
    },

    async changeUserRole(userId: string, role: UserRole): Promise<UserDTO> {
        try {
            const response = await api.put<UserDTO>(`/users/changeRole/${userId}`,
                role,
                { headers: { 'Content-Type': 'application/json' } }
            );
            return response.data;
        } catch (error) {
            console.error('Ошибка при изменении роли:', error);
            throw error;
        }
    },

    async toggleUserBlock(userId: string): Promise<UserDTO> {
        try {
            const response = await api.put<UserDTO>(`/users/handleAccess/${userId}`);
            return response.data;
        } catch (error) {
            console.error('Ошибка при изменении статуса блокировки:', error);
            throw error;
        }
    },

    async deleteUser(userId: string): Promise<void> {
        try {
            await api.delete(`/users/deleteUser/${userId}`);
        } catch (error) {
            console.error('Ошибка при удалении пользователя:', error);
            throw error;
        }
    },

    async getMyRoomNumber(): Promise<string | null> {
        try {
            const response = await api.get<number>('/users/getMyRoomNumber');
            const room = response.data;
            return room && room !== 0 ? String(room) : null;
        } catch (error) {
            console.error('Ошибка при получении номера комнаты:', error);
            return null;
        }
    },

    async updateProfile(data: UpdateUserProfileDTO): Promise<UserDTO> {
        try {
            const response = await api.put<UserDTO>('/users/updateProfile', data);
            return response.data;
        } catch (error) {
            console.error('Ошибка при обновлении профиля:', error);
            throw error;
        }
    },

    async getMyInfo(): Promise<UserDTO[]> {
        try {
            const response = await api.get<UserDTO[]>('/users/getInfo');
            return response.data;
        } catch (error) {
            console.error('Ошибка при получении пользователей:', error);
            throw error;
        }
    },
};