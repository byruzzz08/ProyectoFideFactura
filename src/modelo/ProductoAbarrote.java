package modelo;

import java.time.LocalDate;

public class ProductoAbarrote extends Producto {

    private static final long serialVersionUID = 1L;

    private LocalDate fechaVencimiento;
    private boolean canastaBasica;

    public ProductoAbarrote(String codigo, String nombre, String descripcion, double precioUnitario,
            int existencias, int existenciaMinima, LocalDate fechaVencimiento, boolean canastaBasica) {
        super(codigo, nombre, descripcion, precioUnitario, existencias, existenciaMinima);
        this.fechaVencimiento = fechaVencimiento;
        this.canastaBasica = canastaBasica;
    }

    // Canasta basica paga 1 %, el resto 13 %
    @Override
    public double getTasaImpuesto() {
        return canastaBasica ? 0.01 : 0.13;
    }

    @Override
    public String getTipo() {
        return "Abarrote";
    }

    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public boolean isCanastaBasica() { return canastaBasica; }
    public void setCanastaBasica(boolean canastaBasica) { this.canastaBasica = canastaBasica; }
}
