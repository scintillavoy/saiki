package io.github.scintillavoy.saiki;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.scintillavoy.saiki.Clock.FakeClock;

class ClockTest {
  @Nested
  class FakeClockTest {
    @Test
    void getZoneReturnsZone() {
      Instant now = Instant.now();
      FakeClock fakeClock = new FakeClock(now, ZoneId.of("UTC"));
      assertEquals(
          ZoneId.of("UTC"),
          fakeClock.getZone(),
          "getZone should return the zone of the clock.");

      fakeClock = new FakeClock(now, ZoneId.of("Asia/Seoul"));
      assertEquals(
          ZoneId.of("Asia/Seoul"),
          fakeClock.getZone(),
          "getZone should return the zone of the clock.");
    }

    @Test
    void withZoneReturnsCopyOfFakeClockWithZone() {
      Instant now = Instant.now();
      FakeClock fakeClock = new FakeClock(now, ZoneId.of("UTC"));
      FakeClock fakeClock2 = (FakeClock) fakeClock.withZone(
          ZoneId.of("Asia/Seoul"));
      assertNotSame(
          fakeClock,
          fakeClock2,
          "withZone should return the copy of the original clock.");
      assertEquals(
          ZoneId.of("UTC"),
          fakeClock.getZone(),
          "Zone of the original clock should remain the same.");
      assertEquals(
          ZoneId.of("Asia/Seoul"),
          fakeClock2.getZone(),
          "Zone of the copied clock should be the specified one when copied.");
    }

    @Test
    void instantReturnsFixedInstantUnlessAdvanced() {
      Instant now = Instant.now();
      FakeClock fakeClock = new FakeClock(now, ZoneId.of("UTC"));
      assertEquals(
          now,
          fakeClock.instant(),
          "instant should return the same Instant unless the clock is advanced.");
    }

    @Test
    void advanceAdvancesFakeClockBySpecificAmountOfDuration() {
      Instant now = Instant.now();
      FakeClock fakeClock = new FakeClock(now, ZoneId.of("UTC"));
      fakeClock.advance(Duration.ofMillis(12));
      assertEquals(
          now.plusMillis(12),
          fakeClock.instant(),
          "instant should return the advanced Instant after the clock is advanced.");

      fakeClock.advance(Duration.ofMillis(3));
      fakeClock.advance(Duration.ofMillis(7));
      assertEquals(
          now.plusMillis(22),
          fakeClock.instant(),
          "instant should return the advanced Instant after the clock is advanced.");
    }

    @Test
    void fakeClockCanBeUsedForNowMethodInJavaTimeLibrary() {
      Instant now = Instant.now();
      ZoneId zoneId = ZoneId.of("UTC");
      FakeClock fakeClock = new FakeClock(now, zoneId);
      Instant instantFromClock = fakeClock.instant();
      assertEquals(
          instantFromClock,
          Instant.now(fakeClock),
          "Instant::now should return the same Instant as the clock");
      assertEquals(
          LocalDate.ofInstant(instantFromClock, zoneId),
          LocalDate.now(fakeClock),
          "LocalDate::now should return the same LocalDate as the clock");
      assertEquals(
          LocalDateTime.ofInstant(instantFromClock, zoneId),
          LocalDateTime.now(fakeClock),
          "LocalDateTime::now should return the same LocalDateTime as the clock");
      assertEquals(
          LocalTime.ofInstant(instantFromClock, zoneId),
          LocalTime.now(fakeClock),
          "LocalTime::now should return the same LocalTime as the clock");
      assertEquals(
          OffsetDateTime.ofInstant(instantFromClock, zoneId),
          OffsetDateTime.now(fakeClock),
          "OffsetDateTime::now should return the same OffsetDateTime as the clock");
      assertEquals(
          OffsetTime.ofInstant(instantFromClock, zoneId),
          OffsetTime.now(fakeClock),
          "OffsetTime::now should return the same OffsetTime as the clock");
      assertEquals(
          Year.from(instantFromClock.atZone(zoneId).toLocalDate()),
          Year.now(fakeClock),
          "Year::now should return the same Year as the clock");
      assertEquals(
          YearMonth.from(instantFromClock.atZone(zoneId).toLocalDate()),
          YearMonth.now(fakeClock),
          "YearMonth::now should return the same YearMonth as the clock");
      assertEquals(
          ZonedDateTime.ofInstant(instantFromClock, zoneId),
          ZonedDateTime.now(fakeClock),
          "ZonedDateTime::now should return the same ZonedDateTime as the clock");
    }
  }
}
