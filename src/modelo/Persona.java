package modelo;

import java.io.Serializable;

public abstract class Persona implements Serializable {
    protected String nombre;
    protected  String apellido;
    protected String DNI;
    protected String telefono;


    public Persona(String nombre, String apellido, String DNI, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.DNI = DNI;
        this.telefono = telefono;
    }

    public String descripcion(){
        return "Nombre: " + nombre + "\nApellido: " + apellido + "\nDNI: " + DNI + "\nTelefono: " + telefono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDNI() {
        return DNI;
    }

    public String getTelefono() {
        return telefono;
    }
    
}
