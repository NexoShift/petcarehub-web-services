package com.nexoshift.petcarehub.iam.domain.model.entities;

import com.nexoshift.petcarehub.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Authorization role. The three roles are seeded when the application starts. */
@Entity
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(length = 30, nullable = false, unique = true)
  private Roles name;

  protected Role() {
  }

  public Role(Roles name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public Roles getName() {
    return name;
  }

  public String getStringName() {
    return name.name();
  }
}
