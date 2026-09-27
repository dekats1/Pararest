import { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import authService from "../api/authservice.ts";
import type { UserDTO } from "../models/UserDTO.ts";
import type { LoginRequest, RegisterRequest } from "../models/types/auth.types.ts";

interface AuthContextType {
    user: UserDTO | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    userRole: string | null;
    login: (data: LoginRequest) => Promise<void>;
    logout: () => void;
    register: (data: RegisterRequest) => Promise<void>;
    updateUser: (userData: Partial<UserDTO>) => void;
    refreshAuth: () => Promise<void>;
    hasRole: (roles: string | string[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const roleRoutes: Record<string, string> = {
    'ADMIN': '/admin/dashboard',
    'MANAGER': '/manager/dashboard',
    'EMPLOYEE': '/employee/dashboard',
    'USER': '/home'
};

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<UserDTO | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    const loadUser = async (): Promise<UserDTO | null> => {
        try {
            const userData = await authService.getCurrentUser();
            setUser(userData);
            return userData;
        } catch (error) {
            setUser(null);
            return null;
        }
    };

    const redirectByRole = (role: string) => {
        const route = roleRoutes[role] || '/home';
        if (window.location.pathname !== route) {
            navigate(route, { replace: true });
        }
    };

    useEffect(() => {
        const initializeAuth = async () => {
            try {
                const token = authService.getToken();
                if (token) {
                    await loadUser();
                }
            } catch (error) {
                console.error('Auth initialization failed:', error);
            } finally {
                setIsLoading(false);
            }
        };

        initializeAuth();
    }, []);

    useEffect(() => {
        if (user?.role && !isLoading) {
            redirectByRole(user.role);
        }
    }, [user, isLoading]);

    const login = async (data: LoginRequest): Promise<void> => {
        setIsLoading(true);
        try {
            await authService.login(data);
            const userData = await loadUser();
            if(userData?.blocked){
                authService.removeTokens();
                setUser(null);
                window.location.href = "/blocked";
                return ;
            }
            if (userData) {
                redirectByRole(userData.role);
            }

        } catch (error) {
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (data: RegisterRequest): Promise<void> => {
        setIsLoading(true);
        try {
            await authService.register(data);
            const userData = await loadUser();
            if (userData) {
                redirectByRole(userData.role);
            }
        } catch (error) {
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const logout = () => {
        authService.removeTokens();
        setUser(null);
        navigate('/home', { replace: true });
    };

    const updateUser = (userData: Partial<UserDTO>) => {
        if (user) {
            setUser({ ...user, ...userData });
        }
    };

    const refreshAuth = async (): Promise<void> => {
        try {
            const refreshToken = authService.getRefreshToken();
            if (!refreshToken) {
                throw new Error('Refresh token not found');
            }
            await authService.refreshToken(refreshToken);
            await loadUser();
        } catch (error) {
            logout();
        }
    };

    const hasRole = (roles: string | string[]): boolean => {
        if (!user?.role) return false;
        if (Array.isArray(roles)) {
            return roles.includes(user.role);
        }
        return user.role === roles;
    };

    return (
        <AuthContext.Provider value={{
            user,
            isAuthenticated: !!user,
            isLoading,
            userRole: user?.role || null,
            login,
            logout,
            register,
            updateUser,
            refreshAuth,
            hasRole
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};