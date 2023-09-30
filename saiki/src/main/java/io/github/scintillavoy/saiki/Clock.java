package io.github.scintillavoy.saiki;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import io.github.scintillavoy.saiki.Timer.FakeTimer;

public abstract class Clock extends java.time.Clock {
  public static java.time.Clock systemUTC() {
    return new RealClock(java.time.Clock.systemUTC());
  }

  public static Clock systemDefaultZone() {
    return new RealClock(java.time.Clock.systemDefaultZone());
  }

  public static Clock system(ZoneId zone) {
    return new RealClock(java.time.Clock.system(zone));
  }

  public static Clock tickMillis(ZoneId zone) {
    return new RealClock(java.time.Clock.tickMillis(zone));
  }

  public static Clock tickSeconds(ZoneId zone) {
    return new RealClock(java.time.Clock.tickSeconds(zone));
  }

  public static Clock tickMinutes(ZoneId zone) {
    return new RealClock(java.time.Clock.tickMinutes(zone));
  }

  public static Clock tick(java.time.Clock baseClock, Duration tickDuration) {
    return new RealClock(java.time.Clock.tick(baseClock, tickDuration));
  }

  public static Clock fixed(Instant fixedInstant, ZoneId zone) {
    return new RealClock(java.time.Clock.fixed(fixedInstant, zone));
  }

  public static Clock offset(java.time.Clock baseClock, Duration offsetDuration) {
    return new RealClock(java.time.Clock.offset(baseClock, offsetDuration));
  }

  static final class RealClock extends Clock implements Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final java.time.Clock clock;

    RealClock(java.time.Clock clock) {
      this.clock = clock;
    }

    @Override
    public ZoneId getZone() {
      return clock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return new RealClock(clock.withZone(zone));
    }

    @Override
    public Instant instant() {
      return clock.instant();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof RealClock other
          && clock.equals(other.clock);
    }

    @Override
    public int hashCode() {
      return clock.hashCode();
    }

    @Override
    public String toString() {
      return "RealClock[" + clock + "]";
    }
  }

  public static final class FakeClock extends Clock implements Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Instant instant;
    private final ZoneId zone;
    private final List<FakeTimer> fakeTimers = new ArrayList<>();

    public FakeClock(ZoneId zone) {
      this(Instant.now(), zone);
    }

    public FakeClock(Instant instant, ZoneId zone) {
      this.instant = instant;
      this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public synchronized Clock withZone(ZoneId zone) {
      if (zone.equals(this.zone)) {
        return this;
      }
      return new FakeClock(instant, zone);
    }

    @Override
    public synchronized Instant instant() {
      return instant;
    }

    @Override
    public synchronized boolean equals(Object obj) {
      return obj instanceof FakeClock other
          && instant.equals(other.instant)
          && zone.equals(other.zone);
    }

    @Override
    public synchronized int hashCode() {
      return instant.hashCode() ^ zone.hashCode();
    }

    @Override
    public synchronized String toString() {
      return "FakeClock[" + instant + "," + zone + "]";
    }

    synchronized void addFakeTimer(FakeTimer fakeTimer) {
      fakeTimers.add(fakeTimer);
    }

    public synchronized void advance(Duration duration) {
      instant = instant.plus(duration);
      fakeTimers.forEach(fakeTimer -> fakeTimer.checkExpiration(instant));
    }
  }
}
