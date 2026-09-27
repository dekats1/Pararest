export interface CategoryDTO{
    name: string
}

export interface MenuItemDTO{
    id: number;
    name: string;
    description: string;
    price: number;
    category: CategoryDTO;
    imageUrl?: string | null;
    active: boolean;
    adderName: string;
    createdAt: string;
    updatedAt?: string;
}