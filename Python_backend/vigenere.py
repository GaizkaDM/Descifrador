"""
    Practica del cifrado Vigenère. UD1 - Python
    Implementa el cifrado y descifrado Vigenère.
    Convierte letras en números (A=0, ..., Z=25), aplica suma o resta
    con la clave, y vuelve a convertir a letras.
    Autor: Gaizka
    Fecha: 2024-09-16
"""

import os

ABC = "AÁÀÄÂÃBCDEÉÈËÊFGHIÍÌÏÎJKLMNÑOÓÒÖÔÕPQRSTUÚÙÜÛVWXYZ"
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
    if letra == ' ':
        return -1  # Valor especial si quieres manejar espacios aparte
    if letra not in ABC:
        raise ValueError(f"Caracter no válido: '{letra}'")
    return ABC.index(letra)

def num_a_letra(num):
    """Convierte un número en una letra usando módulo 26.

    Args:
        num (int): Número a convertir
    Returns:
        str: Letra correspondiente
    """
    return ABC[num % len(ABC)]

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


def cifrar(texto, clave):
    """Cifra el texto usando el cifrado Vigenère.
    Args:
        texto (str): Texto a cifrar
        clave (str): Clave para el cifrado
    Returns:
        str: Texto cifrado
    """
    texto = texto.upper()
    clave = repetir_clave(texto, clave)
    resultado = ""
    indice_clave = 0
    for char in texto:
        if char == ' ':
            resultado += ' '  # Mantiene espacios tal cual
        elif char in ABC:
            k = clave[indice_clave % len(clave)]
            suma = letra_a_num(char) + letra_a_num(k)
            resultado += num_a_letra(suma)
            indice_clave += 1
        else:
            raise ValueError(f"Caracter inválido en texto: '{char}'")
    return resultado


def descifrar(texto, clave):
    """Descifra el texto usando el cifrado Vigenère.
    Args:
        texto (str): Texto a descifrar
        clave (str): Clave para el descifrado
    Returns:
        str: Texto descifrado
    """
    texto = texto.upper()
    clave = repetir_clave(texto, clave)
    resultado = ""
    indice_clave = 0
    for char in texto:
        if char == ' ':
            resultado += ' '
        elif char in ABC:
            k = clave[indice_clave % len(clave)]
            resta = letra_a_num(char) - letra_a_num(k)
            resultado += num_a_letra(resta)
            indice_clave += 1
        else:
            raise ValueError(f"Caracter inválido en texto: '{char}'")
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
