package com.Capinteria.carpinteria.enumeration;

public enum Role {

    ADMIN, CLIENTE, EMPLEADO


    /*
    CLIENTE(1),
    ADMIN(2),
    EMPLEADO(3);

    private final int valorNumerico;
    Role(int valorNumerico){
        this.valorNumerico = valorNumerico;
    }
    public int getValorNumerico(){
        return valorNumerico;
    }
    public static Role fromValorNumerico(int valor){
        for(Role estado: values()){
            if (estado.valorNumerico == valor){
                return estado;
            }
        }
        return null;
    }
¨*/
}
