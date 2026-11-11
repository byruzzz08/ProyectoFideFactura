package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import logica.Sistema;
import modelo.Usuario;

// Menu principal: solo muestra los modulos del rol que inicio sesion (HU-01, HU-02)
public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;

    public VentanaPrincipal(Usuario usuario) {
        super("FideFactura - Menu principal");
        this.usuario = usuario;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        construir();
        setSize(520, 380);
        setLocationRelativeTo(null);
    }

    private void construir() {
        JLabel lblSesion = new JLabel("Sesion: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
        lblSesion.setFont(lblSesion.getFont().deriveFont(Font.BOLD));
        JButton btnCerrar = new JButton("Cerrar sesion");
        btnCerrar.addActionListener(e -> cerrarSesion());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(lblSesion, BorderLayout.WEST);
        encabezado.add(btnCerrar, BorderLayout.EAST);

        JPanel modulos = new JPanel(new GridLayout(3, 2, 12, 12));
        modulos.add(botonModulo("Facturacion", () ->
                abrirModulo("FideFactura - Facturacion - Cajero: " + usuario.getNombreUsuario(),
                        new PanelFacturacion(usuario), 980, 620)));
        modulos.add(botonModulo("Clientes", () ->
                abrirModulo("FideFactura - Clientes", new PanelClientes(usuario), 980, 600)));
        modulos.add(botonModulo("Productos", () ->
                abrirModulo("FideFactura - Productos e inventario", new PanelProductos(usuario), 980, 600)));
        modulos.add(botonModulo("Historial", () ->
                UtilVista.mostrarInfo(this, "El historial de facturas se entrega en el siguiente avance.")));

        if (usuario.esAdministrador()) {
            modulos.add(botonModulo("Usuarios", () ->
                    UtilVista.mostrarInfo(this, "La gestion de usuarios se entrega en el siguiente avance.")));
        } else {
            JLabel nota = new JLabel("<html><center>Usuarios solo aparece<br>para el administrador</center></html>");
            nota.setForeground(Color.GRAY);
            nota.setHorizontalAlignment(JLabel.CENTER);
            modulos.add(nota);
        }

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(modulos, BorderLayout.CENTER);
        setContentPane(contenido);
    }

    private JButton botonModulo(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.setFont(b.getFont().deriveFont(15f));
        b.setPreferredSize(new Dimension(180, 60));
        b.addActionListener(e -> accion.run());
        return b;
    }

    // Abre el modulo en una ventana modal y guarda los datos al cerrarla
    private void abrirModulo(String titulo, JPanel panel, int ancho, int alto) {
        JDialog dialogo = new JDialog(this, titulo, true);
        dialogo.setContentPane(panel);
        dialogo.setSize(ancho, alto);
        dialogo.setLocationRelativeTo(this);
        dialogo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                UtilVista.guardarEnSegundoPlano(VentanaPrincipal.this);
            }
        });
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.setVisible(true);
    }

    // HU-02: vuelve al inicio de sesion y limpia el usuario actual
    private void cerrarSesion() {
        Sistema.getInstancia().getGestorUsuarios().cerrarSesion();
        UtilVista.guardarEnSegundoPlano(this);
        dispose();
        new VentanaLogin().setVisible(true);
    }

    // Al salir se guarda en el mismo hilo para asegurar que el archivo quede escrito
    private void salir() {
        try {
            Sistema.getInstancia().guardar();
        } catch (IOException e) {
            UtilVista.mostrarError(this, "No se pudieron guardar los datos: " + e.getMessage());
        }
        System.exit(0);
    }
}
