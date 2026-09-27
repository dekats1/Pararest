export enum UserRole {
    USER = "USER",
    ADMIN = "ADMIN",
    MANAGER = "MANAGER",
    EMPLOYEE = "EMPLOYEE"
}

export interface UserDTO{
    id: string,
    login: string,
    fullName: string,
    email: string,
    phone: string,
    birthday: string,
    wallet: number,
    role: UserRole,
    roomNumber:number | null,
    blocked:boolean
}


