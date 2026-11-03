package com.nexoshift.petcarehub.clinicalattention.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.AbstractAggregateRoot;

/** Reflection helpers so unit tests can work without a database. */
public final class ClinicalTestSupport {

  private ClinicalTestSupport() {
  }

  /** Sets the generated id of an aggregate or entity, as the database would do. */
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

  /** Domain events registered in the aggregate and not published yet. */
  @SuppressWarnings("unchecked")
  public static List<Object> domainEvents(AbstractAggregateRoot<?> aggregate) {
    try {
      Method method = AbstractAggregateRoot.class.getDeclaredMethod("domainEvents");
      method.setAccessible(true);
      return new ArrayList<>((Collection<Object>) method.invoke(aggregate));
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public static <E> List<E> domainEvents(AbstractAggregateRoot<?> aggregate, Class<E> type) {
    return domainEvents(aggregate).stream().filter(type::isInstance).map(type::cast).toList();
  }

  public static void clearDomainEvents(AbstractAggregateRoot<?> aggregate) {
    try {
      Method method = AbstractAggregateRoot.class.getDeclaredMethod("clearDomainEvents");
      method.setAccessible(true);
      method.invoke(aggregate);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException(exception);
    }
  }
}
