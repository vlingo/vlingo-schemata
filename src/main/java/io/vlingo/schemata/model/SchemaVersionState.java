// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.model;

import java.util.Map;

import io.vlingo.schemata.model.Id.SchemaVersionId;
import io.vlingo.symbio.store.object.MapQueryExpression.FluentMap;
import io.vlingo.symbio.store.object.StateObject;

public class SchemaVersionState extends StateObject {
  private static final long serialVersionUID = 1L;

  public final SchemaVersionId schemaVersionId;
  public final String description;
  public final SchemaVersion.Specification specification;
  public final SchemaVersion.Status status;
  public final SchemaVersion.Version previousVersion;
  public final SchemaVersion.Version currentVersion;

  public static SchemaVersionState from(final SchemaVersionId schemaVersionId) {
    return new SchemaVersionState(schemaVersionId);
  }

  public static SchemaVersionState from(
          final long id,
          final long version,
          final SchemaVersionId schemaVersionId,
          final SchemaVersion.Specification specification,
          final String description,
          final SchemaVersion.Status status,
          final SchemaVersion.Version previousVersion,
          final SchemaVersion.Version currentVersion) {
    return new SchemaVersionState(
            id,
            version,
            schemaVersionId,
            specification,
            description,
            status,
            previousVersion,
            currentVersion);
  }

  public SchemaVersionState asPublished() {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, this.specification, this.description, SchemaVersion.Status.Published, this.previousVersion, this.currentVersion);
  }

  public SchemaVersionState asDeprecated() {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, this.specification, this.description, SchemaVersion.Status.Deprecated, this.previousVersion, this.currentVersion);
  }

  public SchemaVersionState asRemoved() {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, this.specification, this.description, SchemaVersion.Status.Removed, this.previousVersion, this.currentVersion);
  }

  public SchemaVersionState defineWith(final String description, final SchemaVersion.Specification specification, final SchemaVersion.Version previousVersion, final SchemaVersion.Version currentVersion) {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, specification, description, SchemaVersion.Status.Draft, previousVersion, currentVersion);
  }

  public SchemaVersionState withSpecification(final SchemaVersion.Specification specification) {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, specification, this.description, this.status, this.previousVersion, this.currentVersion);
  }

  public SchemaVersionState withDescription(final String description) {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, this.specification, description, this.status, this.previousVersion, this.currentVersion);
  }

  public SchemaVersionState withVersion(final SchemaVersion.Version currentVersion) {
    return new SchemaVersionState(this.persistenceId(), this.version() + 1, this.schemaVersionId, this.specification, this.description, this.status, this.previousVersion, currentVersion);
  }

  @Override
  public Map<String, Object> queryMap() {
    return FluentMap
            .has("organizationId", schemaVersionId.schemaId.contextId.unitId.organizationId.value)
            .and("unitId", schemaVersionId.schemaId.contextId.unitId.value)
            .and("contextId", schemaVersionId.schemaId.contextId.value)
            .and("schemaId", schemaVersionId.schemaId.value)
            .and("schemaVersionId", schemaVersionId.value);
  }

  @Override
  public int hashCode() {
    return 31 * this.schemaVersionId.value.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != getClass()) {
      return false;
    } else if (this == other) {
      return true;
    }

    final SchemaVersionState otherState = (SchemaVersionState) other;

    return this.persistenceId() == otherState.persistenceId();
  }

  @Override
  public String toString() {
    return "SchemaVersionState[persistenceId=" + persistenceId() +
            " version=" + version() +
            " schemaVersionId=" + schemaVersionId.value +
            " specification=" + specification +
            " description=" + description +
            " status=" + status.name() +
            " previousVersion=" + previousVersion +
            " currentVersion=" + currentVersion + "]";
  }

  private SchemaVersionState(final SchemaVersionId schemaVersionId) {
    this(Unidentified, 0,
         schemaVersionId,
         new SchemaVersion.Specification("(unknown)"),
         "",
         SchemaVersion.Status.Draft,
         new SchemaVersion.Version("0.0.0"),
         new SchemaVersion.Version("0.0.0"));
  }

  private SchemaVersionState(
          final long id,
          final long version,
          final SchemaVersionId schemaVersionId,
          final SchemaVersion.Specification specification,
          final String description,
          final SchemaVersion.Status status,
          final SchemaVersion.Version previousVersion,
          final SchemaVersion.Version currentVersion) {
    super(id, version);
    this.schemaVersionId = schemaVersionId;
    this.specification = specification;
    this.description = description;
    this.status = status;
    this.previousVersion = previousVersion;
    this.currentVersion = currentVersion;
  }
}
