package io.github.pulsebeat02.neon.utils.mutable;

import org.jetbrains.annotations.NotNull;

public final class MutableInt {

  private int number;

  public MutableInt(final int number) {
    this.number = number;
  }

  public void increment() {
    this.number++;
  }

  public void decrement() {
    this.number--;
  }

  public void add(final int add) {
    this.number += add;
  }

  public void subtract(final int subtract) {
    this.number -= subtract;
  }

  public void set(final int newNumber) {
    this.number = newNumber;
  }

  public int getNumber() {
    return this.number;
  }
}
