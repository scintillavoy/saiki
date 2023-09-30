package io.github.scintillavoy.saiki;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.scintillavoy.saiki.Clock.FakeClock;
import io.github.scintillavoy.saiki.Timer.FakeTimer;
import io.github.scintillavoy.saiki.Timer.RealTimer;

class TimerTest {
  @Test
  void ofInstantiatesProperTimerForGivenClock() {
    Clock systemClock = Clock.systemUTC();
    Timer timerOfSystemClock = Timer.of(systemClock);
    assertInstanceOf(
        RealTimer.class,
        timerOfSystemClock,
        "Timer instantiated with SystemClock should be RealTimer.");

    Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
    Timer timerOfFixedClock = Timer.of(fixedClock);
    assertInstanceOf(
        RealTimer.class,
        timerOfFixedClock,
        "Timer instantiated with FixedClock should be RealTimer.");

    Clock offsetClock = Clock.offset(Clock.systemUTC(), Duration.ZERO);
    Timer timerOfOffsetClock = Timer.of(offsetClock);
    assertInstanceOf(
        RealTimer.class,
        timerOfOffsetClock,
        "Timer instantiated with OffsetClock should be RealTimer.");

    Clock tickClock = Clock.tick(Clock.systemUTC(), Duration.ZERO);
    Timer timerOfTickClock = Timer.of(tickClock);
    assertInstanceOf(
        RealTimer.class,
        timerOfTickClock,
        "Timer instantiated with TickClock should be RealTimer.");

    FakeClock fakeClock = new FakeClock(ZoneId.of("UTC"));
    Timer timerOfFakeClock = Timer.of(fakeClock);
    assertInstanceOf(
        FakeTimer.class,
        timerOfFakeClock,
        "Timer instantiated with FakeClock should be FakeTimer.");
  }

  @Nested
  class RealTimerTest {
    @Test
    void realTimerHasJavaUtilTimer() {
      RealTimer realTimer = new RealTimer();
      try {
        Field timerField = RealTimer.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        java.util.Timer timer = (java.util.Timer) timerField.get(realTimer);
        assertNotNull(
            timer,
            "RealTimer should have a timer field of type java.util.Timer");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Nested
  class FakeTimerTest {
    @Test
    void fakeTimerHasFakeClock() {
      FakeClock fakeClock = new FakeClock(ZoneId.of("UTC"));
      FakeTimer fakeTimer = new FakeTimer(fakeClock);
      try {
        Field fakeClockField = FakeTimer.class.getDeclaredField("fakeClock");
        fakeClockField.setAccessible(true);
        FakeClock fakeClockFromFakeTimer = (FakeClock) fakeClockField.get(fakeTimer);
        assertSame(
            fakeClock,
            fakeClockFromFakeTimer,
            "FakeTimer should have the same FakeClock passed as an argument.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
