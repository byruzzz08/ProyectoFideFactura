package modelo;

public class ProductoElectrodomestico extends Producto {

    private static final long serialVersionUID = 1L;

    private String marca;
    private String modelo;
    private int garantiaMeses;

    public ProductoElectrodomestico(String codigo, String nombre, String descripcion, double precioUnitario,
            int existencias, int existenciaMinima, String marca, String modelo, int garantiaMeses) {
        super(codigo, nombre, descripcion, precioUnitario, existencias, existenciaMinima);
        this.marca = marca;
        this.modelo = modelo;
        this.garantiaMeses = garantiaMeses;
    }

    @Override
    public double getTasaImpuesto() {
        return 0.13;
    }

    @Override
    public String getTipo() {
        return "Electrodomestico";
    }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getGarantiaMeses() { return garantiaMeses; }
    public void setGarantiaMeses(int garantiaMeses) { this.garantiaMeses = garantiaMeses; }
}
