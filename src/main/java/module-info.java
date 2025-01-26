module vlcj.javafx.demo {
    requires com.miglayout.javafx;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires uk.co.caprica.media.scanner;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;

    requires static lombok;

    exports uk.co.caprica.vlcj.javafx.demo to javafx.graphics;
}
