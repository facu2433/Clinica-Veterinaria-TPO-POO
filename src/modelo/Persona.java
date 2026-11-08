package modelo;

import java.io.Serializable;

public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nombre;
    protected String apellido;
    protected String DNI;
    protected String telefono;

    public Persona(String nombre, String apellido, String DNI, String telefono) {
        // Datos de identidad obligatorios: validar acá evita personas "vacías" en todo el sistema.
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (DNI == null || DNI.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }
        this.nombre = nombre;
        this.apellido = apellido;
        this.DNI = DNI;
        this.telefono = telefono; // opcional
    }

    public String descripcion() {
        return "Nombre: " + nombre + "\nApellido: " + apellido + "\nDNI: " + DNI
                + "\nTelefono: " + telefono;
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
