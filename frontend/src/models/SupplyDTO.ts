export interface SupplierDTO {
    id: number;
    name: string;
    phone: string;
    address: string;
}

export interface SupplyDTO {
    id: number;
    supplier: SupplierDTO;
    status: SupplyStatus;
    items: SupplyItemDTO[];
    dateOfRequest: string;
    dateOfSupply: string | null;
    totalPrice: number;
}

export interface SupplyItemDTO {
    id: number;
    ingredient: {
        id: number;
        name: string;
        unit: string;
    };
    quantity: number;
    priceAtSupply: number;
}

export enum SupplyStatus {
    IN_DELIVERY_CENTER = 'IN_DELIVERY_CENTER',
    IN_SORT_CENTER = 'IN_SORT_CENTER',
    IN_SHIPPED = 'IN_SHIPPED',
    SHIPPED = 'SHIPPED',
    CANCELED = 'CANCELED'
}

export interface AddSupplyItemDTO {
    ingredientId: number;
    quantity: number;
    priceAtSupply: number;
}

export interface AddSupplyDTO {
    supplierId: number;
    items: AddSupplyItemDTO[];
}