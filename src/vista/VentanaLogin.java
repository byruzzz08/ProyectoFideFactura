package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import logica.CredencialesInvalidasException;
import logica.Sistema;
import modelo.Usuario;

// HU-01: inicio de sesion con usuario y contrasena
public class VentanaLogin extends JFrame {

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtContrasena = new JPasswordField(20); // la contrasena se ve oculta
    private final JLabel lblError = UtilVista.etiquetaError();

    public VentanaLogin() {
        super("FideFactura - Iniciar sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        construir();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void construir() {
        JPanel encabezado = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("FideFactura", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitulo = new JLabel("Fidecompro - Facturacion e inventario", SwingConstants.CENTER);
        subtitulo.setForeground(Color.GRAY);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.SOUTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(formulario, 0, 0, "Usuario", txtUsuario);
        UtilVista.agregarCampo(formulario, 1, 0, "Contrasena", txtContrasena);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar); // Enter tambien ingresa

        JPanel pie = new JPanel(new BorderLayout(0, 6));
        pie.add(btnIngresar, BorderLayout.NORTH);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        pie.add(lblError, BorderLayout.CENTER);
        JLabel ayuda = new JLabel("Prueba: admin / admin123  -  cajero / cajero123", SwingConstants.CENTER);
        ayuda.setForeground(Color.GRAY);
        ayuda.setFont(ayuda.getFont().deriveFont(11f));
        pie.add(ayuda, BorderLayout.SOUTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());
        try {
            Usuario u = Sistema.getInstancia().getGestorUsuarios().iniciarSesion(usuario, contrasena);
            new VentanaPrincipal(u).setVisible(true);
            dispose();
        } catch (CredencialesInvalidasException ex) {
            lblError.setText(ex.getMessage());
            txtContrasena.setText("");
            txtContrasena.requestFocus();
        }
    }
}
