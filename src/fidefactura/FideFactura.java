package fidefactura;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import logica.Sistema;
import vista.VentanaLogin;

// Clase principal: carga los datos y abre el inicio de sesion
public class FideFactura {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Si Nimbus no esta disponible se usa el aspecto por defecto
        }

        Sistema.getInstancia(); // carga fidefactura.dat o crea los datos de ejemplo

        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
