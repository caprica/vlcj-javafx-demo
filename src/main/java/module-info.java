module uk.co.caprica.vlcj.javafx.demo {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires lombok;
    requires miglayout.javafx;
    requires uk.co.caprica.media.scanner;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;

    opens uk.co.caprica.vlcj.javafx.demo to javafx.graphics;
}
