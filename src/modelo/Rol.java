package modelo;

// Roles del sistema: definen que modulos ve cada usuario
public enum Rol {
    ADMINISTRADOR("Administrador"),
    CAJERO("Cajero");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
