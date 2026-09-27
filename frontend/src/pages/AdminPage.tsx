import React, { useState, useEffect } from 'react';

import '../styles/admin/AdminPage.css';
import AdminHeader from "../components/layout/Header/AdminHeader.tsx";
import AdminNavbar from "../components/admin/AdminNavbar.tsx";
import UsersTable from "../components/admin/UsersTable.tsx";
import MenuTable from "../components/admin/MenuTable.tsx";
import IngredientsTable from "../components/admin/IngredientTable.tsx";
import SuppliesTable from "../components/admin/SupplierTable.tsx";
import OrdersTable from "../components/admin/OrdersTable.tsx";
import PaymentsTable from "../components/admin/PaymentsTable.tsx";
import AnalyticsSection from "../components/admin/AnalyticsSection.tsx";

const AdminPage: React.FC = () => {
    const [activeSection, setActiveSection] = useState('analytics');

    useEffect(() => {
        const handleScroll = () => {
            const sections = ['analytics', 'users', 'menu', 'ingredients', 'supplies', 'orders', 'transactions'];

            for (const sectionId of sections) {
                const element = document.getElementById(sectionId);
                if (element) {
                    const rect = element.getBoundingClientRect();
                    if (rect.top <= 150 && rect.bottom >= 150) {
                        setActiveSection(sectionId);
                        break;
                    }
                }
            }
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const handleSectionChange = (sectionId: string) => {
        setActiveSection(sectionId);
        const element = document.getElementById(sectionId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };

    return (
        <div className="admin-layout">
            <AdminNavbar
                activeSection={activeSection}
                onSectionChange={handleSectionChange}
            />
            <div className="admin-main">
                <AdminHeader />
                <main className="admin-content">
\                    <section id="analytics" className="admin-section">
                        <AnalyticsSection />
                    </section>

                    <section id="users" className="admin-section">
                        <UsersTable />
                    </section>

                    <section id="menu" className="admin-section">
                        <MenuTable />
                    </section>

                    <section id="ingredients" className="admin-section">
                        <IngredientsTable />
                    </section>

                    <section id="supplies" className="admin-section">
                        <SuppliesTable />
                    </section>

                    <section id="orders" className="admin-section">
                        <OrdersTable />
                    </section>

                    <section id="transactions" className="admin-section">
                        <PaymentsTable />
                    </section>

                </main>
            </div>
        </div>
    );
};

export default AdminPage;