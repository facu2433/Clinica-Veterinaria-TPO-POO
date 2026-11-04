package modelo;
import java.util.ArrayList;
import java.util.List;

public class Duenio extends Persona {
    private String email;
    private PlanSalud planSalud;
    private List<Mascota> mascotas;

    public Duenio(String nombre, String apellido, String DNI, String telefono, String email, PlanSalud planSalud) {
        super(nombre, apellido, DNI, telefono);
        this.email = email;
        this.planSalud = planSalud;
        this.mascotas = new ArrayList<>();
        
    }

    @Override 
    public String descripcion() {
        return "Dueño: \n" + super.descripcion() + "\nEmail: " + email + "\nPlan de Salud: " + planSalud;
    }
    
    public void quitarMascota(Mascota mascota) {
        mascotas.remove(mascota);
    }

    public void agregarMascota(Mascota mascota) {
        mascotas.add(mascota);
    }
}
