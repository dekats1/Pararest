import React from 'react';
import { Users, Menu, Package, Truck, ShoppingCart, BarChart3 } from "lucide-react";
import '../../styles/admin/AdminNavbar.css';

interface AdminNavbarProps {
    activeSection: string;
    onSectionChange: (section: string) => void;
}

const AdminNavbar: React.FC<AdminNavbarProps> = ({ activeSection, onSectionChange }) => {
    const sections = [
        { id: 'analytics',    label: 'Аналитика',     icon: <BarChart3 size={20} /> },
        { id: 'users',        label: 'Пользователи', icon: <Users size={20} /> },
        { id: 'menu',         label: 'Меню',          icon: <Menu size={20} /> },
        { id: 'ingredients',  label: 'Ингредиенты',   icon: <Package size={20} /> },
        { id: 'supplies',     label: 'Поставки',      icon: <Truck size={20} /> },
        { id: 'orders',       label: 'Заказы',        icon: <ShoppingCart size={20} /> },
        { id: 'transactions', label: 'Транзакции',    icon: <BarChart3 size={20} /> },
    ];

    return (
        <nav className="admin-navbar">
            <div className="admin-navbar-header">
                <h3>Панель управления</h3>
            </div>
            <ul className="admin-navbar-menu">
                {sections.map(section => (
                    <li
                        key={section.id}
                        className={`admin-navbar-item ${activeSection === section.id ? 'active' : ''}`}
                        onClick={() => onSectionChange(section.id)}
                    >
                        <span className="admin-navbar-icon">{section.icon}</span>
                        <span className="admin-navbar-label">{section.label}</span>
                    </li>
                ))}
            </ul>
        </nav>
    );
};

export default AdminNavbar;