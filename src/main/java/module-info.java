module uk.co.caprica.vlcj.javafx.demo {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires miglayout.javafx;
    requires uk.co.caprica.media.scanner;
    requires uk.co.caprica.vlcj;
    requires uk.co.caprica.vlcj.javafx;

    requires static lombok;

    exports uk.co.caprica.vlcj.javafx.demo to javafx.graphics;
}
