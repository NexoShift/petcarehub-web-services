package com.nexoshift.petcarehub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Comprueba que todo el contexto de Spring arranca (todos los BCs juntos).
 * Usa el perfil "test" (H2 en memoria); sin él intenta conectarse a MySQL y falla.
 */
@SpringBootTest
@ActiveProfiles("test")
class PetcareHubPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
