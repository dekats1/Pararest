import React, { useState } from "react";
import type { CreateReplenishment } from "../models/types/payment.types.ts";
import "../styles/ReplenishmentSection.css";
import {paymentService} from "../api/paymentservice.ts";

const QUICK_AMOUNTS = [20, 50, 100, 200];

interface Props {
    onSuccess?: () => void;
}

const ReplenishmentSection: React.FC<Props> = ({ onSuccess }) => {
    const [amount, setAmount] = useState<string>("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const handleQuickAmount = (value: number) => {
        setAmount(String(value));
        setError(null);
        setSuccess(false);
    };

    const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        let val = e.target.value.replace(/[^0-9.]/g, "");

        const parts = val.split(".");
        if (parts.length > 2) {
            val = parts[0] + "." + parts.slice(1).join("");
        }

        if (parts.length === 2 && parts[1].length > 2) {
            val = parts[0] + "." + parts[1].slice(0, 2);
        }

        const numericVal = parseFloat(val);
        const MAX_AMOUNT = 100000000000;

        if (!isNaN(numericVal) && numericVal > MAX_AMOUNT) {
            setAmount(MAX_AMOUNT.toString());
        } else {
            setAmount(val);
        }

        setError(null);
        setSuccess(false);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const numericAmount = parseFloat(amount);
        if (!amount || isNaN(numericAmount) || numericAmount <= 0) {
            setError("Введите корректную сумму");
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const payload: CreateReplenishment = { totalAmount: numericAmount };
            await paymentService.newReplenishment(payload);
            setSuccess(true);
            setAmount("");
            onSuccess?.();
        } catch (err: any) {
            setError(err?.response?.data?.message || "Ошибка при пополнении. Попробуйте снова.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="replenishment-section">
            <div className="replenishment-card">
                <div className="replenishment-header">
                    <div className="wallet-icon">
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M21 7H3C2.44772 7 2 7.44772 2 8V19C2 19.5523 2.44772 20 3 20H21C21.5523 20 22 19.5523 22 19V8C22 7.44772 21.5523 7 21 7Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M16 13.5C16 14.3284 16.6716 15 17.5 15C18.3284 15 19 14.3284 19 13.5C19 12.6716 18.3284 12 17.5 12C16.6716 12 16 12.6716 16 13.5Z" fill="currentColor"/>
                            <path d="M7 7V5C7 4.44772 7.44772 4 8 4H20C20.5523 4 21 4.44772 21 5V7" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div>
                        <h2 className="replenishment-title">Пополнение кошелька</h2>
                        <p className="replenishment-subtitle">Выберите сумму или введите вручную</p>
                    </div>
                </div>

                <form className="replenishment-form" onSubmit={handleSubmit}>
                    <div className="quick-amounts">
                        {QUICK_AMOUNTS.map((val) => (
                            <button
                                key={val}
                                type="button"
                                className={`quick-amount-btn ${amount === String(val) ? "active" : ""}`}
                                onClick={() => handleQuickAmount(val)}
                            >
                                {val.toLocaleString("ru-RU")} BYN
                            </button>
                        ))}
                    </div>

                    <div className="amount-input-wrapper">
                        <input
                            className={`amount-input ${error ? "error" : ""}`}
                            type="text"
                            inputMode="decimal"
                            placeholder="0.00"
                            value={amount}
                            onChange={handleInput}
                            disabled={loading}
                        />
                        <span className="currency-symbol">BYN</span>
                    </div>

                    {error && (
                        <div className="replenishment-error">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <circle cx="12" cy="16" r="1" fill="currentColor"/>
                            </svg>
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="replenishment-success">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                                <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                                <path d="M8 12L11 15L16 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Кошелёк успешно пополнен!
                        </div>
                    )}

                    <button
                        type="submit"
                        className={`replenishment-btn ${loading ? "loading" : ""}`}
                        disabled={loading}
                    >
                        {loading ? (
                            <span className="btn-spinner"/>
                        ) : (
                            <>
                                Пополнить
                                {amount && !isNaN(parseFloat(amount)) && parseFloat(amount) > 0 && (
                                    <span className="btn-amount"> — {parseFloat(amount).toLocaleString("ru-RU")} BYN</span>
                                )}
                            </>
                        )}
                    </button>
                </form>
            </div>
        </section>
    );
};

export default ReplenishmentSection;