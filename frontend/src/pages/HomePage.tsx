import React from "react";
import Header from "../components/layout/Header/Header.tsx";
import HeroSection from "../components/home/HeroSection.tsx";
import AboutSection from "../components/home/AboutSection.tsx";
import MenuPreview from "../components/home/MenuPreview.tsx";
import ReservationCTA from "../components/home/ReservationCTA.tsx";
import Footer from "../components/layout/Footer.tsx";

const HomePage: React.FC = () => {
    return (
        <div className="home-page">
            <Header />
            <main>
                <HeroSection />
                <AboutSection />
                <MenuPreview />
                <ReservationCTA />
            </main>
            <Footer />
        </div>
    );
};

export default HomePage;