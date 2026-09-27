import type { RegisterRequest, JwtResponse, LoginRequest } from '../models/types/auth.types.ts';
import type { ErrorResponse } from '../models/ErrorResponse.ts';
import type { UserDTO } from '../models/UserDTO.ts';

const API_BASE_URL = 'http://localhost:8080';
const GET_INFO_URL = '/api/v1/users/getInfo';
const AUTH_ROUTES = '/api/v1/auth';

class AuthService {
    async register(data: RegisterRequest): Promise<JwtResponse> {
        const response = await fetch(`${API_BASE_URL}${AUTH_ROUTES}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        });

        if (!response.ok) {
            const error: ErrorResponse = await response.json();
            throw new Error(error.message || 'Ошибка регистрации');
        }

        const jwtResponse: JwtResponse = await response.json();

        if (jwtResponse.accessToken) {
            this.setToken(jwtResponse.accessToken);
        }
        if (jwtResponse.refreshToken) {
            this.setRefreshToken(jwtResponse.refreshToken);
        }

        return jwtResponse;
    }

    async login(data: LoginRequest): Promise<JwtResponse> {
        const response = await fetch(`${API_BASE_URL}${AUTH_ROUTES}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        });

        if (!response.ok) {
            const error: ErrorResponse = await response.json();
            throw new Error(error.message || 'Ошибка входа');
        }

        const jwtResponse: JwtResponse = await response.json();

        if (jwtResponse.accessToken) {
            this.setToken(jwtResponse.accessToken);
        }
        if (jwtResponse.refreshToken) {
            this.setRefreshToken(jwtResponse.refreshToken);
        }

        return jwtResponse;
    }

    async getCurrentUser(): Promise<UserDTO> {
        const token = this.getToken();
        if (!token) {
            throw new Error('Токен не найден');
        }

        const response = await fetch(`${API_BASE_URL}${GET_INFO_URL}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            if (response.status === 401) {
                this.removeTokens();
            }
            const error: ErrorResponse = await response.json();
            throw new Error(error.message || 'Не удалось получить данные пользователя');
        }

        return await response.json();
    }

    async refreshToken(refreshToken: string): Promise<JwtResponse> {
        const response = await fetch(`${API_BASE_URL}${AUTH_ROUTES}/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken }),
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Не удалось обновить токен');
        }

        const jwtResponse: JwtResponse = await response.json();

        if (jwtResponse.accessToken) {
            this.setToken(jwtResponse.accessToken);
        }
        if (jwtResponse.refreshToken) {
            this.setRefreshToken(jwtResponse.refreshToken);
        }

        return jwtResponse;
    }

    private setCookie(name: string, value: string, maxAgeSeconds: number = 86400): void {
        const cookieValue = encodeURIComponent(value);
        document.cookie = `${name}=${cookieValue}; path=/; max-age=${maxAgeSeconds}; SameSite=Lax`;
    }

    private getCookie(name: string): string | null {
        const cookies = document.cookie.split('; ');
        for (const cookie of cookies) {
            const [cookieName, cookieValue] = cookie.split('=');
            if (cookieName === name) {
                return decodeURIComponent(cookieValue);
            }
        }
        return null;
    }

    private removeCookie(name: string): void {
        document.cookie = `${name}=; path=/; max-age=0; SameSite=Lax`;
    }

    setToken(token: string): void {
        localStorage.setItem('access_token', token);
        this.setCookie('auth_token', token, 86400);
        this.setCookie('access_token', token, 86400);
    }

    setRefreshToken(token: string): void {
        localStorage.setItem('refresh_token', token);
        this.setCookie('refresh_token', token, 604800);
    }

    removeTokens(): void {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        this.removeCookie('auth_token');
        this.removeCookie('access_token');
        this.removeCookie('refresh_token');
    }

    getToken(): string | null {
        const cookieToken = this.getCookie('auth_token') || this.getCookie('access_token');
        if (cookieToken) {
            return cookieToken;
        }
        return localStorage.getItem('access_token');
    }

    getRefreshToken(): string | null {
        const cookieRefreshToken = this.getCookie('refresh_token');
        if (cookieRefreshToken) {
            return cookieRefreshToken;
        }
        return localStorage.getItem('refresh_token');
    }
}

export default new AuthService();