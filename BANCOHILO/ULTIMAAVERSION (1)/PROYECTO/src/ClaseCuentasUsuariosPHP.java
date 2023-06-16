import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ClaseCuentasUsuariosPHP {
    private List<Cliente> clientes;

    public static void main(String[] args) {
        ClaseCuentasUsuariosPHP cuentasUsuarios = new ClaseCuentasUsuariosPHP();
        cuentasUsuarios.descargarCuentasUsuarios();

        // Accede a los datos de los clientes
        if (cuentasUsuarios.clientes != null) {
            for (Cliente cliente : cuentasUsuarios.clientes) {
                System.out.println("Nombre: " + cliente.getNombre());
                System.out.println("Clave: " + cliente.getClave());
                System.out.println("Saldo: " + cliente.getSaldo());
                System.out.println("-------------------");
            }
        }
    }

    public void descargarCuentasUsuarios() {
        try {
            URL url = new URL("http://192.168.1.131/compartida/ciudades-junio.php");
            URLConnection conexion = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String linea;
            List<String> lineas = new ArrayList<>();
            while ((linea = reader.readLine()) != null) {
                lineas.add(linea);
            }
            reader.close();

            clientes = parsearDatosClientes(lineas);
        } catch (IOException e) {
            e.printStackTrace();
            clientes = null;
        }
    }

    private List<Cliente> parsearDatosClientes(List<String> lineas) {
        List<Cliente> clientes = new ArrayList<>();

        int numClientes = Integer.parseInt(lineas.get(0));
        int numLineasPorCliente = 3;
        int numLineasTotales = numClientes * numLineasPorCliente;

        if (lineas.size() != numLineasTotales + 1) {
            System.out.println("Error en el formato de los datos de los clientes.");
            return clientes;
        }

        for (int i = 1; i <= numLineasTotales; i += numLineasPorCliente) {
            String nombre = lineas.get(i);
            String clave = lineas.get(i + 1);
            double saldo = Double.parseDouble(lineas.get(i + 2));

            Cliente cliente = new Cliente(nombre, clave, saldo);
            clientes.add(cliente);
        }

        return clientes;
    }

    private static class Cliente {
        private String nombre;
        private String clave;
        private double saldo;

        public Cliente(String nombre, String clave, double saldo) {
            this.nombre = nombre;
            this.clave = clave;
            this.saldo = saldo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getClave() {
            return clave;
        }

        public double getSaldo() {
            return saldo;
        }
    }
}
