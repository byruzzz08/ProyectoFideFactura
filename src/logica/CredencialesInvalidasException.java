package logica;

// Se lanza cuando el usuario o la contrasena no son validos, o el usuario esta inactivo
public class CredencialesInvalidasException extends Exception {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
