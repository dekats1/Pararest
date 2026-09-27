import React from 'react';
import { useNavigate } from 'react-router-dom';
import RegisterForm from '../../components/auth/RegisterForm.tsx';
import '../../styles/auth/RegisterPage.css';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();

  const handleRegistrationSuccess = () => {
    navigate('/home');
  };

  return (
    <div className="register-page">
      <div className="register-container">
        
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
          <RegisterForm onSuccess={handleRegistrationSuccess} />
        </div>
        
      </div>
    </div>
  );
};

export default RegisterPage;