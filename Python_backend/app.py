"""
API REST para cifrado Vigenère con validaciones mejoradas
"""
import logging
from flask import Flask, request, jsonify
from flask_cors import CORS
from vigenere import cifrar_vigenere, descifrar_vigenere, preparar_texto

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Crear aplicación Flask
app = Flask(__name__)
CORS(app)

# Constantes de validación
MAX_LONGITUD_TEXTO = 100000
MAX_LONGITUD_CLAVE = 1000
MIN_LONGITUD_CLAVE = 3


def validar_entrada(texto, clave):
    """
    Valida el texto y la clave de entrada
    
    Returns:
        tuple: (es_valido, mensaje_error)
    """
    if not isinstance(texto, str) or not isinstance(clave, str):
        return False, 'Los campos "texto" y "clave" deben ser strings'

    if len(texto) > MAX_LONGITUD_TEXTO:
        return False, f'El texto es demasiado largo. Máximo {MAX_LONGITUD_TEXTO} caracteres'

    if len(clave) > MAX_LONGITUD_CLAVE:
        return False, f'La clave es demasiado larga. Máximo {MAX_LONGITUD_CLAVE} caracteres'

    if len(clave.strip()) < MIN_LONGITUD_CLAVE:
        return False, f'La clave debe tener al menos {MIN_LONGITUD_CLAVE} caracteres'

    clave_preparada = preparar_texto(clave)
    if not clave_preparada:
        return False, 'La clave no contiene letras válidas (solo se permiten A-Z)'

    texto_preparado = preparar_texto(texto)
    if not texto_preparado:
        return False, 'El texto no contiene caracteres válidos (solo se permiten letras A-Z)'

    return True, None


@app.route('/', methods=['GET'])
def home():
    """Endpoint raíz - Información de la API"""
    return jsonify({
        'nombre': 'API Cifrado Vigenère',
        'version': '1.1.0',
        'autor': 'Gaizka, Diego',
        'descripcion': 'API REST para cifrado y descifrado usando el algoritmo Vigenère',
        'limitaciones': {
            'max_longitud_texto': MAX_LONGITUD_TEXTO,
            'min_longitud_clave': MIN_LONGITUD_CLAVE,
            'caracteres_permitidos': 'A-Z (no sensible a mayúsculas)'
        },
        'endpoints': {
            'POST /api/vigenere/cifrar': 'Cifrar texto',
            'POST /api/vigenere/descifrar': 'Descifrar texto',
            'GET /api/health': 'Estado del servidor'
        }
    }), 200


@app.route('/api/health', methods=['GET'])
def health():
    """Endpoint de salud"""
    return jsonify({
        'status': 'ok',
        'mensaje': 'El servidor está funcionando correctamente'
    }), 200


@app.route('/api/vigenere/cifrar', methods=['POST'])
def cifrar():
    """Endpoint para cifrar texto con validaciones"""
    try:
        data = request.get_json()

        if not data:
            return jsonify({'error': 'No se proporcionaron datos JSON'}), 400

        texto = data.get('texto')
        clave = data.get('clave')

        if texto is None or clave is None:
            return jsonify({'error': 'Se requieren los campos "texto" y "clave"'}), 400

        es_valido, mensaje_error = validar_entrada(texto, clave)
        if not es_valido:
            return jsonify({'error': mensaje_error}), 400

        texto_cifrado = cifrar_vigenere(texto, clave)

        # ✅ Lazy formatting correcto
        logger.info("Texto cifrado exitosamente (longitud original: %d, válida: %d)",
                   len(texto), len(preparar_texto(texto)))

        return jsonify({
            'texto_cifrado': texto_cifrado,
            'longitud_original': len(texto),
            'longitud_valida': len(preparar_texto(texto)),
            'longitud_cifrado': len(texto_cifrado),
            'caracteres_ignorados': len(texto) - len(preparar_texto(texto))
        }), 200

    except ValueError as ve:
        # ✅ Lazy formatting correcto
        logger.error("Error de validación: %s", str(ve))
        return jsonify({'error': str(ve)}), 400

    except Exception as e:
        # ✅ Lazy formatting correcto
        logger.error("Error inesperado al cifrar: %s", str(e))
        return jsonify({'error': 'Error interno del servidor'}), 500


@app.route('/api/vigenere/descifrar', methods=['POST'])
def descifrar():
    """Endpoint para descifrar texto con validaciones"""
    try:
        data = request.get_json()

        if not data:
            return jsonify({'error': 'No se proporcionaron datos JSON'}), 400

        texto_cifrado = data.get('texto')
        clave = data.get('clave')

        if texto_cifrado is None or clave is None:
            return jsonify({'error': 'Se requieren los campos "texto" y "clave"'}), 400

        es_valido, mensaje_error = validar_entrada(texto_cifrado, clave)
        if not es_valido:
            return jsonify({'error': mensaje_error}), 400

        texto_descifrado = descifrar_vigenere(texto_cifrado, clave)

        # ✅ Lazy formatting correcto
        logger.info("Texto descifrado exitosamente (longitud: %d)", len(texto_descifrado))

        return jsonify({
            'texto_descifrado': texto_descifrado,
            'longitud_cifrado': len(texto_cifrado),
            'longitud_descifrado': len(texto_descifrado)
        }), 200

    except ValueError as ve:
        # ✅ Lazy formatting correcto
        logger.error("Error de validación: %s", str(ve))
        return jsonify({'error': str(ve)}), 400

    except Exception as e:
        # ✅ Lazy formatting correcto
        logger.error("Error inesperado al descifrar: %s", str(e))
        return jsonify({'error': 'Error interno del servidor'}), 500


@app.errorhandler(404)
def not_found(_error):
    return jsonify({'error': 'Endpoint no encontrado'}), 404


@app.errorhandler(405)
def method_not_allowed(_):
    return jsonify({'error': 'Método HTTP no permitido'}), 405


@app.errorhandler(413)
def request_entity_too_large(_):
    return jsonify({'error': 'Payload demasiado grande'}), 413


if __name__ == '__main__':
    print("=" * 50)
    print("      API CIFRADO VIGENÈRE v1.1.0")
    print("=" * 50)
    print("\n🚀 Servidor iniciando en http://127.0.0.1:5000")
    print("📅 Autor: Gaizka, Diego")
    print(f"\n⚙️  Configuración:")
    print(f"  • Longitud máxima de texto: {MAX_LONGITUD_TEXTO:,} caracteres")
    print(f"  • Longitud mínima de clave: {MIN_LONGITUD_CLAVE} caracteres")
    print(f"  • Caracteres permitidos: A-Z (insensible a mayúsculas)")
    print("\n📋 Endpoints disponibles:")
    print("  GET  /                        - Información de la API")
    print("  GET  /api/health             - Estado del servidor")
    print("  POST /api/vigenere/cifrar    - Cifrar texto")
    print("  POST /api/vigenere/descifrar - Descifrar texto")
    print("\n✅ Servidor listo para recibir peticiones")
    print("=" * 50)
    
    app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024
    
    app.run(host='127.0.0.1', port=5000, debug=True)
