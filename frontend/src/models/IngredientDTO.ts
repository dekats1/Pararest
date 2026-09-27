export enum Metrics {
    KG = 'KG',
    G = 'G',
    TH = 'TH',
    L = 'L',
    ML = 'ML'
}

export const metricsLabels: Record<Metrics, string> = {
    [Metrics.KG]: 'кг',
    [Metrics.G]: 'г',
    [Metrics.TH]: 'шт',
    [Metrics.L]: 'л',
    [Metrics.ML]: 'мл'
};

export interface IngredientDTO {
    id: number;
    name: string;
    unit: Metrics;
    currentStock: number;
    minStock: number;
    updatedAt: string;
}

export interface AddIngredientDTO {
    name: string;
    unit: Metrics;
    currentStock: number;
    minStock: number;
}