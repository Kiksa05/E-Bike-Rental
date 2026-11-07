import axios from "axios";
//komunikacija z backend
const api = axios.create({
    baseURL: process.env.REACT_APP_BASE_URL,
    timeout: 30000,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// An expired token means the session is over. A failed login is just a wrong password.
api.interceptors.response.use(undefined, (error) => {
    if (error.response?.status === 401 && error.config?.url !== "/customers/login") {
        sessionStorage.clear();
        window.location.assign("/login");
    }
    return Promise.reject(error);
});

export const errorMessage = (error, fallback) =>
    typeof error.response?.data === "string" && error.response.data ? error.response.data : fallback;

export default api;
