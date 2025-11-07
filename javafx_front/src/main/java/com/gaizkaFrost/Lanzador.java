package com.gaizkaFrost;

/**
 * Clase lanzadora de la aplicación.
 * <p>
 * Proporciona un punto de entrada alternativo para iniciar la aplicación
 * JavaFX a través del metodo {@link Main#main(String[])}. Es útil en
 * algunos entornos donde se requiere una clase con metodo {@code main} independiente.
 * </p>
 *
 * <p><b>Ejemplo de ejecución:</b></p>
 * <pre>{@code
 * java com.gaizkaFrost.Lanzador
 * }</pre>
 */
public class Lanzador {

    /**
     * Metodo principal que delega la ejecución al metodo {@link Main#main(String[])}.
     *
     * @param args argumentos de línea de comandos, que se pasan a {@code App.main(args)}
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
