package modelo;

public class Administrador extends Persona {
    private static final long serialVersionUID = 1L;

    public Administrador(String nombre, String apellido, String DNI, String telefono) {
        super(nombre, apellido, DNI, telefono);
    }

    @Override 
    public String descripcion() {
        return "Administrador: \n" + super.descripcion();
    }
    
}
