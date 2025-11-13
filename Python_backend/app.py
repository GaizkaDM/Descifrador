"""
API REST para cifrado Vigenère
Servidor Flask que expone endpoints para cifrar y descifrar texto
Integrado con el módulo vigenere.py existente
"""
import logging
import re
from logging.handlers import RotatingFileHandler
from flask import Flask, request, jsonify
from flask_cors import CORS
from vigenere import cifrar_vigenere, descifrar_vigenere


# Configurar logger general
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# Crear un manejador que guarde en un archivo (con rotación para evitar archivos enormes)
handler = RotatingFileHandler('api_cifrado_vigenere.log', maxBytes=1024*1024*10, backupCount=5)
handler.setLevel(logging.INFO)

# Formato legible para los logs
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)

# Añadir el handler al logger
logger.addHandler(handler)

# Crear aplicación Flask
app = Flask(__name__)

# Habilitar CORS para permitir peticiones desde JavaFX
CORS(app)

@app.route('/', methods=['GET'])
def home():
    """
    Endpoint raíz - Información de la API
    """
    return jsonify({
        'nombre': 'API Cifrado Vigenère',
        'version': '1.0.0',
        'autor': 'Gaizka, Diego',
        'descripcion': 'API REST para cifrado y descifrado usando el algoritmo Vigenère',
        'endpoints': {
            'POST /api/vigenere/cifrar': 'Cifrar texto',
            'POST /api/vigenere/descifrar': 'Descifrar texto',
            'GET /api/health': 'Estado del servidor'
        }
    }), 200


@app.route('/api/health', methods=['GET'])
def health():
    """
    Endpoint de salud - Verificar que el servidor está funcionando
    """
    return jsonify({
        'status': 'ok',
        'mensaje': 'El servidor está funcionando correctamente'
    }), 200


@app.route('/api/vigenere/cifrar', methods=['POST'])
def cifrar():
    """
    Endpoint para cifrar texto

    Body JSON esperado:
    {
        "texto": "Texto a cifrar",
        "clave": "Clave de cifrado"
    }
    """
    try:
        data = request.get_json()

        if not data:
            return jsonify({'error': 'No se proporcionaron datos JSON'}), 400

        texto = data.get('texto')
        clave = data.get('clave')

        if not texto or not clave:
            return jsonify({'error': 'Se requieren los campos "texto" y "clave"'}), 400

        validar_texto_sin_emoticonos(texto)
        validar_texto_sin_emoticonos(clave)

        if not clave.strip():
            return jsonify({'error': 'La clave no puede estar vacía'}), 400
        if len(clave.strip()) < 3:
            return jsonify({'error': 'La clave debe tener al menos 3 caracteres'}), 400
        # Cifrar usando tu función existente
        texto_cifrado = cifrar_vigenere(texto, clave)

        logger.info("Texto cifrado exitosamente (longitud: %d)", len(texto))

        return jsonify({
            'texto_cifrado': texto_cifrado,
            'longitud_original': len(texto),
            'longitud_cifrado': len(texto_cifrado)
        }), 200

    except ValueError as ve:
        logger.error("Error de validación: %s", str(ve))
        return jsonify({'error': str(ve)}), 400

    except KeyError as ke:
        logger.error("Error de clave: %s", str(ke))
        return jsonify({'error': f'Error de clave: {str(ke)}'}), 400
    except TypeError as te:
        logger.error("Error de tipo: %s", str(te))
        return jsonify({'error': f'Error de tipo: {str(te)}'}), 400
    except Exception as e:
        logger.error("Error inesperado en descifrar: %s", str(e))
        return jsonify({'error': 'Error interno del servidor'}), 500

@app.route('/api/vigenere/descifrar', methods=['POST'])
def descifrar():
    """
    Endpoint para descifrar texto

    Body JSON esperado:
    {
        "texto": "Texto cifrado",
        "clave": "Clave de descifrado"
    }
    """
    try:

        data = request.get_json()

        if not data:
            return jsonify({'error': 'No se proporcionaron datos JSON'}), 400

        texto_cifrado = data.get('texto')
        clave = data.get('clave')

        validar_texto_sin_emoticonos(texto_cifrado)
        validar_texto_sin_emoticonos(clave)
        if not texto_cifrado or not clave:
            return jsonify({'error': 'Se requieren los campos "texto" y "clave"'}), 400

        validar_texto_sin_emoticonos(texto_cifrado)
        validar_texto_sin_emoticonos(clave)

        if not clave.strip():
            return jsonify({'error': 'La clave no puede estar vacía'}), 400

        if len(clave.strip()) < 3:
            return jsonify({'error': 'La clave debe tener al menos 3 caracteres'}), 400
        # Descifrar usando tu función existente
        texto_descifrado = descifrar_vigenere(texto_cifrado, clave)

        logger.info("Texto descifrado exitosamente (longitud: %d)", len(texto_cifrado))

        return jsonify({
            'texto_descifrado': texto_descifrado,
            'longitud_cifrado': len(texto_cifrado),
            'longitud_descifrado': len(texto_descifrado)
        }), 200

    except ValueError as ve:
        logger.error("Error de validación: %s", str(ve))
        return jsonify({'error': str(ve)}), 400
    except KeyError as ke:
        logger.error("Error de clave: %s", str(ke))
        return jsonify({'error': f'Error de clave: {str(ke)}'}), 400
    except TypeError as te:
        logger.error("Error de tipo: %s", str(te))
        return jsonify({'error': f'Error de tipo: {str(te)}'}), 400
    except Exception as e:
        logger.error("Error inesperado en descifrar: %s", str(e))
        return jsonify({'error': 'Error interno del servidor'}), 500



@app.errorhandler(404)
def not_found(_error):
    """
    Controlador de error para cuando no se encuentra el endpoint solicitado.

    Args:
        _error (HTTPException): Objeto de error que contiene información sobre
                               el error HTTP 404 (No encontrado).

    Returns:
        Response: Respuesta JSON con un mensaje de error y código HTTP 404.
    """
    return jsonify({'error': 'Endpoint no encontrado'}), 404


@app.errorhandler(405)
def method_not_allowed(_error):
    """
    Controlador de error para cuando se usa un método HTTP no permitido
    en un endpoint.

    Args:
        _error (HTTPException): Objeto de error que contiene información sobre
                               el error HTTP 405 (Método no permitido).

    Returns:
        Response: Respuesta JSON con un mensaje de error y código HTTP 405.
    """
    return jsonify({'error': 'Método HTTP no permitido'}), 405


def validar_texto_sin_emoticonos(texto):
    """Metodo que valida el tipo de caracter quue se inserta tanto en la clave como en el cuerpo

    Args:
        texto (String): texto y clave de introducidas

    Raises:
        ValueError: Error que se muestra si contiene caracteres incorrectos
    """
    # Permite sólo letras, números, signos básicos y espacios
    patron = re.compile(r'^[A-Za-z0-9 .,;:¡!¿?()\-\n\r]*$')
    # Modifica el patrón para lo que desees permitir
    if not patron.match(texto):
        raise ValueError("El texto contiene caracteres no permitidos,"
                          + "como emoticonos o símbolos especiales.")


if __name__ == '__main__':
    print("=" * 50)
    print("      API CIFRADO VIGENÈRE")
    print("=" * 50)
    print("\n🚀 Servidor iniciando en http://172.20.106.20:5000")
    print("📅 Autor: Gaizka, Diego")
    print("\n📋 Endpoints disponibles:")
    print("  GET  /                        - Información de la API")
    print("  GET  /api/health             - Estado del servidor")
    print("  POST /api/vigenere/cifrar    - Cifrar texto")
    print("  POST /api/vigenere/descifrar - Descifrar texto")
    print("\n✅ Servidor listo para recibir peticiones")
    print("=" * 50)

    app.run(host='172.20.106.20', port=5000, debug=True)
