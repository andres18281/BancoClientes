# 🏦 Banco API – Guía de Inicio Rápido

Este proyecto incluye una API bancaria construida con **Spring Boot**, autenticación **JWT**, y base de datos **PostgreSQL**.  
Este documento explica cómo levantar el entorno y cómo acceder a la documentación de la API.

---

## 1. 🚀 Configuración de la Base de Datos (PostgreSQL con Docker)

Antes de iniciar la aplicación Spring Boot, asegúrate de que PostgreSQL esté ejecutándose en el puerto **5432**, de acuerdo con la configuración en `application.properties`.

### ▶️ Levantar el contenedor PostgreSQL

Ejecuta el siguiente comando:

```bash
docker run -d \
  --name banco_postgres_db \
  -p 5432:5432 \
  -e POSTGRES_DB=banco_db \
  -e POSTGRES_USER=appuser \
  -e POSTGRES_PASSWORD=myappsecret \
  -v db-data:/var/lib/postgresql/data \
  postgres:14-alpine

Comandos útiles de Docker
Comando	Descripción
docker stop banco_postgres_db	Detiene el contenedor.
docker start banco_postgres_db	Inicia un contenedor detenido.
docker rm -f banco_postgres_db	Elimina el contenedor de forma forzada.

## 2. 📘 Acceso a la Documentación – Swagger UI

Una vez que la aplicación esté ejecutándose (por defecto en: `http://localhost:8080`), puedes acceder a la documentación interactiva Swagger UI en la siguiente URL:

🔗 **Swagger UI:**  

http://localhost:8080/swagger-ui/index.htm


Desde esta interfaz podrás visualizar todos los endpoints disponibles, sus modelos, parámetros y ejecutar pruebas directamente en el navegador.

---

## 3. 🔐 Autenticación – JWT Bearer Token

La API está protegida mediante **Spring Security + JWT**, por lo que todos los endpoints seguros requieren un token válido.

### 📝 Pasos para autenticarse

#### 1️⃣ Realizar Login  
Envía una petición al endpoint de autenticación, normalmente:

