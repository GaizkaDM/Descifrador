package com.gaizkaFrost;

/**
 * <h2>Clase Lanzador</h2>
 * Clase encargada de proporcionar un punto de entrada alternativo para iniciar la
 * aplicación JavaFX.
 *
 * <p>
 * Algunos entornos requieren que la aplicación disponga de una clase propia con un
 * método {@code main} separado, especialmente al generar artefactos ejecutables
 * o lanzarla desde ciertos gestores de ejecución. Esta clase simplemente delega la
 * llamada al método {@link Main#main(String[])}.
 * </p>
 *
 * <h3>Ejemplo de ejecución</h3>
 * <pre>
 * {@code
 * java com.gaizkaFrost.Lanzador
 * }
 * </pre>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class Lanzador {

    /**
     * <h3>Método main</h3>
     * Método principal encargado de redirigir la ejecución al método
     * {@link Main#main(String[])} de la clase principal de la aplicación.
     *
     * @param args Argumentos recibidos desde la línea de comandos.
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}
