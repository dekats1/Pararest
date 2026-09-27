import React from 'react';

import '../styles/CartPage.css';
import Header from "../components/layout/Header/Header.tsx";
import Footer from "../components/layout/Footer.tsx";
import CartSection from "../components/CartSection.tsx";

const CartPage: React.FC = () => {
    const handleOrderCreated = () => {
        console.log('Заказ оформлен');
    };

    return (
        <div className="cart-page">
            <Header />
            <main>
                <CartSection onOrderCreated={handleOrderCreated} />
            </main>
            <Footer />
        </div>
    );
};

export default CartPage;