package modelo;

// Metodos de pago aceptados al confirmar una factura
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    SINPE_MOVIL("SINPE Movil"),
    TRANSFERENCIA("Transferencia");

    private final String etiqueta;

    MetodoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
