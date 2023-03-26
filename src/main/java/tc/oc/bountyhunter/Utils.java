package tc.oc.bountyhunter;

import java.time.Duration;

public class Utils {

  private static long TICKS_PER_SECOND = 20;

  public static long durationTicks(Duration duration) {
    return duration.getSeconds() * TICKS_PER_SECOND;
  }
}
