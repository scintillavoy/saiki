# Saiki

A fake clock implementation in Java.

# Usage

Inject `Clock` from this library to your class (or method) and use it to get the
current time.

```java
import io.github.scintillavoy.saiki.Clock;

public class SomeClass {
  private final Clock clock;

  SomeClass(Clock clock) {
    this.clock = clock;
  }

  public void someMethod() {
    Instant now = clock.instant();
    doSomething();
  }
}
```

In production code, use a clock you want.

```java
import io.github.scintillavoy.saiki.Clock;

// This clock is a wrapper of java.time.Clock and works as the same.
Clock clock = Clock.systemUTC();
SomeClass instance = new SomeClass(clock);
```

During a test, use a `FakeClock` and advance it instead of calling
`Thread.sleep`.

```java
import io.github.scintillavoy.saiki.FakeClock;

// This clock is a fake clock which can be advanced manually.
FakeClock fakeClock = new FakeClock(ZoneId.of("UTC"));
SomeClass instance = new SomeClass(fakeClock);

// Assert before the time goes by.
assertState();

// Call advance instead of Thread.sleep.
fakeClock.advance(Duration.ofSeconds(4));

// Assert after the time has passed.
assertState();
```

If you want to use `java.util.Timer` with a `FakeClock`, use `Timer.of` method
in this library to instantiate a timer.

```java
import io.github.scintillavoy.saiki.Clock;
import io.github.scintillavoy.saiki.Timer;
import java.util.TimerTask;

Clock clock = Clock.systemUTC();

// This timer is a wrapper of java.util.Timer and works as the same.
Timer timer = Timer.of(clock);
timer.schedule(new TimerTask() {
  @Override
  public void run() {
    doSomething();
  }
}, 4000);
```

And use it with a `FakeClock` for testing.

```java
import io.github.scintillavoy.saiki.FakeClock;
import io.github.scintillavoy.saiki.Timer;

FakeClock fakeClock = new FakeClock(ZoneId.of("UTC"));

// This timer is a fake timer and works with a FakeClock.
Timer timer = Timer.of(fakeClock);
timer.schedule(new TimerTask() {
  @Override
  public void run() {
    doSomething();
  }
}, 4000);

// The task is not executed yet.
assertState();

// Call advance instead of Thread.sleep.
fakeClock.advance(Duration.ofSeconds(4));

// The task should be executed now.
assertState();
```
