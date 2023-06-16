# BancoServidorClientes
Cajero automático Java: Cliente-servidor con encriptación, FTP, GUI. Gestiona transacciones bancarias. Ejemplo Java para proyectos complejos.
Este repositorio contiene el código fuente de un sistema cliente-servidor implementado en Java. El sistema es un cliente de cajero automático que se conecta a un servidor para realizar transacciones bancarias. Algunas de las características del sistema incluyen:

Conexión FTP: El cliente se conecta a un servidor FTP para descargar un archivo de anuncios. Se utiliza la biblioteca Apache Commons Net para la transferencia de archivos.

Encriptación de mensajes: Se utiliza el algoritmo DES para cifrar y descifrar los mensajes enviados entre el cliente y el servidor. Se generan claves secretas y se utilizan para cifrar los mensajes antes de ser enviados.

Hashing de contraseñas: Se utiliza la función de hash SHA-256 para calcular el resumen de la contraseña ingresada por el usuario. El resumen se envía al servidor para autenticación.

Interfaz gráfica de usuario: El cliente utiliza una interfaz gráfica de usuario (GUI) implementada con la biblioteca Swing. La GUI permite al usuario interactuar con el cajero automático, realizar transacciones y ver mensajes del servidor.

Comunicación bidireccional: El cliente envía mensajes al servidor y recibe respuestas en tiempo real. Los mensajes del servidor se muestran en una segunda área de texto en la GUI.

Gestión de transacciones: El cliente puede realizar diferentes transacciones como consultar el saldo, retirar dinero e ingresar dinero. Cada transacción se envía al servidor y se procesa según las reglas del sistema bancario.

Este código es una implementación básica de un sistema cliente-servidor y puede servir como punto de partida para proyectos más complejos o como ejemplo de programación en Java con encriptación y comunicación en red.
