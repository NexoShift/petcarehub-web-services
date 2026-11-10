package com.nexoshift.petcarehub.profiles.support;

import java.lang.reflect.Field;

/**
 * Ayudas para los tests unitarios de Profiles (sin base de datos).
 * Los consultorios y asignaciones se buscan por id dentro de Clinic; sin base de datos el id
 * queda en null, así que aquí se pone a mano, como lo haría JPA al guardar.
 */
public final class ProfilesTestSupport {

  private ProfilesTestSupport() {
  }

  /** Pone el id generado de una entidad o agregado (campo "id" de la clase base). */
  public static <T> T withId(T entity, Long id) {
    var type = entity.getClass();
    while (type != null) {
      try {
        Field field = type.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return entity;
      } catch (NoSuchFieldException exception) {
        type = type.getSuperclass();
      } catch (IllegalAccessException exception) {
        throw new IllegalStateException(exception);
      }
    }
    throw new IllegalStateException("No id field in " + entity.getClass());
  }
}
