// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.model;

import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestWorld;
import io.vlingo.lattice.model.object.ObjectTypeRegistry;
import io.vlingo.schemata.NoopDispatcher;
import io.vlingo.schemata.SchemataConfig;
import io.vlingo.schemata.codegen.TypeDefinitionCompilerActor;
import io.vlingo.schemata.codegen.TypeDefinitionMiddleware;
import io.vlingo.schemata.codegen.backend.Backend;
import io.vlingo.schemata.codegen.backend.java.JavaBackend;
import io.vlingo.schemata.codegen.parser.AntlrTypeParser;
import io.vlingo.schemata.codegen.parser.TypeParser;
import io.vlingo.schemata.codegen.processor.Processor;
import io.vlingo.schemata.codegen.processor.types.CacheTypeResolver;
import io.vlingo.schemata.codegen.processor.types.ComputableTypeProcessor;
import io.vlingo.schemata.codegen.processor.types.TypeResolver;
import io.vlingo.schemata.codegen.processor.types.TypeResolverProcessor;
import io.vlingo.schemata.infra.persistence.SchemataObjectStore;
import io.vlingo.schemata.model.Id.*;
import io.vlingo.schemata.model.SchemaVersion.Specification;
import io.vlingo.schemata.resource.data.SchemaVersionData;
import io.vlingo.symbio.store.dispatch.Dispatcher;
import io.vlingo.symbio.store.object.ObjectStore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.vlingo.schemata.LambdaMatcher.matches;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SchemaVersionTest {
  private ObjectTypeRegistry registry;
  private SchemaVersion simpleSchemaVersion;
  private SchemaVersionId simpleSchemaVersionId;
  private SchemaVersion basicTypesSchemaVersion;
  private SchemaVersionId basicTypesSchemaVersionId;
  private ObjectStore objectStore;
  private TypeDefinitionMiddleware typeDefinitionMiddleware;
  private World world;
  private Stage stage;
  private SchemaVersionState firstVersion;

  @Before
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void setUp() throws Exception {
    World world = TestWorld.startWithDefaults(getClass().getSimpleName()).world();
    TypeParser typeParser = world.actorFor(TypeParser.class, AntlrTypeParser.class);
    Dispatcher dispatcher = new NoopDispatcher();

    final SchemataObjectStore schemataObjectStore = SchemataObjectStore.instance(SchemataConfig.forRuntime("test"));

    ObjectStore objectStore = schemataObjectStore.objectStoreFor(world, dispatcher, schemataObjectStore.persistentMappers());
    final ObjectTypeRegistry registry = new ObjectTypeRegistry(world);
    schemataObjectStore.register(registry, objectStore);

    TypeResolver typeResolver = new CacheTypeResolver();

    typeDefinitionMiddleware = world.actorFor(TypeDefinitionMiddleware.class, TypeDefinitionCompilerActor.class,
        typeParser,
        Arrays.asList(
            world.actorFor(Processor.class, ComputableTypeProcessor.class),
            world.actorFor(Processor.class, TypeResolverProcessor.class, typeResolver)
        ),
        world.actorFor(Backend.class, JavaBackend.class)
    );

    simpleSchemaVersionId = SchemaVersionId.uniqueFor(SchemaId.uniqueFor(ContextId.uniqueFor(UnitId.uniqueFor(OrganizationId.unique()))));
    simpleSchemaVersion = world.actorFor(SchemaVersion.class, SchemaVersionEntity.class, simpleSchemaVersionId);
    firstVersion = simpleSchemaVersionState();

    basicTypesSchemaVersionId = SchemaVersionId.uniqueFor(SchemaId.uniqueFor(ContextId.uniqueFor(UnitId.uniqueFor(OrganizationId.unique()))));
    basicTypesSchemaVersion = world.actorFor(SchemaVersion.class, SchemaVersionEntity.class, basicTypesSchemaVersionId);
    completeBasicTypesSchemaVersionState();
  }

  @Test
  public void equalSpecificationsAreCompatible() {
    final SchemaVersionState firstVersion = simpleSchemaVersionState();

    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification(firstVersion.specification.value)));

    assertCompatible("Versions with only added attributes must be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test
  public void schemaVersionWithAddedAttributeIsCompatible() {
    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification("event Foo { " +
            "string bar\n" +
            "string baz\n" +
            "}")));

    assertCompatible("Versions with only added attributes must be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test
  public void schemaVersionWithRemovedAttributeIsNotCompatible() {
    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification("event Foo { " +
            "string baz\n" +
            "}")));

    assertIncompatible("Versions with only removed attributes must not be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test
  public void schemaVersionWithAddedAndRemovedAttributesAreNotCompatible() {
    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification("event Foo { " +
            "string baz\n" +
            "}")));

    assertIncompatible("Versions with added and removed attributes must not be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test
  public void schemaVersionWithTypeChangesAreNotCompatible() {
    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification("event Foo { " +
            "int bar\n" +
            "}")));

    assertIncompatible("Versions with reordered attributes must not be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test
  public void schemaVersionWithReorderedAttributesAreNotCompatible() {
    final SchemaVersionData secondVersion = SchemaVersionData.from(firstVersion.withSpecification(
        new Specification("event Foo { " +
            "string baz\n" +
            "string bar\n" +
            "}")));

    assertIncompatible("Versions with added and removed attributes must not be compatible",
        simpleSchemaVersion.diff(typeDefinitionMiddleware, secondVersion).await());
  }

  @Test public void identicalSpecificationsHaveNoDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { string bar }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware,dst).await();
    assertThat(diff.changes().size(),is(0));
  }

  @Test public void removalIsTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware, dst).await();
    assertThat(diff.changes().size(),is(1));
    assertThat(diff.changes().get(0).subject, is(Change.Subject.FIELD));
    assertThat(diff.changes().get(0).type, is(Change.Type.REMOVAL));
  }

  @Test public void multipleRemovalsAreTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { \n" +
      "type eventType\n"+
      "version eventVersion\n"+
      "byte byteAttribute\n"+
      "double doubleAttribute\n"+
      "int intAttribute\n"+
      "short shortAttribute\n"+
      "}",null,null,null, null);

    SpecificationDiff diff = basicTypesSchemaVersion.diff(typeDefinitionMiddleware, dst).await();
    assertThat(diff.changes().size(),is(6));
    assertThat(diff.changes(), everyItem(matches(c -> c.type == Change.Type.REMOVAL, "All changes must be removals")));
    assertThat(diff.changes(), everyItem(matches(c -> c.subject == Change.Subject.FIELD, "All changes must have occurred on fields")));
  }

  @Test public void additionIsTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { string bar\nstring baz\nstring qux }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware,dst).await();
    assertThat(diff.changes().size(),is(2));
    assertThat(diff.changes(), everyItem(matches(c -> c.type == Change.Type.ADDITION, "All changes must be additions")));
    assertThat(diff.changes(), everyItem(matches(c -> c.subject == Change.Subject.FIELD, "All changes must have occurred on fields")));
  }

  @Test public void renamingIsTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { string baz }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware,dst).await();
    assertThat(diff.changes().size(),is(2));
    assertThat(diff.changes().stream().filter(c -> c.type == Change.Type.ADDITION).count(), is(1L));
    assertThat(diff.changes().stream().filter(c -> c.type == Change.Type.REMOVAL).count(), is(1L));
  }

  @Test public void typeChangeIsTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Bar { string bar }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware,dst).await();
    assertThat(diff.changes().size(),is(1));
    assertThat(diff.changes().get(0).subject, is(Change.Subject.TYPE));
    assertThat(diff.changes().get(0).type, is(Change.Type.CHANGE));
    // TODO: Assert change values
  }

  @Test public void fieldTypeChangeIsTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Foo { int bar }",null,null,null, null);

    SpecificationDiff diff = simpleSchemaVersion.diff(typeDefinitionMiddleware,dst).await();
    assertThat(diff.changes().size(),is(1));
    assertThat(diff.changes().get(0).subject, is(Change.Subject.FIELD));
    assertThat(diff.changes().get(0).type, is(Change.Type.CHANGE));
    // TODO: Assert change values
  }

  @Test public void mixedChangesArtTrackedInDiff() {
    SchemaVersionData dst = SchemaVersionData.just("event Bar { " +
      "timestamp at\n"+
      "string newStringAttribute\n" +
      "version eventVersion\n"+
      "int charAttribute\n"+
      "int intAttribute\n"+
      "long longAttribute\n"+
      "short shortAttribute\n"+
      "string stringAttribute\n"+
      "}",null,null,null, null);

    SpecificationDiff diff = basicTypesSchemaVersion.diff(typeDefinitionMiddleware, dst).await();
    assertThat(diff.changes().size(),is(10));

    List<Change> typeChanges = diff.changes().stream().filter(c -> c.subject == Change.Subject.TYPE).collect(toList());
    List<Change> additions = diff.changes().stream().filter(c -> c.type == Change.Type.ADDITION).collect(toList());
    List<Change> removals = diff.changes().stream().filter(c -> c.type == Change.Type.REMOVAL).collect(toList());
    List<Change> changes = diff.changes().stream().filter(c -> c.type == Change.Type.CHANGE).collect(toList());

    assertEquals(typeChanges.size(), 1);
    assertThat(typeChanges, hasItem(Change.ofType("Foo", "Bar")));


    assertEquals(removals.size(), 6);
    assertThat(removals, hasItems(
      Change.removalOfField("eventType"),
      Change.removalOfField("occurredOn"),
      Change.removalOfField("booleanAttribute"),
      Change.removalOfField("byteAttribute"),
      Change.removalOfField("doubleAttribute"),
      Change.removalOfField("floatAttribute")));


    assertEquals(changes.size(), 2);
    assertThat(changes, hasItems(
      Change.ofType("Foo","Bar"),
      Change.ofFieldType("char","int")));

    assertEquals(additions.size(), 2);
    assertThat(additions, hasItems(
      Change.additionOfField("newStringAttribute"),
      Change.additionOfField("at")));
  }


  private SchemaVersionState simpleSchemaVersionState() {
    return simpleSchemaVersion.defineWith(
        new Specification("event Foo { " +
            "string bar\n" +
            "}"),
        "description",
        new SchemaVersion.Version("0.0.0"),
        new SchemaVersion.Version("1.0.0"))
        .await();
  }

  private SchemaVersionState completeBasicTypesSchemaVersionState() {
    return basicTypesSchemaVersion.defineWith(
      new Specification("event Foo { " +
        "type eventType\n"+
        "timestamp occurredOn\n"+
        "version eventVersion\n"+
        "boolean booleanAttribute\n"+
        "byte byteAttribute\n"+
        "char charAttribute\n"+
        "double doubleAttribute\n"+
        "float floatAttribute\n"+
        "int intAttribute\n"+
        "long longAttribute\n"+
        "short shortAttribute\n"+
        "string stringAttribute\n"+
        "}"),
      "description",
      new SchemaVersion.Version("0.0.0"),
      new SchemaVersion.Version("1.0.0"))
      .await();
  }

  private static void assertCompatible(String message, SpecificationDiff diff) {
    assertTrue(message, diff.isCompatible());
  }

  private static void assertIncompatible(String message, SpecificationDiff diff) {
    assertFalse(message, diff.isCompatible());
  }


}
