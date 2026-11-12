workspace "PetCare Hub - C4 Architecture Model" {

model {
    // =====================================================
    // ACTORS
    // =====================================================

    veterinarian = person "Veterinario" {
        description "Profesional que realiza atención médica a mascotas."
        tags "Person"
    }

    petOwner = person "Dueño de Mascota" {
        description "Persona que tiene a su cargo una o más mascotas."
        tags "Person"
    }

    visitor = person "Visitante" {
        description "Usuario que consulta información pública del sistema."
        tags "Visitor"
    }

    // =====================================================
    // EXTERNAL SYSTEMS
    // =====================================================

    roboflow = softwareSystem "Roboflow" {
        description "Servicio externo para análisis de imágenes mediante Computer Vision."
        tags "ExternalSystem"
    }

    openai = softwareSystem "OpenAI API" {
        description "Servicio externo basado en modelos LLM para asistencia clínica."
        tags "ExternalSystem"
    }

    stripe = softwareSystem "Stripe" {
        description "Servicio externo para procesamiento de pagos y suscripciones."
        tags "ExternalSystem"
    }

    amazonS3 = softwareSystem "Amazon S3" {
        description "Servicio externo para almacenamiento de imágenes clínicas y resultados."
        tags "ExternalSystem"

    }

    // =====================================================
    // SOFTWARE SYSTEM
    // =====================================================
    petcare = softwareSystem "PetCare Hub" {
        description "Sistema multiplataforma para gestión de historias clínicas veterinarias y asistencia clínica basada en IA."
        tags "MainSystem"

        // =====================================================
        // CONTAINERS
        // =====================================================

        landingPage = container "Landing Page" {
             description "Página pública informativa del producto."
             technology "Astro, HTML, CSS, JavaScript"
             tags "WebApplication"

             staticWebUI = component "Static Web UI" {
                  description "Muestra información pública del producto, beneficios y características."
                  technology "Astro"
             }

             servicePresentation = component "Service Presentation Component" {
                 description "Presenta las funcionalidades principales de PetCare Hub."
                 technology "Astro"
             }

             accessRedirect = component "Application Access Redirect" {
                 description "Redirecciona usuarios hacia la aplicación web."
                 technology "Astro"
             }
        }

    webApplication = container "PetCare Hub Web Application" {
        description "Aplicación web para veterinarios y propietarios de mascotas."
        technology "Vue.js"
        tags "WebApplication"

        // COMPONENTS WEB APPLICATION
        authenticationUI = component "Authentication UI" {
            description "Interfaz para inicio de sesión y acceso al sistema."
            technology "Vue.js"
        }

        petProfileUI = component "Pet Profile UI" {
            description "Permite consultar información de propietarios y mascotas."
            technology "Vue.js"
        }

        clinicalHistoryUI = component "Clinical History UI" {
            description "Permite visualizar historial clínico de mascotas."
            technology "Vue.js"
        }

        clinicalAttentionUI = component "Clinical Attention UI" {
            description "Permite gestionar información de atención veterinaria."
            technology "Vue.js"
        }

        examsUI = component "Clinical Exams UI" {
            description "Permite consultar resultados de exámenes."
            technology "Vue.js"
        }

        aiAssistanceUI = component "AI Assistance UI" {
            description "Muestra asistencia clínica generada mediante IA."
            technology "Vue.js"
        }

        subscriptionUI = component "Subscription UI"  {
            description "Gestiona visualización de planes y suscripciones."
            technology "Vue.js"
        }
    }

        mobileApplication = container "PetCare Hub Mobile Application" {
            description "Aplicación móvil multiplataforma para gestión clínica."
            technology "Flutter"
            tags "MobileApplication"

            // COMPONENTS MOBILE APPLICATION
            mobileAuthentication = component "Authentication Mobile UI" {
                description "Gestiona inicio de sesión y registro desde dispositivos móviles."
                technology "Flutter"
            }

            mobilePetProfile = component "Pet Profile Mobile UI" {
                description "Consulta información de mascotas registradas."
                technology "Flutter"
            }

            mobileClinicalHistory = component "Clinical History Mobile UI" {
                description "Consulta historial clínico de mascotas."
                technology "Flutter"
            }

            mobileAppointments = component "Clinical Attention Mobile UI" {
                description "Consulta información relacionada a atenciones veterinarias."
                technology "Flutter"
            }

            mobileExams = component "Clinical Exams Mobile UI" {
                description "Consulta resultados de exámenes."
                technology "Flutter"
            }

            mobileAI = component "AI Assistance Mobile UI" {
                description "Visualiza sugerencias generadas mediante IA."
                technology "Flutter"
            }

            mobileSubscriptions = component "Subscription Mobile UI"    {
                description "Gestiona planes y suscripciones."
                technology "Flutter"
            }

            mobileFacade = component "Mobile Facade" {
                description "Simplifica la comunicación entre componentes móviles y servicios backend."
                technology "Flutter"
            }
        }

        apiRest = container "PetCare Platform REST API" {
            description "Backend principal implementado como Monolito Modular basado en DDD."
            technology "Spring Boot"
            tags "RestAPI"

            // =====================================================
            // COMPONENTS
            // =====================================================

            iamContext = component "Identity & Access Management Context" {
                description "Gestiona usuarios, autenticación, autorización y permisos."
                technology "Spring Boot"
            }

            profilesContext = component "Profiles & Organization Context" {
                description "Gestiona propietarios, mascotas, veterinarios y clínicas."
                technology "Spring Boot"
            }

            clinicalAttentionContext = component "Clinical Attention Context" {
                description "Gestiona consultas clínicas, anamnesis, diagnósticos, tratamientos y recetas."
                technology "Spring Boot"
            }

            medicalHistoryContext = component "Medical History Context" {
                description "Gestiona la historia clínica longitudinal de las mascotas."
                technology "Spring Boot"
            }

            clinicalExamsContext = component "Clinical Exams Context" {
                description "Gestiona solicitudes, seguimiento y resultados de exámenes clínicos."
                technology "Spring Boot"
            }

            aiAssistanceContext = component "AI Assistance Context" {
                description "Gestiona Computer Vision y asistencia mediante modelos LLM."
                technology "Spring Boot"
            }

            paymentContext = component "Payment & Subscriptions Context" {
                description "Gestiona planes, pagos y suscripciones."
                technology "Spring Boot"
            }

        }

        database = container "PetCare Hub PostgreSQL Database" {
            description "Base de datos principal del sistema."
            technology "PostgreSQL"
            tags "Database"

        }

    }

    // =====================================================
    // SYSTEM CONTEXT RELATIONSHIPS
    // =====================================================

    visitor -> petcare "Consulta información pública"

    veterinarian -> petcare "Gestiona atención clínica"

    petOwner -> petcare "Consulta información clínica"

    petcare -> roboflow "Analiza imágenes clínicas"

    petcare -> openai "Solicita asistencia basada en LLM"

    petcare -> stripe "Procesa pagos"

    petcare -> amazonS3 "Almacena archivos clínicos"

    // =====================================================
    // CONTAINER RELATIONSHIPS
    // =====================================================

    visitor -> landingPage "Accede al contenido público"

    landingPage -> webApplication "Redirige al sistema"

    veterinarian -> webApplication "Utiliza plataforma web"

    petOwner -> webApplication "Consulta información"

    veterinarian -> mobileApplication "Utiliza aplicación móvil"

    petOwner -> mobileApplication "Utiliza aplicación móvil"

    webApplication -> apiRest "Solicita servicios REST"

    mobileApplication -> apiRest "Solicita servicios REST"

    apiRest -> database "Lee y almacena información"

    apiRest -> roboflow "Solicita análisis Computer Vision"

    apiRest -> openai "Solicita procesamiento LLM"

    apiRest -> stripe "Gestiona pagos"

    apiRest -> amazonS3 "Gestiona almacenamiento"

    // =====================================================
    // WEB APPLICATION COMPONENT RELATIONSHIPS
    // =====================================================

    authenticationUI -> apiRest "Solicita autenticación"

    petProfileUI -> apiRest "Consulta perfiles y mascotas"

    clinicalHistoryUI -> apiRest "Consulta historial clínico"

    clinicalAttentionUI -> apiRest "Gestiona atención clínica"

    examsUI -> apiRest "Consulta exámenes"

    aiAssistanceUI -> apiRest "Consulta asistencia IA"

    subscriptionUI -> apiRest "Gestiona suscripciones"

    // =====================================================
    // LANDING PAGE COMPONENT RELATIONSHIPS
    // =====================================================

    staticWebUI -> servicePresentation "Muestra información del producto"

    servicePresentation -> accessRedirect "Redirecciona usuarios"

    accessRedirect -> webApplication "Accede a aplicación web"

    // =====================================================
    // MOBILE APPLICATION COMPONENT RELATIONSHIPS
    // =====================================================

    mobileAuthentication -> mobileFacade "Solicita autenticación"

    mobilePetProfile -> mobileFacade "Consulta mascotas"

    mobileClinicalHistory -> mobileFacade "Consulta historial"

    mobileAppointments -> mobileFacade "Consulta atención clínica"

    mobileExams -> mobileFacade "Consulta resultados"

    mobileAI -> mobileFacade "Consulta asistencia IA"

    mobileSubscriptions -> mobileFacade "Gestiona suscripciones"

    mobileFacade -> apiRest "Consume servicios REST"

    // =====================================================
    // COMPONENT RELATIONSHIPS REST API
    // =====================================================
    iamContext -> profilesContext "Utiliza identidad de usuario"
    iamContext -> paymentContext "Valida usuario"
    iamContext -> database "Persiste información de usuarios"
    profilesContext -> database "Persiste perfiles"
    profilesContext -> clinicalAttentionContext "Use"
    clinicalAttentionContext -> medicalHistoryContext "Consulta antecedentes clínicos"
    clinicalAttentionContext -> clinicalExamsContext "Consulta resultados de exámenes"
    clinicalAttentionContext -> aiAssistanceContext "Solicita apoyo diagnóstico"
    aiAssistanceContext -> medicalHistoryContext "Obtiene contexto clínico"
    aiAssistanceContext -> clinicalExamsContext "Obtiene información de exámenes"
    clinicalAttentionContext -> database "Persiste atenciones"
    medicalHistoryContext -> database "Persiste historial"
    clinicalExamsContext -> database "Persiste exámenes"
    paymentContext -> database "Persiste suscripciones"
    paymentContext -> stripe "Pagos"
    paymentContext -> aiAssistanceContext "Solicita notificación sobre la próxima finalización de la suscripción."
    aiAssistanceContext -> openai "Solicita servicios externos"
    aiAssistanceContext -> roboflow "Computer Vision"
    medicalHistoryContext -> amazonS3 "Almacena imágenes clínicas y resultados de exámenes"
}
views {
    // =====================================================
    // C4 LEVEL 1
    // SYSTEM CONTEXT DIAGRAM
    // =====================================================
    systemContext petcare "PetCareHub-SystemContext" {
        include *
        autolayout lr
    }
    // =====================================================
    // C4 LEVEL 2
    // CONTAINER DIAGRAM
    // =====================================================
    container petcare "PetCareHub-ContainerDiagram" {
        include *
        autolayout lr
    }
    // =====================================================
    // C4 LEVEL 3
    // COMPONENT DIAGRAM
    // API REST SPRING BOOT
    // =====================================================
    component apiRest "PetCareHub-API-ComponentDiagram" {
        include *
        autolayout lr
    }
    component landingPage "PetCareHub-LandingPage-Components" {
    include *
    autolayout lr
}
component webApplication "PetCareHub-WebApplication-Components" {
    include *
    autolayout lr
}
component mobileApplication "PetCareHub-MobileApplication-Components" {
    include *
    autolayout lr
}
    // =====================================================
    // STYLES C4
    // =====================================================
    styles {
        // -----------------------------
        // PERSON
        // -----------------------------
        element "Person" {
            shape Person
            background "#1F4E79"
            color "#FFFFFF"
            fontSize 22
        }
        element "Visitor" {
            shape Person
            background "#808080"
            color "#FFFFFF"
        }
        // -----------------------------
        // SOFTWARE SYSTEM
        // -----------------------------
        element "MainSystem" {
            shape RoundedBox
            background "#0066CC"
            color "#FFFFFF"
        }
        element "ExternalSystem" {
            shape RoundedBox
            background "#8C8C8C"
            color "#FFFFFF"
        }
        // -----------------------------
        // CONTAINERS
        // -----------------------------
        element "WebApplication" {
            shape WebBrowser
            background "#438AD5"
            color "#FFFFFF"
        }
        element "MobileApplication" {
            shape MobileDevicePortrait
            background "#438AD5"
            color "#FFFFFF"
        }
        element "RestAPI" {
            shape RoundedBox
            background "#438AD5"
            color "#FFFFFF"
        }
        element "Database" {
            shape Cylinder
            background "#4B89DC"
            color "#FFFFFF"
        }
        // -----------------------------
        // COMPONENTS
        // -----------------------------
        element "Component" {

            shape Component
            background "#85B7E8"
            color "#000000"
        }
    }
}
}
