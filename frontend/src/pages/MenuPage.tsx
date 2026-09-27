import Header from "../components/layout/Header/Header";
import Footer from "../components/layout/Footer";
import MenuSection from "../components/MenuSection";
import "../styles/MenuPage.css";

const MenuPage: React.FC = () => {
    return (
        <div className="menu-page">
            <Header />
            <main>
                <MenuSection />
            </main>
            <Footer />
        </div>
    );
};

export default MenuPage;