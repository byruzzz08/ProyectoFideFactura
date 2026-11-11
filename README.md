# FideFactura - Avance 2

Aplicacion de facturacion e inventario para Fidecompro.
Curso Programacion Cliente/Servidor Concurrente - Universidad Fidelitas.
Estudiante: Byron Ruiz Gutierrez.

## Como ejecutar
1. Abrir la carpeta del proyecto en NetBeans (File > Open Project).
2. Clic derecho en el proyecto > Run (clase principal: fidefactura.FideFactura).
3. Usuarios de prueba: admin / admin123 (Administrador) y cajero / cajero123 (Cajero).

Requiere JDK 17 o superior.

## Historias de usuario implementadas (10 de 14 = 71 %)
| ID | Historia | Estado |
|----|----------|--------|
| HU-01 | Iniciar sesion | Implementada |
| HU-02 | Cerrar sesion | Implementada |
| HU-03 | Gestion de usuarios | Pendiente (Avance 3) |
| HU-04 | Registrar cliente fisico o juridico | Implementada |
| HU-05 | Buscar y editar cliente | Implementada |
| HU-06 | Desactivar cliente | Implementada |
| HU-07 | Registrar producto por tipo | Implementada |
| HU-08 | Editar producto y existencias | Implementada |
| HU-09 | Productos bajo minimo | Implementada |
| HU-10 | Armar factura | Implementada |
| HU-11 | Confirmar pago | Implementada |
| HU-12 | Exportar factura a .txt | Implementada |
| HU-13 | Historial de facturas | Pendiente (Avance 3) |
| HU-14 | Anular factura | Logica lista, pantalla pendiente |

## Conceptos aplicados
- Clases y objetos: paquete modelo.
- Herencia: Cliente -> ClienteFisico / ClienteJuridico; Producto -> ProductoAbarrote / ProductoLimpieza / ProductoElectrodomestico.
- Polimorfismo: getTasaImpuesto(), getPorcentajeDescuento() y getNombreParaFactura().
- Interfaz: Exportable (la implementa Factura).
- Excepciones propias: CredencialesInvalidas, ClienteNoEncontrado, ProductoNoEncontrado, StockInsuficiente.
- Colecciones: ArrayList (usuarios, clientes, facturas, detalles) y HashMap (inventario por codigo).
- Serializacion: logica.Sistema guarda todo en fidefactura.dat.
- Interfaz grafica: Java Swing.
- Multihilos: SwingWorker para exportar la factura y guardar los datos sin congelar la ventana; metodos synchronized en Inventario.
