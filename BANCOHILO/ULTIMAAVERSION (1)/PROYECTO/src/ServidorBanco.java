import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ServidorBanco extends JFrame {
    private ServerSocket serverSocket;
    private JLabel tiempo;
    private JLabel tiempoConectado;
    private JTextArea textarea1;
    private boolean clienteConectado;
    private long tiempoInicio;
    private int segundosTranscurridos;
    private SecretKey secretKey;
    private List<CuentaUsuario> cuentasUsuarios;
    private String usuarioActual;
    boolean retirarbien;
    boolean ingresarbien;
    boolean variablebuclecliente;

    public static void main(String[] args) {
        ServidorBanco servidor = new ServidorBanco();
        servidor.iniciar();
    }

    public ServidorBanco() {
        setLayout(null);
        JPanel panel = new JPanel();
        getContentPane();
        setFocusable(true);
        panel.setLayout(null);

        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Servidor Examen Final 2 Recuperación");

        tiempo = new JLabel("00:00:000");
        add(tiempo);
        tiempo.setFont(new Font("Serif", Font.BOLD, 30));
        tiempo.setBounds(20, 0, tiempo.getPreferredSize().width, tiempo.getPreferredSize().height);

        tiempoConectado = new JLabel("00:00:000");
        add(tiempoConectado);
        tiempoConectado.setFont(new Font("Serif", Font.BOLD, 15));
        tiempoConectado.setBounds(400, 0, tiempoConectado.getPreferredSize().width,
                tiempoConectado.getPreferredSize().height);

        textarea1 = new JTextArea();
        JScrollPane scrollpane1 = new JScrollPane(textarea1);
        scrollpane1.setBounds(30, 70, 400, 400);
        add(scrollpane1);

        panel.setBounds(0, 0, 500, 600);
        panel.setVisible(true);
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        add(panel);

        cargarClaveSecreta();
        cargarCuentasUsuarios();

        setSize(500, 600);
        setVisible(true);
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(12345); // Puerto del servidor
            System.out.println("Servidor Banco iniciado.");

            // Crea y comienza el hilo del reloj
            Thread relojThread = new Thread(() -> {
                while (true) {
                    Date fechaActual = new Date();
                    SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss.SSS");
                    String horaFormateada = formato.format(fechaActual);

                    SwingUtilities.invokeLater(() -> {
                        tiempo.setText(horaFormateada);
                    });

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            relojThread.start();

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado.");

                // Crea un nuevo hilo para manejar la conexión con el cliente
                ClienteHandler clienteHandler = new ClienteHandler(clienteSocket);
                clienteHandler.start();
                /*Estas líneas de código crean una instancia de la clase ClienteHandler y luego inician su ejecución en un hilo separado. Aquí está la explicación:

ClienteHandler clienteHandler = new ClienteHandler(clienteSocket);: Se crea un nuevo objeto ClienteHandler pasando como argumento el objeto clienteSocket.
Presumiblemente, la clase ClienteHandler es una clase personalizada que implementa la lógica necesaria para manejar las interacciones entre el servidor y
el cliente. Al pasar el clienteSocket al constructor del ClienteHandler, se proporciona una forma de comunicación con el cliente.

clienteHandler.start();: Se llama al método start() en el objeto clienteHandler. Suponiendo que ClienteHandler extiende la clase Thread o
implementa la interfaz Runnable, el método start() inicia la ejecución del hilo. Esto implica que el código dentro del método run() de ClienteHandler
 se ejecutará en paralelo con el hilo principal del programa, lo que permite un manejo concurrente de múltiples clientes.

En resumen, estas líneas de código crean un nuevo objeto ClienteHandler y lo inician en un hilo separado, lo que permite manejar las
interacciones con un cliente específico de manera independiente mientras el servidor sigue atendiendo a otros clientes.

                 */

                // Inicia el contador cuando se conecta un cliente
                clienteConectado = true;
                tiempoInicio = System.currentTimeMillis();

                Thread contadorThread = new Thread(() -> {
                    while (clienteConectado) {
                        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
                        segundosTranscurridos = (int) (tiempoTranscurrido / 1000);

                        SwingUtilities.invokeLater(() -> {
                            String tiempoFormateado = formatearTiempo(segundosTranscurridos);
                            tiempoConectado.setText(tiempoFormateado);
                        });

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                contadorThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatearTiempo(int segundos) {
        int horas = segundos / 3600;
        int minutos = (segundos % 3600) / 60;
        int segundosRestantes = segundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, segundosRestantes);
    }

    private void cargarClaveSecreta() {
        try {
            Path claveSecretaPath = Paths.get("C:\\Users\\USUARIO\\Downloads\\xamp\\htdocs\\compartida\\clave_secreta.key");
            byte[] claveBytes = Files.readAllBytes(claveSecretaPath);
            KeySpec claveSpec = new DESKeySpec(claveBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            secretKey = keyFactory.generateSecret(claveSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCuentasUsuarios() {
        try {
            URL url = new URL("http://192.168.1.129/compartida/cuentasUsuarios.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int numCuentas = Integer.parseInt(reader.readLine().trim());
                cuentasUsuarios = new ArrayList<>();

                for (int i = 0; i < numCuentas; i++) {
                    String nombre = reader.readLine().trim();
                    String hashClave = reader.readLine().trim();

                    String saldoHex = reader.readLine().trim();
                    double saldo = Double.parseDouble(saldoHex);


                    CuentaUsuario cuenta = new CuentaUsuario(nombre, hashClave, saldo);
                    cuentasUsuarios.add(cuenta);
                    System.out.println(cuenta.getHashClave());
                }

                reader.close();
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String cifrarMensaje(String mensaje) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] mensajeBytes = mensaje.getBytes("UTF-8");
            byte[] mensajeCifrado = cipher.doFinal(mensajeBytes);
            return Base64.getEncoder().encodeToString(mensajeCifrado);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String descifrarMensaje(String mensajeCifrado) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] mensajeCifradoBytes = Base64.getDecoder().decode(mensajeCifrado);
            byte[] mensajeBytes = cipher.doFinal(mensajeCifradoBytes);
            return new String(mensajeBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class ClienteHandler extends Thread {
        private Socket clienteSocket;
        private BufferedReader entrada;
        private PrintWriter salida;
        private boolean esperaContraseña = false;
        private String usuario;

        public ClienteHandler(Socket socket) {
            clienteSocket = socket;
        }
        /*
        En resumen, el constructor ClienteHandler se utiliza para crear una instancia de la clase ClienteHandler y establecer el
         socket del cliente en el atributo clienteSocket. Esto permite que el
         objeto ClienteHandler pueda acceder y utilizar el socket del cliente para enviar y recibir datos durante la comunicación entre el servidor y el cliente.
         */

        @Override
        public void run() {
            try {
                entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                salida = new PrintWriter(clienteSocket.getOutputStream(), true);
                // Envía un mensaje al cliente al arrancar el servidor
                String mensajeInicial = "Dime tu usuario";
                String mensajeInicialCifrado = cifrarMensaje(mensajeInicial);
                salida.println(mensajeInicialCifrado);

                while (true) {
                    String mensajeCifrado = entrada.readLine();
                    System.out.println("Mensaje cifrado del cliente: " + mensajeCifrado);

                    String mensaje = descifrarMensaje(mensajeCifrado);
                    System.out.println("Mensaje descifrado del cliente: " + mensaje);

                    SwingUtilities.invokeLater(() -> {
                        textarea1.append("Cliente: " + mensaje + "\n");
                    });
                    /*
                    En Java Swing, todas las actualizaciones de la interfaz de usuario deben realizarse en el hilo de despacho
                    de eventos de Swing (también conocido como hilo EDT). El método invokeLater() de SwingUtilities se utiliza
                    para agregar una tarea al hilo EDT y asegurarse de que se ejecute de forma sincronizada con la interfaz de usuario.

En este caso específico, la tarea que se agrega al hilo EDT es una función lambda que actualiza el contenido de un componente de la
interfaz de usuario, en este caso textarea1. El código dentro de la función lambda concatena el mensaje recibido del cliente con la
 etiqueta "Cliente:" y agrega un salto de línea al final. La función append() se utiliza para agregar el texto al componente textarea1.

La razón por la que se utiliza invokeLater() es garantizar que la actualización de la interfaz de usuario se realice correctamente en
el hilo EDT, evitando posibles problemas de concurrencia y asegurando que la interfaz de usuario responda de manera fluida al interactuar con ella.
                     */
                    if (mensaje.equals("DETENER_CONTADOR")) {
                        clienteConectado = false;
                    }
                    if (mensaje.equals("INICIAR_CONTADOR")) {

                    }

                    if (esperaContraseña) {
                        // Comprobar si la contraseña es correcta
                        boolean contraseñaCorrecta = verificarContraseña(usuario, mensaje);

                        // Enviar respuesta al cliente
                        //  String respuesta222 = "“Password correcta, Hola" +usuario;

                        String respuesta;
                        if (contraseñaCorrecta) {
                            respuesta = "Password correcta, Hola " + usuario + "\n" + " que quieres hacer ahora ingresar, saber tu disponibilidad, salir , o retirar?";

                            ///////////////////////////////////////////////////////7//////////////////


                            String respuestaCifrada = cifrarMensaje(respuesta);
                            salida.println(respuestaCifrada);


                            while (true) {


                                String mensajeCifrado2 = entrada.readLine();
                                System.out.println("Mensaje cifrado del cliente2: " + mensajeCifrado2);

                                String mensaje2 = descifrarMensaje(mensajeCifrado2);
                                System.out.println("Mensaje descifrado del cliente2: " + mensaje2);

                                SwingUtilities.invokeLater(() -> {
                                    textarea1.append("Cliente: " + mensaje2 + "\n");
                                });


                                if (retirarbien) {
                                    ////////////
                                    double extra = 0.0;

                                    double numero = Double.parseDouble(mensaje2);
                                    for (CuentaUsuario cuenta : cuentasUsuarios) {
                                        if (cuenta.getNombre().equals(usuario)) {
                                            System.out.println(cuenta.getSaldo());
                                            cuenta.setSaldo(cuenta.getSaldo() - numero);
                                            System.out.println(cuenta.getSaldo());
                                            extra = cuenta.getSaldo();
                                        }
                                    }
                                    String extraString = String.valueOf(extra);
                                    String respuesta33 = usuario +"Tu cantidad es " + extraString + " seleccione siguiente accion";

                                    retirarbien = false;


                                    String finalRespuesta1 = respuesta33;
                                    SwingUtilities.invokeLater(() -> {
                                        textarea1.append("Cliente: " + finalRespuesta1 + "\n");
                                    });


                                } else if (mensaje2.equals("retirar")) {
                                    retirarbien = true;
                                } else if (ingresarbien) {

                                    double extra = 0.0;

                                    double numero = Double.parseDouble(mensaje2);
                                    for (CuentaUsuario cuenta : cuentasUsuarios) {
                                        if (cuenta.getNombre().equals(usuario)) {
                                            System.out.println(cuenta.getSaldo());
                                            cuenta.setSaldo(cuenta.getSaldo() + numero);
                                            System.out.println(cuenta.getSaldo());
                                            extra = cuenta.getSaldo();
                                        }
                                    }
                                    String extraString = String.valueOf(extra);
                                    String respuesta33 = usuario +"Tu cantidad es "+ " " + extraString + " seleccione siguiente accion" ;

                                    ingresarbien = false;


                                    String finalRespuesta1 = respuesta33;
                                    SwingUtilities.invokeLater(() -> {
                                        textarea1.append("Cliente: " + finalRespuesta1 + "\n");
                                    });


                                } else if (mensaje2.equals("ingresar")) {
                                    ingresarbien = true;
                                } else if (mensaje2.equals("saldo")) {

                                    double extra = 0.0;


                                    for (CuentaUsuario cuenta : cuentasUsuarios) {
                                        if (cuenta.getNombre().equals(usuario)) {
                                            System.out.println(cuenta.getSaldo());

                                            System.out.println(cuenta.getSaldo());
                                            extra = cuenta.getSaldo();
                                        }
                                    }

                                    String respuesta33 = "El saldo actual de tu cuenta es" + extra + " seleccione la siguiente accion" +usuario;
                                    String finalRespuesta1 = respuesta33;
                                    SwingUtilities.invokeLater(() -> {
                                        textarea1.append("Cliente: " + finalRespuesta1 + "\n");
                                    });


                                } else if (mensaje2.equals("*")) {
                                    String respuesta33 = "el cliente" + usuario + " ha cerrado su sesion ";
                                    String finalRespuesta1 = respuesta33;
                                    SwingUtilities.invokeLater(() -> {
                                        textarea1.append("Cliente: " + finalRespuesta1 + "\n");
                                    });

                                    Thread.sleep(1000);
                                    clienteSocket.close();
                                    System.out.println("Conexión con el cliente cerrada.");
                                    //Detiene el contador cuando se desconecta el cliente
                                    clienteConectado = false;


                                } else {


                                    String finalRespuesta1 = "seleccione que quiere hacer ";
                                    SwingUtilities.invokeLater(() -> {
                                        textarea1.append("Cliente: " + finalRespuesta1 + "\n");
                                    });


                                }


                            }

                            //  String respuestaCifrada222 = cifrarMensaje(respuesta222);
                            //    salida.println(respuestaCifrada222);
                        } else {
                            respuesta = "CONTRASEÑA_INCORRECTA";
                        }
                        String respuestaCifrada = cifrarMensaje(respuesta);
                        salida.println(respuestaCifrada);

                        String finalRespuesta = respuesta;
                        SwingUtilities.invokeLater(() -> {
                            textarea1.append("Servidor: " + finalRespuesta + "\n");
                        });

                        esperaContraseña = false;
                    } else {
                        // Verificar si el usuario existe en el archivo php
                        boolean usuarioExiste = verificarUsuario(mensaje);

                        // Envía una respuesta al cliente
                        String respuesta;
                        if (usuarioExiste) {
                            respuesta = "PASSWORD";
                            esperaContraseña = true;
                            usuario = mensaje;
                        } else {
                            respuesta = "USUARIO_INVALIDO";

                        }

                        String respuestaCifrada = cifrarMensaje(respuesta);
                        salida.println(respuestaCifrada);

                        SwingUtilities.invokeLater(() -> {
                            textarea1.append("Servidor: " + respuesta + "\n");
                        });

                        if (!usuarioExiste) {
                            //IMPORTANTE SI QUITAS LOS COMENTARIO DE AQUI ABAJO SI PONES UN USUARIO NO VALIDO CIERRA LA VENTA POR SEGURIDAD IMPLEMENTACIÓN PROPIA PARA SEGURIDAD
                            //LA DEJO COMENTADA PARA QUE SIGA MEJOR LAS PAUTAS DEL ENUNCIADO PERO SE PUEDE QUITAR LOS COMENTARIOS DE AQUI DEBAJO PARA PRUEBA
                            // Cierra la conexión si el usuario es inválido

                            //   clienteSocket.close();
                            //    System.out.println("Conexión con el cliente cerrada.");

                            // Detiene el contador cuando se desconecta el cliente
                            // clienteConectado = false;
                            //  break;

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean verificarUsuario(String usuario) {
            for (CuentaUsuario cuenta : cuentasUsuarios) {
                if (cuenta.getNombre().equals(usuario)) {
                    return true;
                }
            }
            return false;
        }

        private boolean verificarContraseña(String usuario, String contraseña) {
            for (CuentaUsuario cuenta : cuentasUsuarios) {
                if (cuenta.getNombre().equals(usuario)) {
                    System.out.println(contraseña);
                    System.out.println(cuenta.getHashClave());
                    return cuenta.getHashClave().equals(contraseña);
                }
            }
            return false;
        }
        /*
        private boolean verificarContraseña(String usuario, String contraseña) {
            for (CuentaUsuario cuenta : cuentasUsuarios) {
                if (cuenta.getNombre().equals(usuario)) {
                    System.out.println();
                    String hashContraseña = CuentaUsuario.calcularHashContraseña(contraseña);
                    System.out.println("Contraseñah recibida del cliente: " + contraseña + "ho->>>" +hashContraseña);
                    System.out.println("Contraseña almacenada en la cuenta: " + cuenta.getHashClave());
                    return cuenta.getHashClave().equals(hashContraseña);
                }
            }
            return false;
        }


*/
    }

    private static class CuentaUsuario {
        private String nombre;
        private String hashClave;
        private double saldo;

        public CuentaUsuario(String nombre, String hashClave, double saldo) {
            this.nombre = nombre;
            this.hashClave = hashClave;
            this.saldo = saldo;
        }

        public String getNombre() {
            return nombre;
        }

        public void setSaldo(double saldo) {
            this.saldo = saldo;
        }

        public String getHashClave() {
            return hashClave;
        }

        public double getSaldo() {
            return saldo;
        }

        public static String calcularHashContraseña(String contraseña) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(contraseña.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(hashBytes);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }

        private static String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
    }
}