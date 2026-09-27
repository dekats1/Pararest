export enum PaymentStatus {
    PAID = 'PAID',
    NOT_PAID = 'NOT_PAID',
    ERROR = 'ERROR'
}

export enum PaymentType {
    CASH = 'CASH',
    CARD = 'CARD',
    REPLENISHMENT = 'REPLENISHMENT'
}

export interface UpdatePaymentStatusDTO {
    paymentStatus: PaymentStatus;
}

export interface PaymentDTO{
    id: number,
    userId: string,
    totalAmount: number,
    orderId: number,
    paymentType:string,
    paymentStatus: string,
    paymentDate: string
}

export interface CreateReplenishment{
    totalAmount: number
}