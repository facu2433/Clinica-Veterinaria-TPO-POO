package modelo;

import java.io.Serializable;

// PROVISORIO: lo completa la Parte B (lotes, stock, proveedores)
public class Producto implements Serializable {
    private String codigo;
    private String nombre;

    public Producto(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
}
