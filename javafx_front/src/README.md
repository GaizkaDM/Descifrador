# Descifrador - Aplicación de Cifrado Vigenère

Una aplicación de escritorio moderna desarrollada con **JavaFX** y **Flask** que permite cifrar y descifrar texto utilizando el algoritmo **Vigenère**. Interfaz temática inspirada en Hogwarts con un diseño elegante y funcional.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Uso](#uso)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [API](#api)
- [Autores](#autores)

---

## ✨ Características

- **Cifrado y Descifrado Vigenère**: Implementación completa del algoritmo de cifrado por sustitución polialfabética
- **Interfaz Gráfica Intuitiva**: Diseño temático Hogwarts con colores cálidos y dorados
- **Validaciones Robustas**: 
  - Validación de caracteres permitidos (sin emoticonos ni símbolos especiales)
  - Longitud mínima de clave (3 caracteres)
  - Mensajes de error descriptivos
- **Menú Completo**:
  - Opción de salir
  - Cambio de idioma (Español/Inglés)
  - Manual de uso
  - Información sobre los desarrolladores
- **Gestión de Archivos**: Cargar archivos, guardar resultados y copiar al portapapeles
- **Logging**: Registro detallado de operaciones en archivo separado
- **API REST**: Backend Flask para operaciones de cifrado

---

## 🔧 Requisitos

### Backend (Flask)
- Python 3.8 o superior
- Flask
- Flask-CORS
- Módulo `vigenere.py` personalizado

### Frontend (JavaFX)
- Java 17 o superior
- JavaFX SDK 22
- Maven (para compilación)

---

## 📦 Instalación

### 1. Backend (Servidor Flask)

```bash
# Clonar o descargar el proyecto
cd Python_backend

# Instalar dependencias
pip install flask flask-cors

# Ejecutar el servidor
python app.py
```

El servidor estará disponible en `http://172.20.106.20:5000`

### 2. Frontend (Aplicación JavaFX)

```bash
# Navegar a la carpeta del proyecto Java
cd javafx_front

# Compilar con Maven
mvn clean package

# Ejecutar la aplicación
mvn javafx:run
```

O desde el IDE (IntelliJ IDEA, Eclipse, etc.):
- Abre el proyecto
- Ejecuta la clase `Main.java`

---

## 🎮 Uso

### Interfaz Principal

1. **Clave**: Introduce la clave de cifrado (mínimo 3 caracteres)
2. **Algoritmo**: Selecciona el tipo de cifrado (actualmente disponible: Vigenère)
3. **Texto de entrada**: Escribe o carga el texto a cifrar/descifrar
4. **Botones de acción**:
   - **Cifrar**: Encripta el texto usando la clave
   - **Descifrar**: Desencripta el texto usando la clave
5. **Texto de salida**: Visualiza el resultado
6. **Opciones adicionales**:
   - Cargar archivo
   - Limpiar campos
   - Guardar resultado
   - Copiar resultado

### Menú

- **Archivo**: Salir de la aplicación
- **Idioma**: Cambiar entre Español e Inglés
- **Información**: Acceder al manual o información sobre los desarrolladores

---

## 📁 Estructura del Proyecto

```
Descifrador/
├── Python_backend/
│   ├── app.py                 # API REST Flask
│   ├── vigenere.py            # Módulo de cifrado Vigenère
│   ├── api_cifrado_vigenere.log  # Archivo de logs
│   └── requirements.txt
│
├── javafx_front/
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/gaizkaFrost/
│   │   │       ├── Main.java           # Clase principal
│   │   │       ├── MainController.java # Controlador
│   │   │       └── APIClient.java      # Cliente HTTP
│   │   └── main/resources/
│   │       ├── MainView.fxml           # Interfaz FXML
│   │       └── css/
│   │           └── styles.css          # Estilos CSS Hogwarts
│   └── pom.xml
│
└── README.md
```

---

## 🔌 API

### Base URL
```
http://172.20.106.20:5000
```

### Endpoints

#### 1. Obtener información de la API
```
GET /
```
Devuelve información general de la API y endpoints disponibles.

#### 2. Verificar estado del servidor
```
GET /api/health
```
Respuesta:
```json
{
    "status": "ok",
    "mensaje": "El servidor está funcionando correctamente"
}
```

#### 3. Cifrar texto
```
POST /api/vigenere/cifrar
Content-Type: application/json

{
    "texto": "Hola Mundo",
    "clave": "secreto"
}
```

Respuesta exitosa (200):
```json
{
    "texto_cifrado": "UKPQ MXAHZ",
    "longitud_original": 10,
    "longitud_cifrado": 11
}
```

Respuesta con error (400):
```json
{
    "error": "La clave debe tener al menos 3 caracteres"
}
```

#### 4. Descifrar texto
```
POST /api/vigenere/descifrar
Content-Type: application/json

{
    "texto": "UKPQ MXAHZ",
    "clave": "secreto"
}
```

Respuesta exitosa (200):
```json
{
    "texto_descifrado": "HOLA MUNDO",
    "longitud_cifrado": 11,
    "longitud_descifrado": 10
}
```

---

## 🎨 Temas y Estilos

La aplicación utiliza un esquema de colores inspirado en Hogwarts:

- **Colores Principales**:
  - Oro: `#d4af37`
  - Marrón oscuro: `#3b2f2f`, `#4b3b3b`
  - Fondo: Gradiente de gris oscuro a marrón

- **Archivo CSS**: `styles.css` en la carpeta resources

Para personalizar los colores, edita el archivo CSS directamente.

---

## 📝 Validaciones

La aplicación valida:

- ✅ Campos requeridos (texto y clave)
- ✅ Caracteres válidos (sin emoticonos ni símbolos especiales)
- ✅ Longitud mínima de clave (3 caracteres)
- ✅ Clave no puede estar vacía

Mensajes de error descriptivos aparecen en alertas para mayor claridad.

---

## 📊 Logging

Los logs se guardan en el archivo `api_cifrado_vigenere.log` en la carpeta del backend.

Formato:
```
2025-11-13 10:56:20 - app - ERROR - La clave debe tener al menos 3 caracteres
```

Los logs incluyen:
- Fecha y hora
- Nivel de severidad (INFO, ERROR, WARNING)
- Mensaje descriptivo

---

## 🐛 Solución de Problemas

### El servidor Flask no inicia
- Verifica que Python 3.8+ está instalado
- Instala las dependencias: `pip install -r requirements.txt`
- Cambia la dirección IP en `app.py` a tu máquina local

### La aplicación JavaFX no se conecta a la API
- Verifica que el servidor Flask está ejecutándose
- Comprueba que la URL en `APIClient.java` es correcta
- Revisa el cortafuegos y puertos (puerto 5000)

### Errores en la interfaz gráfica
- Verifica que JavaFX SDK 22 está configurado en el proyecto
- Limpia la caché: `mvn clean`
- Reconstruye: `mvn package`

---

## 👥 Autores

- **Gaizka** - Desarrollo Backend (API Flask)
- **Diego** - Desarrollo Frontend (Interfaz JavaFX)

---

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 📞 Contacto

Para reportar bugs o sugerencias, contacta a los desarrolladores.

---

**Última actualización**: 13 de noviembre de 2025