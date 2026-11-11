package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import logica.Sistema;

// Metodos de apoyo que usan todas las pantallas
public final class UtilVista {

    public static final Color ROJO_ERROR = new Color(200, 40, 40);
    public static final Color AMARILLO_BAJO_MINIMO = new Color(255, 243, 205);
    public static final Color NARANJA_BAJO_MINIMO = new Color(200, 120, 0);
    public static final String COLON = "\u20A1 ";

    private UtilVista() {
    }

    public static JLabel etiquetaError() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(ROJO_ERROR);
        return lbl;
    }

    // Agrega una etiqueta arriba y el campo abajo, en la fila indicada de un GridBagLayout
    public static void agregarCampo(JPanel panel, int fila, int columna, String etiqueta, JComponent campo) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = columna;
        gbc.gridy = fila * 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 4, 0, 4);
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridy = fila * 2 + 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(2, 4, 2, 4);
        panel.add(campo, gbc);
    }

    public static double leerDouble(JTextField campo, String nombre) {
        String texto = campo.getText().trim().replace(" ", "").replace(",", ".");
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El campo " + nombre + " debe ser un numero");
        }
    }

    public static int leerEntero(JTextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("El campo " + nombre + " debe ser un numero entero");
        }
    }

    // Ejecuta una accion cada vez que cambia el texto (filtrar mientras se escribe)
    public static void alCambiarTexto(JTextField campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "FideFactura", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "FideFactura", JOptionPane.INFORMATION_MESSAGE);
    }

    // Guarda los datos en un hilo aparte (SwingWorker) para no congelar la ventana
    public static void guardarEnSegundoPlano(Component padre) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Sistema.getInstancia().guardar();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    mostrarError(padre, "No se pudieron guardar los datos: " + e.getMessage());
                }
            }
        }.execute();
    }
}
