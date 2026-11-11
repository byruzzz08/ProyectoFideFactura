package modelo;

public class ProductoLimpieza extends Producto {

    private static final long serialVersionUID = 1L;

    private int contenidoMl;
    private boolean esToxico;

    public ProductoLimpieza(String codigo, String nombre, String descripcion, double precioUnitario,
            int existencias, int existenciaMinima, int contenidoMl, boolean esToxico) {
        super(codigo, nombre, descripcion, precioUnitario, existencias, existenciaMinima);
        this.contenidoMl = contenidoMl;
        this.esToxico = esToxico;
    }

    @Override
    public double getTasaImpuesto() {
        return 0.13;
    }

    @Override
    public String getTipo() {
        return "Limpieza";
    }

    public int getContenidoMl() { return contenidoMl; }
    public void setContenidoMl(int contenidoMl) { this.contenidoMl = contenidoMl; }
    public boolean isEsToxico() { return esToxico; }
    public void setEsToxico(boolean esToxico) { this.esToxico = esToxico; }
}
