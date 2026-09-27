import React, { useState } from "react";

import "../styles/ReplenishmentPage.css";
import Header from "../components/layout/Header/Header.tsx";
import Footer from "../components/layout/Footer.tsx";
import ReplenishmentSection from "../components/ReplenishmentSection.tsx";
import PaymentHistorySection from "../components/PaymentHistorySection.tsx";

const ReplenishmentPage: React.FC = () => {
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    const handleReplenishmentSuccess = () => {
        setRefreshTrigger((prev) => prev + 1);
    };

    return (
        <div className="cart-page">
            <Header />
            <main className="replenishment-main">
                <ReplenishmentSection onSuccess={handleReplenishmentSuccess} />
                <PaymentHistorySection refreshTrigger={refreshTrigger} />
            </main>
            <Footer />
        </div>
    );
};

export default ReplenishmentPage;