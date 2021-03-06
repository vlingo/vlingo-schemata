package io.vlingo.schemata.codegen.ast.values;

public class SingleValue<T> implements Value<T> {
  public final T value;

  public SingleValue(T value) {
    this.value = value;
  }

  @Override
  public String name() {
    return value.getClass().getSimpleName();
  }

  @Override
  public T value() {
    return value;
  }
}
