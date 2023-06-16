import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.security.NoSuchAlgorithmException;
public class ClienteCajero extends JFrame implements ActionListener {
    private static final String SERVIDOR_FTP = "localhost";
    private static final int PUERTO_FTP = 21;
    private static final String USUARIO = "walde";
    private static final String CONTRASENA = "123456";
    private static final String ARCHIVO_ORIGEN = "anuncios.dgt";
    private static final String ARCHIVO_DESTINO = "anuncios.txt";
    private static final int INTERVALO_MOSTRAR = 2000; // 2 segundos
    private JPanel panel;
    private JTextArea mensaje;
    private JTextArea textarea1;
    private JTextArea textarea2;
    private JButton botonSaldo;
    private JButton botonRetirarDinero;
    private JButton botonIngresarDinero;
    private JButton botonSalir;

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private SecretKey secretKey;
    private boolean esperandoContrasena = false;

    public static void main(String[] args) {
        ClienteCajero cliente = new ClienteCajero();
        cliente.conectar();
    }

    public ClienteCajero() {
        setLayout(null);
        panel = new JPanel();
        getContentPane();
        setFocusable(true);
        panel.setLayout(null);

        mensaje = new JTextArea();
        JScrollPane scrollpane3 = new JScrollPane(mensaje);

        mensaje.setBounds(300, 10, 100, 30);
        add(scrollpane3);
        mensaje.setEditable(true);
        add(mensaje);

        textarea1 = new JTextArea();
        JScrollPane scrollpane1 = new JScrollPane(textarea1);
        scrollpane1.setBounds(10, 50, 400, 300);
        add(scrollpane1);
        textarea1.setEditable(true);
        textarea1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (esperandoContrasena) {
                        String contrasena = textarea1.getText();
                        mensaje.setText("Ingrese la contraseña:");
                        enviarMensajeServidor(getSHA256Hash(contrasena));
                        esperandoContrasena= false;
                        textarea1.setText("");
                    } else {
                        String usuario = textarea1.getText();
                        enviarMensajeServidor(usuario);
                        textarea1.setText("");

                     //  esperandoContrasena = true;

                    }
                }
            }
        });

        textarea2 = new JTextArea();
        JScrollPane scrollpane2 = new JScrollPane(textarea2);
        scrollpane2.setBounds(10, 370, 500, 50);
        add(scrollpane2);
        textarea2.setEditable(false);

        botonSaldo = new JButton("Saldo");
        botonSaldo.setBounds(420, 10, 130, 30);
        add(botonSaldo);
        botonSaldo.addActionListener(this);

        botonRetirarDinero = new JButton("Retirar Dinero");
        botonRetirarDinero.setBounds(420, 50, 130, 30);
        add(botonRetirarDinero);
        botonRetirarDinero.addActionListener(this);

        botonIngresarDinero = new JButton("Ingresar Dinero");
        botonIngresarDinero.setBounds(420, 100, 130, 30);
        add(botonIngresarDinero);
        botonIngresarDinero.addActionListener(this);

        botonSalir = new JButton("Salir");
        botonSalir.setBounds(420, 150, 100, 30);
        add(botonSalir);
        botonSalir.addActionListener(this);

        panel.setBounds(0, 0, 600, 500);
        panel.setVisible(true);
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        add(panel);

        setSize(600, 500);
        setVisible(true);


    }

    public void conectar() {


        try {
            socket = new Socket("localhost", 12345); // Dirección IP y puerto del servidor
            System.out.println("Conexión establecida con el servidor.");

            //
            try {
                // Conexión FTP
                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(SERVIDOR_FTP, PUERTO_FTP);
                ftpClient.login(USUARIO, CONTRASENA);

                // Descargar archivo
                String rutaSalida = "C:\\Users\\USUARIO\\Downloads"; // Ruta de salida deseada
                boolean descargado = descargarArchivoFTP(ftpClient, ARCHIVO_ORIGEN, rutaSalida);

                if (descargado) {
                    File archivoDescargado = new File(rutaSalida + File.separator + ARCHIVO_ORIGEN);

                    // Leer datos del archivo descargado
                    FileInputStream fis = new FileInputStream(archivoDescargado);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Object objetoComprobar = ois.readObject();

                    String datos = objetoComprobar.toString();
                    System.out.println(datos);

                    // Leer resumen original
                    objetoComprobar = ois.readObject();
                    byte[] resumenOriginal = (byte[]) objetoComprobar;

                    // Generar resumen SHA-256 de los datos
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(datos.getBytes());
                    byte[] resumenActual = md.digest();

                    System.out.println("Resumen esperado: " + Arrays.toString(resumenOriginal));
                    System.out.println("Resumen generado: " + Arrays.toString(resumenActual));

                    // Comprobar resumen
                    if (MessageDigest.isEqual(resumenActual, resumenOriginal)) {
                        // Generar archivo con los datos
                        generarArchivoConDatos(datos.getBytes(), rutaSalida + File.separator + ARCHIVO_DESTINO);

                        System.out.println("Archivo generado con los datos: " + ARCHIVO_DESTINO);

                    } else {
                        System.out.println("El resumen no coincide. No se puede confiar en el archivo.");
                    }

                    ois.close();
                } else {
                    System.out.println("Error al descargar el archivo desde FTP.");
                }
                // Cerrar conexión FTP
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                mostrarAnunciosLeidos(2000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ///
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);

            cargarClaveSecreta();

            // Bucle principal para recibir mensajes del servidor continuamente
            boolean conexionActiva = true;
            while (conexionActiva) {
                // Lee un mensaje encriptado del servidor
                String mensajeServidorEncriptado = entrada.readLine();

                if (mensajeServidorEncriptado == null) {
                    // Si el mensaje es null, significa que el servidor cerró la conexión
                    conexionActiva = false;
                } else {
                    System.out.println("Mensaje encriptado del servidor: " + mensajeServidorEncriptado);
                    // Descifra el mensaje
                    String mensajeServidor = descifrarMensaje(mensajeServidorEncriptado);
                    System.out.println("Mensaje del servidor: " + mensajeServidor);
                    // Muestra el mensaje en el segundo JTextArea
                    mostrarMensaje(mensajeServidor);
                  if(mensajeServidor.equals("PASSWORD")){
                   esperandoContrasena = true;
                   }
                    /*
                    // Realiza las acciones correspondientes en respuesta al mensaje del servidor
                    if (mensajeServidor.equals("INICIAR_CONTADOR")) {
                        iniciarContador(); // Llama al método para iniciar el contador en el servidor
                    } else if (mensajeServidor.equals("DETENER_CONTADOR")) {
                        detenerContador(); // Llama al método para detener el contador en el servidor
                    } else {
                        // Realiza otras acciones en respuesta al mensaje del servidor por ejemplo la palabra password
                    }
                     */
                }
            }

            System.out.println("Conexión cerrada por el servidor.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Cierra la conexión al salir del bucle principal
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cargarClaveSecreta() {
        try {
            Path claveSecretaPath = Paths.get("EXAMENRECUPERACION/clave_secreta.key");
            byte[] claveSecretaBytes = Files.readAllBytes(claveSecretaPath);

            KeySpec keySpec = new DESKeySpec(claveSecretaBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            secretKey = keyFactory.generateSecret(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String cifrarMensaje(String mensaje) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] mensajeBytes = mensaje.getBytes();
            byte[] mensajeCifradoBytes = cipher.doFinal(mensajeBytes);
            return Base64.getEncoder().encodeToString(mensajeCifradoBytes);
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
            return new String(mensajeBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void mostrarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            textarea2.append(mensaje + "\n");
        });
    }

    private void iniciarContador() {
        // Lógica para iniciar el contador en el servidor
        // En este ejemplo, simplemente envía un mensaje encriptado al servidor para iniciar el contador
        String mensajeEncriptado = cifrarMensaje("INICIAR_CONTADOR");
        salida.println(mensajeEncriptado);
    }

    private void detenerContador() {
        // Lógica para detener el contador en el servidor
        // En este ejemplo, simplemente envía un mensaje encriptado al servidor para detener el contador
        String mensajeEncriptado = cifrarMensaje("DETENER_CONTADOR");
        salida.println(mensajeEncriptado);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == botonSaldo) {
            enviarMensajeServidor("saldo");

        } else if (e.getSource() == botonRetirarDinero) {
            String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad a retirar:");
            enviarMensajeServidor("retirar");
            enviarMensajeServidor(cantidad);
            // textarea1.setText("");
        } else if (e.getSource() == botonIngresarDinero) {
            String cantidad = JOptionPane.showInputDialog(this, "Ingrese la cantidad a ingresar:");
            enviarMensajeServidor("ingresar");

            enviarMensajeServidor(cantidad);
            // textarea1.setText("");
        } else if (e.getSource() == botonSalir) {

            enviarMensajeServidor("*");
            detenerContador();
            System.exit(0);
        }
    }

    private void enviarMensajeServidor(String mensaje) {
        String mensajeEncriptado = cifrarMensaje(mensaje);
        System.out.println("Mensaje enviado al servidor: " + mensaje); // Imprimir el mensaje antes de enviarlo
        mostrarMensaje("Mensaje enviado al servidor: " + mensaje); // Mostrar el mensaje en el JTextArea

        salida.println(mensajeEncriptado);
    }



    private String getSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /////////////////////////////
    private static boolean descargarArchivoFTP(FTPClient ftpClient, String nombreArchivo, String rutaSalida) throws IOException {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        BufferedOutputStream bufSalida = new BufferedOutputStream(new FileOutputStream(rutaSalida + "/" + nombreArchivo));
        boolean descargado = ftpClient.retrieveFile(nombreArchivo, bufSalida);
        bufSalida.close();
        return descargado;
    }

    private static String obtenerResumenDesdeBytes(byte[] resumenBytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : resumenBytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String generarResumenSHA256(byte[] datos) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(datos);
        return obtenerResumenDesdeBytes(hashedBytes);
    }

    private static void generarArchivoConDatos(byte[] datosBytes, String rutaArchivo) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(rutaArchivo);
        fileOutputStream.write(datosBytes);
        fileOutputStream.close();
    }




    public void mostrarAnunciosLeidos(int tiempoMs) throws IOException, InterruptedException {
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\USUARIO\\Downloads\\anuncios.txt"));
        String linea = "";
        while (linea != null) {
            linea = br.readLine();
            if (linea != null) {
                this.mensaje.setText(linea);
                Thread.sleep(tiempoMs);
            }
        }
        br.close();
    }

}

