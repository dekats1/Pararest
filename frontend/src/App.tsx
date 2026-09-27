import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import RegistrationPage from './pages/auth/RegisterPage.tsx';
import LoginPage from './pages/auth/LoginPage.tsx';
import HomePage from './pages/HomePage.tsx';
import {AuthProvider} from "./context/AuthContext.tsx";
import AdminPage from "./pages/AdminPage.tsx";
import MenuPage from "./pages/MenuPage.tsx";
import CartPage from "./pages/CartPage.tsx";
import ReplenishmentPage from "./pages/ReplenishmentPage.tsx";
import OrdersPage from "./pages/OrdersPage.tsx";
import ProfilePage from "./pages/ProfilePage.tsx";
import BlockedPage from "./pages/BlockedPage.tsx";
import OAuth2CallbackPage from "./pages/auth/OAuth2CallbackPage.tsx";

function App() {
  return (
    <BrowserRouter>
        <AuthProvider>
            <Routes>
                <Route path="/" element={<Navigate to="/home" replace />} />
                <Route path="/register" element={<RegistrationPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/home" element={<HomePage />} />
                <Route path="/admin/dashboard" element={<AdminPage />} />
                <Route path={"/menu"} element={<MenuPage/>}/>
                <Route path={"/cart"} element={<CartPage/>}/>
                <Route path={"/wallet"} element={<ReplenishmentPage/>}/>
                <Route path={"/orders"} element={<OrdersPage/>}/>
                <Route path={"/profile"} element={<ProfilePage/>}/>
                <Route path={"/blocked"} element={<BlockedPage/>}/>
                <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
            </Routes>
        </AuthProvider>
    </BrowserRouter>
  );
}

export default App;