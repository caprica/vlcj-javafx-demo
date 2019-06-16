package uk.co.caprica.vlcj.javafx.test;

/**
 * A stupid wrapper class to avoid the Java Module System because the Java Module System is a PITA.
 */
public class JavaFxLauncher {

    public static void main(String[] args) {
        // The implementations differ only in which type of timer solution is used to render the video
//        AnimationTimerJavaFXDirectRenderingTest.main(args);
//        NanoTimerJavaFXDirectRenderingTest.main(args);
        TimelineJavaFXDirectRenderingTest.main(args);
    }

}
