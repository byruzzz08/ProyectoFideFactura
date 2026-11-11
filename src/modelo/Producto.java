package modelo;

import java.io.Serializable;

// Clase abstracta: cada tipo de producto calcula su propio impuesto (polimorfismo)
public abstract class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private String descripcion;
    private double precioUnitario;
    private int existencias;
    private int existenciaMinima;

    public Producto(String codigo, String nombre, String descripcion,
            double precioUnitario, int existencias, int existenciaMinima) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.existencias = existencias;
        this.existenciaMinima = existenciaMinima;
    }

    // Tasa como fraccion: 0.13 = 13 %, 0.01 = 1 %
    public abstract double getTasaImpuesto();

    public abstract String getTipo();

    public void rebajarExistencias(int cantidad) {
        if (cantidad <= 0 || cantidad > existencias) {
            throw new IllegalArgumentException("Cantidad invalida para rebajar: " + cantidad);
        }
        existencias -= cantidad;
    }

    public void aumentarExistencias(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad invalida para aumentar: " + cantidad);
        }
        existencias += cantidad;
    }

    public boolean estaBajoMinimo() {
        return existencias < existenciaMinima;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getExistencias() { return existencias; }
    public void setExistencias(int existencias) { this.existencias = existencias; }
    public int getExistenciaMinima() { return existenciaMinima; }
    public void setExistenciaMinima(int existenciaMinima) { this.existenciaMinima = existenciaMinima; }
}
