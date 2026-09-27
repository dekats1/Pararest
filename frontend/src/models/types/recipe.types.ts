export interface RecipeDTO {
    id: {
        menuItemId: number;
        ingredientId: number;
    };
    ingredient: IngredientDTO;
    quantity: number;
}

export interface IngredientDTO {
    id: number;
    name: string;
    unit: string;
    currentStock: number;
    minStock: number;
    updatedAt: string;
    price: number;
}