"""
    Practica del cifrado Vigenère. UD1 - Python
    Implementa el cifrado y descifrado Vigenère.
    Convierte letras en números (A=0, ..., Z=25), aplica suma o resta
    con la clave, y vuelve a convertir a letras.
    Autor: Gaizka
    Fecha: 2024-09-16
"""

import os

ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
CLAVE="CLAVE"



def letra_a_num(letra):
    """Convierte una letra en número (A=0, ..., Z=25).

    Args:
        letra (str): Letra a convertir
    Returns:
        int: Número correspondiente
    Raises:
        ValueError: Si la letra no está en A-Z
    """
    return ABC.index(letra)

def num_a_letra(num):
    """Convierte un número en una letra usando módulo 26.

    Args:
        num (int): Número a convertir
    Returns:
        str: Letra correspondiente
    """
    return ABC[num % 26]

def repetir_clave(texto, clave):
    """Repite la clave hasta la longitud del texto.

    args:
        texto (str): Texto a cifrar o descifrar
        clave (str): Clave para el cifrado o descifrado
    Returns:
        str: Clave repetida o truncada a la longitud del texto
    """
    if not clave:
        raise ValueError("La clave no puede estar vacía")
    clave = clave.upper()
    longitud_texto = len(texto)
    longitud_clave = len(clave)

    # cuántas veces hay que repetir la clave (con 1 extra para asegurar)
    repeticiones = longitud_texto // longitud_clave + 1
    # repetir la clave
    clave_repetida = clave * repeticiones
    # recortar a la longitud exacta del texto
    clave_final = clave_repetida[:longitud_texto]
    return clave_final

def preparar_texto(texto):
    """
    Normaliza el texto:
    - Convierte a mayúsculas
    - Elimina espacios
    - Elimina caracteres no alfabéticos

    Args:
        texto (str): Texto a preparar
    Returns:
        str: Texto normalizado
    """
    texto = texto.upper()
    resultado = ""
    for c in texto:
        if c in ABC:
            resultado += c
    return resultado

def cifrar(texto, clave):
    """Cifra un texto con la clave dada.

    Args:
        texto (str): Texto a cifrar
        clave (str): Clave para el cifrado

    Returns:
        str: Texto cifrado
    """
    texto = preparar_texto(texto)
    clave = repetir_clave(texto, clave)
    resultado = ""
    for t, k in zip(texto, clave):
        suma = letra_a_num(t) + letra_a_num(k)
        resultado += num_a_letra(suma)
    return resultado

def descifrar(texto, clave):
    """Descifra un texto cifrado con la clave dada.

    Args:
        texto (str): Texto cifrado a descifrar
        clave (str): Clave usada en el cifrado
    Returns:
        str: Texto descifrado
    """
    texto = preparar_texto(texto)
    clave = repetir_clave(texto, clave)
    resultado = ""
    for t, k in zip(texto, clave):
        resta = letra_a_num(t) - letra_a_num(k)
        resultado += num_a_letra(resta)
    return resultado

# --- Métodos extra para trabajar con ficheros ---
def leer_fichero(ruta):
    """Lee el contenido de un fichero de texto (UTF-8).

    Args:
        ruta (str): Ruta del fichero a leer

    Returns:
        str: Contenido del fichero
    """
    with open(ruta, "r", encoding="utf-8") as f:
        return f.read()

def escribir_fichero(ruta, contenido):
    """Escribe contenido en un fichero de texto (UTF-8).

    Args:
        ruta (str): Ruta del fichero a escribir
        contenido (str): Contenido a escribir en el fichero
    Returns:
        None
    """
    with open(ruta, "w", encoding="utf-8") as f:
        f.write(contenido)

cifrar_vigenere = cifrar
descifrar_vigenere = descifrar


# --- Ejemplo de uso directo con ficheros ---
if __name__ == "__main__":

    data_dir = os.path.join(os.path.dirname(__file__), "..", "data")

    mensaje_path = os.path.join(data_dir, "ejemplo.txt")
    cifrado_path = os.path.join(data_dir, "mensaje_cifrado.txt")
    descifrado_path = os.path.join(data_dir, "mensaje_descifrado.txt")

    # Leer texto original
    original = leer_fichero(mensaje_path)

    # Cifrar y guardar
    cifrado = cifrar(original, CLAVE)
    escribir_fichero(cifrado_path, cifrado)

    # Descifrar y guardar
    descifrado = descifrar(cifrado, CLAVE)
    escribir_fichero(descifrado_path, descifrado)

    print("✅ Cifrado y descifrado completados.")
    print("Revisa los archivos en la carpeta data/")
