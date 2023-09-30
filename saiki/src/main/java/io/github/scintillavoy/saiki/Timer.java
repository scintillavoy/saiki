package io.github.scintillavoy.saiki;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.PriorityQueue;

import io.github.scintillavoy.saiki.Clock.FakeClock;

public abstract class Timer {
  public static Timer of(Clock clock) {
    if (clock instanceof FakeClock) {
      return new FakeTimer((FakeClock) clock);
    }
    return new RealTimer();
  }

  public static Timer of(Clock clock, boolean isDaemon) {
    if (clock instanceof FakeClock) {
      return new FakeTimer((FakeClock) clock);
    }
    return new RealTimer(isDaemon);
  }

  public static Timer of(Clock clock, String name) {
    if (clock instanceof FakeClock) {
      return new FakeTimer((FakeClock) clock);
    }
    return new RealTimer(name);
  }

  public static Timer of(Clock clock, String name, boolean isDaemon) {
    if (clock instanceof FakeClock) {
      return new FakeTimer((FakeClock) clock);
    }
    return new RealTimer(name, isDaemon);
  }

  public abstract void schedule(java.util.TimerTask task, long delay);

  public abstract void schedule(java.util.TimerTask task, Date time);

  public abstract void schedule(
      java.util.TimerTask task,
      long delay,
      long period);

  public abstract void schedule(
      java.util.TimerTask task,
      Date firstTime,
      long period);

  public abstract void scheduleAtFixedRate(
      java.util.TimerTask task,
      long delay,
      long period);

  public abstract void scheduleAtFixedRate(
      java.util.TimerTask task,
      Date firstTime,
      long period);

  public abstract void cancel();

  public abstract int purge();

  static class RealTimer extends Timer {
    private final java.util.Timer timer;

    RealTimer() {
      timer = new java.util.Timer();
    }

    RealTimer(boolean isDaemon) {
      timer = new java.util.Timer(isDaemon);
    }

    RealTimer(String name) {
      timer = new java.util.Timer(name);
    }

    RealTimer(String name, boolean isDaemon) {
      timer = new java.util.Timer(name, isDaemon);
    }

    @Override
    public void schedule(java.util.TimerTask task, long delay) {
      timer.schedule(task, delay);
    }

    @Override
    public void schedule(java.util.TimerTask task, Date time) {
      timer.schedule(task, time);
    }

    @Override
    public void schedule(
        java.util.TimerTask task,
        long delay,
        long period) {
      timer.schedule(task, delay, period);
    }

    @Override
    public void schedule(
        java.util.TimerTask task,
        Date firstTime,
        long period) {
      timer.schedule(task, firstTime, period);
    }

    @Override
    public void scheduleAtFixedRate(
        java.util.TimerTask task,
        long delay,
        long period) {
      timer.scheduleAtFixedRate(task, delay, period);
    }

    @Override
    public void scheduleAtFixedRate(
        java.util.TimerTask task,
        Date firstTime,
        long period) {
      timer.scheduleAtFixedRate(task, firstTime, period);
    }

    @Override
    public void cancel() {
      timer.cancel();
    }

    @Override
    public int purge() {
      return timer.purge();
    }
  }

  static class FakeTimer extends Timer {
    private final FakeClock fakeClock;
    private final PriorityQueue<TimerTask> timerTasks = new PriorityQueue<>();

    FakeTimer(FakeClock fakeClock) {
      this.fakeClock = fakeClock;
      fakeClock.addFakeTimer(this);
    }

    @Override
    public void schedule(java.util.TimerTask task, long delay) {
      if (delay < 0) {
        throw new IllegalArgumentException("Negative delay.");
      }
      sched(
          task,
          fakeClock.instant().plus(Duration.ofMillis(delay)),
          0);
    }

    @Override
    public void schedule(java.util.TimerTask task, Date time) {
      sched(
          task,
          time.toInstant(),
          0);
    }

    @Override
    public void schedule(
        java.util.TimerTask task,
        long delay,
        long period) {
      if (delay < 0) {
        throw new IllegalArgumentException("Negative delay.");
      }
      if (period <= 0) {
        throw new IllegalArgumentException("Non-positive period.");
      }
      sched(
          task,
          fakeClock.instant().plus(Duration.ofMillis(delay)),
          period);
    };

    @Override
    public void schedule(
        java.util.TimerTask task,
        Date firstTime,
        long period) {
      if (period <= 0) {
        throw new IllegalArgumentException("Non-positive period.");
      }
      sched(
          task,
          firstTime.toInstant(),
          period);
    }

    @Override
    public void scheduleAtFixedRate(
        java.util.TimerTask task,
        long delay,
        long period) {
      schedule(task, delay, period);
    }

    @Override
    public void scheduleAtFixedRate(
        java.util.TimerTask task,
        Date firstTime,
        long period) {
      schedule(task, firstTime, period);
    }

    private synchronized void sched(
        java.util.TimerTask task,
        Instant time,
        long period) {
      TimerTask timerTask = new TimerTask(time, Duration.ofMillis(period)) {
        @Override
        public void run() {
          task.run();
        }
      };
      timerTask.state = TimerTask.SCHEDULED;
      timerTasks.add(timerTask);
    }

    @Override
    public synchronized void cancel() {
      timerTasks.clear();
    }

    @Override
    public synchronized int purge() {
      int sizeBeforePurged = timerTasks.size();
      timerTasks.removeIf(t -> t.state == TimerTask.CANCELLED);
      return sizeBeforePurged - timerTasks.size();
    }

    synchronized void checkExpiration(Instant now) {
      while (timerTasks.size() > 0) {
        final TimerTask timerTask = timerTasks.element();
        if (now.isBefore(timerTask.nextExecutionTime)) {
          break;
        }
        timerTasks.remove();
        if (timerTask.state == TimerTask.CANCELLED) {
          continue;
        }
        timerTask.run();
        if (timerTask.period.equals(Duration.ZERO)) {
          timerTask.state = TimerTask.EXECUTED;
        } else {
          timerTask.nextExecutionTime = timerTask.nextExecutionTime.plus(
              timerTask.period);
          timerTasks.add(timerTask);
        }
      }
    }
  }
}
