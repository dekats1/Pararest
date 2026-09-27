import React from "react";
import LoginForm from "../../components/auth/LoginForm.tsx";
import '../../styles/auth/LoginPage.css'

const LoginPage: React.FC=()=>{
    return (
        <div className="login-page">
            <div className="login-container">

                {}
                <div className="info-section">
                    <div className="logo">
                        <h1>Pararest</h1>
                    </div>

                    <div className="features">
                        <h2>Почему выбирают нас?</h2>
                        <ul>
                            <li>
                                <span className="icon"></span>
                                Готовим на огне
                            </li>
                            <li>
                                <span className="icon"></span>
                                Натуральные фермерские продукты
                            </li>
                            <li>
                                <span className="icon"></span>
                                Вкусная еда
                            </li>
                        </ul>
                    </div>
                </div>

                {}
                <div className="form-section">
                    <LoginForm />
                </div>

            </div>
        </div>
    )
}
export default LoginPage;