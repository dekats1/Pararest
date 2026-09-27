import React from 'react';
import ProfileSection from '../components/ProfileSection';
import '../styles/ProfilePage.css';
import Header from "../components/layout/Header/Header.tsx";
import Footer from "../components/layout/Footer.tsx";

const ProfilePage: React.FC = () => {
    return (
        <div className="profile-page">
            <Header />
            <main>
                <ProfileSection />
            </main>
            <Footer />
        </div>
    );
};

export default ProfilePage;