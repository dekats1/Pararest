import authService from './authservice';

let isRefreshing = false;
let failedQueue: Array<{ resolve: (value?: any) => void; reject: (reason?: any) => void }> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

export const fetchWithAuth = async (url: string, options: RequestInit = {}): Promise<Response> => {
    let token = authService.getToken();

    const request = async (): Promise<Response> => {
        const headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        const response = await fetch(url, { ...options, headers });

        if (response.status === 401 && !url.includes('/auth/refresh')) {
            if (!isRefreshing) {
                isRefreshing = true;

                try {
                    const refreshToken = authService.getRefreshToken();
                    if (!refreshToken) throw new Error('No refresh token');

                    const refreshResponse = await authService.refreshToken(refreshToken);
                    authService.setToken(refreshResponse.accessToken);

                    if (refreshResponse.refreshToken) {
                        authService.setRefreshToken(refreshResponse.refreshToken);
                    }

                    isRefreshing = false;
                    processQueue(null, refreshResponse.accessToken);

                    token = refreshResponse.accessToken;
                    const newHeaders = {
                        ...options.headers,
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    };
                    return fetch(url, { ...options, headers: newHeaders });
                } catch (error) {
                    processQueue(error as Error);
                    isRefreshing = false;
                    throw error;
                }
            } else {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(() => {
                    token = authService.getToken();
                    const newHeaders = {
                        ...options.headers,
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    };
                    return fetch(url, { ...options, headers: newHeaders });
                });
            }
        }

        return response;
    };

    return request();
};