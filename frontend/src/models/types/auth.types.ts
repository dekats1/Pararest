import type { UserAddDTO } from "../UserAddDTO.ts";

export interface JwtResponse{
    accessToken:string,
    refreshToken:string
}


export interface RegisterRequest{
    password:string,
    rememberMe:boolean,
    newUser:UserAddDTO
}

export interface LoginRequest{
    login: string,
    password: string,
    rememberMe: boolean
}