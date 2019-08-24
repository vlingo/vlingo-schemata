// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.resource.data;

import io.vlingo.schemata.model.UnitState;

public class UnitData {
  public final String organizationId;
  public final String unitId;
  public final String name;
  public final String description;

  public static UnitData from(final UnitState state) {
    return new UnitData(state.unitId.organizationId.value, state.unitId.value, state.name, state.description);
  }

  public static UnitData from(final String organizationId, final String unitId, final String name, final String description) {
    return new UnitData(organizationId, unitId, name, description);
  }

  public static UnitData just(final String name, final String description) {
    return new UnitData("", "", name, description);
  }

  @Override
  public String toString() {
    return "UnitData [organizationId=" + organizationId + " unitId=" + unitId + " name=" + name + " description=" + description + "]";
  }

  private UnitData(final String organizationId, final String unitId, final String name, final String description) {
    this.organizationId = organizationId;
    this.unitId = unitId;
    this.name = name;
    this.description = description;
  }
}
