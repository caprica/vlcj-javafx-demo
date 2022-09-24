package uk.co.caprica.vlcj.javafx.test;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

/**
 * A stupid wrapper class to avoid the Java Module System because the Java Module System is a PITA.
 * <p>
 * This system property is sometimes useful:
 * <pre>
 *   -Dprism.verbose=true
 * </pre>
 */
public class JavaFxLauncher {

    private enum TimerType {
        ANIMATION_TIMER,
        NANO_TIMER,
        TIMELINE
    }

    static {
        // We initialise vlcj early - this is sometimes needed to prevent a JVM crash when opening dialogs on Linux
        // (some unexplained problem with vlcj integration)
        new MediaPlayerFactory().release();
    }

    /**
     * Specify the type of timer to use via the command-line.
     * <p>
     * By default the "Timeline" timer will be used, which empirically seems to perform slightly better than the others.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        TimerType timerType = TimerType.TIMELINE;
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "animation":
                    timerType = TimerType.ANIMATION_TIMER;
                    break;
                case "nano":
                    timerType = TimerType.NANO_TIMER;
                    break;
                case "timeline":
                default:
                    timerType = TimerType.TIMELINE;
                    break;
            }
        }

        System.err.printf("Using timer implementation: %s%n", timerType);

        // The implementations differ only in which type of timer solution is used to render the video
        switch (timerType) {
            case ANIMATION_TIMER:
                AnimationTimerJavaFXDirectRenderingTest.main(args);
                break;
            case NANO_TIMER:
                NanoTimerJavaFXDirectRenderingTest.main(args);
                break;
            case TIMELINE:
                TimelineJavaFXDirectRenderingTest.main(args);
                break;
        }
    }

}
