package modelo;

public class Dueño extends Persona {
    private String email;
    private PlanSalud planSalud;

    public Dueño(String nombre, String apellido, String DNI, String telefono, String email, PlanSalud planSalud) {
        super(nombre, apellido, DNI, telefono);
        this.email = email;
        this.planSalud = planSalud;
    }

    @Override 
    public String descripcion() {
        return "Dueño: \n" + super.descripcion() + "\nEmail: " + email + "\nPlan de Salud: " + planSalud;
    }
    
}
