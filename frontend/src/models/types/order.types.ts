import type {MenuItemDTO} from "./menu.types.ts";
import {type PaymentStatus, PaymentType} from "./payment.types.ts";

export interface OrderInfoDTO{
    id: number,
    menuItemId: number,
    quantity: number
}

export interface FullOrderDTO{
    order: OrderDTO,
    menuItem: MenuItemDTO[]
}

export interface OrderDTO {
    id: number;
    items: OrderInfoDTO[];
    userId: string;
    status: OrderStatus;
    type: OrderType;
    orderTime?: string;
    deliveryTime?: string;
    totalPrice: number;
    paymentStatus: PaymentStatus;
    paymentType: PaymentType;
    userRequest?: string;
}

export interface OrderInfoDTO {
    id: number;
    menuItemId: number;
    quantity: number;
}

export interface CreateOrderDTO {
    orderType: OrderType,
    totalPrice: number,
    paymentType: PaymentType,
    userRequest?: string;
}



export enum OrderStatus {
    CANCELED = 'CANCELED',
    PENDING = 'PENDING',
    COMPLETED = 'COMPLETED',
    PREPARING = 'PREPARING',
    IN_CART = 'IN_CART',
    DISCARDED = 'DISCARDED'
}

export enum OrderType {
    DELIVERY_TO_HOTEL_NUMBER = 'DELIVERY_TO_HOTEL_NUMBER',
    IN_RESTAURANT = 'IN_RESTAURANT'
}