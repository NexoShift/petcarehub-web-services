package com.nexoshift.petcarehub.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Converts Java names to the SQL conventions of section 7.1.3:
 * tables in plural snake_case (Pet -> pets) and columns in snake_case (petId -> pet_id).
 */
public class SnakeCaseWithPluralizedTablePhysicalNamingStrategy implements PhysicalNamingStrategy {

  @Override
  public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return toSnakeCase(identifier);
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return toSnakeCase(identifier);
  }

  @Override
  public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return pluralize(toSnakeCase(identifier));
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return toSnakeCase(identifier);
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
    return toSnakeCase(identifier);
  }

  private Identifier toSnakeCase(Identifier identifier) {
    if (identifier == null) {
      return null;
    }
    String snakeCase = identifier.getText()
        .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
        .replace('.', '_')
        .toLowerCase();
    return Identifier.toIdentifier(snakeCase, identifier.isQuoted());
  }

  private Identifier pluralize(Identifier identifier) {
    if (identifier == null) {
      return null;
    }
    String name = identifier.getText();
    String plural;
    if (name.endsWith("s")) {
      plural = name;
    } else if (name.endsWith("y") && name.length() > 1 && !isVowel(name.charAt(name.length() - 2))) {
      plural = name.substring(0, name.length() - 1) + "ies";
    } else if (name.endsWith("x") || name.endsWith("ch") || name.endsWith("sh")) {
      plural = name + "es";
    } else {
      plural = name + "s";
    }
    return Identifier.toIdentifier(plural, identifier.isQuoted());
  }

  private boolean isVowel(char character) {
    return "aeiou".indexOf(character) >= 0;
  }
}
