import React from 'react';
import '../styles/OrdersPage.css';
import Header from "../components/layout/Header/Header.tsx";
import OrdersSection from "../components/OrderSection.tsx";
import Footer from "../components/layout/Footer.tsx";

const OrdersPage: React.FC = () => {
    return (
        <div className="orders-page">
            <Header />
            <main>
                <OrdersSection />
            </main>
            <Footer />
        </div>
    );
};

export default OrdersPage;