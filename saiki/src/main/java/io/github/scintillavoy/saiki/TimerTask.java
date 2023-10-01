package io.github.scintillavoy.saiki;

import java.time.Duration;
import java.time.Instant;

abstract class TimerTask implements Runnable, Comparable<TimerTask> {
  final Object lock = new Object();

  int state = VIRGIN;
  static final int VIRGIN = 0;
  static final int SCHEDULED = 1;
  static final int EXECUTED = 2;
  static final int CANCELLED = 3;

  Instant nextExecutionTime;
  Duration period;

  TimerTask(Instant nextExecutionTime) {
    this.nextExecutionTime = nextExecutionTime;
    this.period = Duration.ZERO;
  }

  TimerTask(Instant nextExecutionTime, Duration period) {
    this.nextExecutionTime = nextExecutionTime;
    this.period = period;
  }

  public abstract void run();

  public synchronized boolean cancel() {
    synchronized (lock) {
      boolean result = (state == SCHEDULED);
      state = CANCELLED;
      return result;
    }
  }

  public long scheduledExecutionTime() {
    synchronized (lock) {
      return nextExecutionTime.toEpochMilli();
    }
  }

  @Override
  public synchronized int compareTo(TimerTask o) {
    if (this.nextExecutionTime.isBefore(o.nextExecutionTime)) {
      return -1;
    }
    if (this.nextExecutionTime.isAfter(o.nextExecutionTime)) {
      return 1;
    }
    return 0;
  }
}
