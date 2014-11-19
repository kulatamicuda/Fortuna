package se.grunka.fortuna.entropy;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import se.grunka.fortuna.accumulator.EventAdder;
import se.grunka.fortuna.accumulator.EventScheduler;

import static org.junit.Assert.assertEquals;

public class URandomEntropySourceTest {

    private URandomEntropySource target;
    private int schedules, adds;

    @Before
    public void before() throws Exception {
        target = new URandomEntropySource();
        schedules = 0;
        adds = 0;
    }

    @Test
    public void shouldAddUptime() throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        // this test makes sense only on Unix systems
        if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS
				.indexOf("aix") > 0) {
            target.event(
                new EventScheduler() {
                    @Override
                    public void schedule(long delay, TimeUnit timeUnit) {
                        assertEquals(100, timeUnit.toMillis(delay));
                        schedules++;
                    }
                },
                new EventAdder() {
                    @Override
                    public void add(byte[] event) {
                        assertEquals(32, event.length);
                        adds++;
                    }
                }
            );
            assertEquals(1, schedules);
            assertEquals(1, adds);
        }
    }
}
