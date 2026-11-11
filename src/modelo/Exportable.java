package modelo;

import java.io.IOException;

// Todo lo que se pueda exportar a un archivo fisico implementa esta interfaz
public interface Exportable {

    void exportar(String ruta) throws IOException;
}
