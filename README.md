# Sistema de Gestión de Incidencias Municipales - Arquitectura de Microservicios Multi-Cloud ☁️🛡️

Ecosistema empresarial distribuido co-diseñado e implementado como proyecto de fin de carrera para resolver la ausencia total de herramientas digitales de atención ciudadana en el Ayuntamiento de Chilpancingo, obteniendo una evaluación de 100/100.

## 📌 El Problema
Actualmente, el municipio no cuenta con ninguna plataforma digital centralizada que permita capturar, procesar y dar seguimiento automatizado a los reportes de fallas e incidencias de la ciudadanía. Los reportes se gestionan de manera fragmentada o informal, provocando cuellos de botella administrativos y una falta de trazabilidad en las soluciones operativas de la ciudad.

## 💡 La Solución
Este ecosistema automatiza el flujo completo de incidencias mediante una **arquitectura basada en microservicios**. Su núcleo operativo distribuye la carga de procesamiento de forma híbrida en un entorno **Multi-Cloud** interconectado entre Oracle Cloud Infrastructure (OCI), AWS Elastic Beanstalk y Railway, asegurando alta disponibilidad y tolerancia a fallas.

---

## 🛠️ Core Tecnológico del Microservicio Central (Incidencias)

Este repositorio aloja el **orquestador central** del sistema, migrado estratégicamente de **Java 17 a Java 21** para garantizar la interoperabilidad, compatibilidad transaccional y máximo rendimiento del ecosistema distribuido del equipo.

* **Backend & Framework:** Java 21, Spring Boot 3 & Spring Data JPA.
* **Comunicación Externa (OpenFeign):** Clientes declarativos interconectados de forma síncrona para consultar módulos distribuidos de Autenticación, Clima, Evidencias, Ubicación y Notificaciones.
* **Patrón de Diseño & Mapeo:** Aislamiento estricto de datos separando Entidades relacionales (MySQL/Hibernate) de Objetos de Transferencia de Datos (DTOs), automatizado mediante **MapStruct**.
* **Seguridad Avanzada:** Filtros de seguridad perimetral (`JwtAuthenticationFilter`) encargados de validar la firma y propagación de tokens JWT.

---

## ⚙️ Estructura del Proyecto (Microservicio Core)

El diseño de este microservicio sigue principios sólidos de arquitectura limpia y separación estricta de responsabilidades:

src/main/java/edu/tecnm/

├── client/        # Clientes OpenFeign (Auth, Clima, Evidencia, Notificación)

├── config/        # Seguridad de Spring Security, Filtros JWT e Interceptores Feign

├── controller/    # Endpoint REST expuesto para la gestión de incidencias

├── dto/           # Data Transfer Objects y esquemas de respuesta desacoplados

├── entity/        # Modelos de persistencia para la base de datos relacional

├── exception/     # Manejo global y centralizado de excepciones (Handler)

├── mapper/        # Interfaces MapStruct para transferencia eficiente entre capas

├── orchestrator/  # Orquestador del flujo y lógica de negocio distribuida

├── repository/    # Interfaces de persistencia (Spring Data JPA)

└── util/          # Utilidades criptográficas y parsing de JWT tokens

## 🏗️ Flujo de Arquitectura de Red y Descubrimiento

El microservicio de Incidencias actúa como el núcleo orquestador distribuido del sistema. Al iniciar el servicio, este se registra dinámicamente en el servidor de descubrimiento **Netflix Eureka (SkyBridge)** hosteado en Railway. 

Cuando una petición web interceptada por el API Gateway llega al controlador, el ecosistema utiliza un **Patrón de Propagación de Tokens (Token Propagation)**: mediante interceptores nativos de Feign (`RequestInterceptor`), el sistema extrae de forma síncrona el token **JWT** del contexto de seguridad y lo inyecta automáticamente en las cabeceras de las peticiones salientes hacia los servicios distribuidos en la infraestructura multi-cloud, garantizando la persistencia de la identidad perimetral.

                  ┌─────────────────────────────────┐
                  │  EUREKA SERVER (SkyBridge) 🛰️   │
                  │  (Registro y Descubrimiento)    │
                  └────────────────┬────────────────┘
                                   ▲
         ┌─────────────────────────┼─────────────────────────┐
         │ Autenticación           │ Registro                │ Monitoreo
         ▼                         ▼                         
    [ API GATEWAY ] ──► [ MS CORE: INCIDENCIAS (Java) ]
    (Filtro JWT)                 │ 
                                 │ (Propagación JWT vía Feign Interceptors)
                                 │
                                 ├──► [ AuthClient ] ──────► Servicio de Identidad (Railway)
                                 ├──► [ ClimaClient ] ─────► API de Condiciones Climáticas
                                 ├──► [ EvidenciaClient ] ─► Gestión de Archivos (AWS Beanstalk)
                                 └──► [ Notificacion ] ────► Servidor de Alertas (AWS Beanstalk)


## 🛠️ Especificaciones de Interconexión y Resiliencia

El microservicio centralizado opera bajo estrictas políticas de tolerancia a fallas y desacoplamiento de infraestructura:

* **Consumos Declarativos:** Interconectado mediante interfaces `@FeignClient` limpias que apuntan a variables dinámicas mapeadas para entornos locales (`localhost`) y servidores de producción.
* **Mecanismos de Resiliencia:** Cuenta con la propiedad `spring.cloud.openfeign.circuitbreaker.enabled=true` activa, inyectando disyuntores lógicos (Circuit Breakers) para evitar caídas en cadena si alguno de los extremos en AWS o Railway experimenta degradación de red o latencia.

## 💻 Sincronización Full-Stack (Capa Cliente)

La interfaz de usuario del ecosistema municipal fue desarrollada como una Single Page Application (SPA) interactiva utilizando **React, Vite y JavaScript moderno (ES6+)**. 

El control de código y mis contribuciones lógicas aplicadas sobre las vistas responsivas y el consumo de las APIs enrutadas por el Gateway pueden auditarse directamente en el repositorio principal del equipo encargado de la capa cliente:
* **Módulo de Interfaz Web:** [Lizherk/AyuntamientoFront](https://github.com/Lizherk/AyuntamientoFront) (Contribuciones directas en diseño y consumo asíncrona de servicios distribuidos).
