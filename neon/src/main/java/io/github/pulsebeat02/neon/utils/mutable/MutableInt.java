package io.github.pulsebeat02.neon.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MutableInt {

  private int number;

  MutableInt(final int number) {
    this.number = number;
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofNumber(@NotNull final Number number) {
    return ofInteger(number.intValue());
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofInteger(final int number) {
    return new MutableInt(number);
  }

  @Contract(value = "_ -> new", pure = true)
  public static @NotNull MutableInt ofString(@NotNull final String string) {
    return ofInteger(Integer.parseInt(string));
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
