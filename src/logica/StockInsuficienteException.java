package logica;

// Se lanza cuando se intenta vender mas unidades de las que hay en inventario
public class StockInsuficienteException extends Exception {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
