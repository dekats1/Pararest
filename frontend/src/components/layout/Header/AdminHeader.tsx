import React from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../../../context/AuthContext.tsx";
import "../../../styles/layout/Header/AdminHeader.css"

const AdminHeader: React.FC =()=>{
    const navigate = useNavigate();
    const {user, logout} = useAuth();

    const handleLogout = () =>{
        logout();
        navigate('/home')
    }

    return (
        <header className="admin-header">
            <div className="admin-header-container">
                <span className="admingreeting">
                    Здравствуйте, {user?.fullName || "администратор"}!
                </span>
                <button onClick={handleLogout} className="logout-btn">
                    Выйти
                </button>
            </div>
        </header>
    )
}

export default AdminHeader;