package io.github.pulsebeat02.neon.utils.mutable;

public final class MutableLong {

  private long number;

  public MutableLong(final long number) {
    this.number = number;
  }

  public void increment() {
    this.number++;
  }

  public void decrement() {
    this.number--;
  }

  public void add(final long add) {
    this.number += add;
  }

  public void subtract(final long subtract) {
    this.number -= subtract;
  }

  public void set(final long newNumber) {
    this.number = newNumber;
  }

  public long getNumber() {
    return this.number;
  }
}
