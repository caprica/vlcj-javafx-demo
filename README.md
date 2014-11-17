![vlcj](https://github.com/caprica/vlcj/raw/master/etc/vlcj-logo.png "vlcj")

vlcj-javafx-demo
================

Demo showing how vlcj can be used to render video to a JavaFX 2.x Canvas.

This uses the vlcj direct rendering media player component. It can not hope to perform as well as the native heavyweight rendering using an AWT Canvas, but nevertheless smooth full HD playback is possible.


Getting Started
---------------

At the moment this is rough and ready.

You will need to make a small edit to the pom.xml to specify the location of your java.home installation. This is needed to properly resolve the JavaFX dependency.

You will also need to edit the test class itself to specify the video file name.
 
Patches/pull-requests are welcome to improve this example.


GPU Support
-----------

GPU support even for modern video cards is pretty poor in JavaFX under Java7.

nVidia cards seem to be better supported at the moment.

If your video card is not supported then JavaFX will fall back to a software renderer. This will hobble your video playback performance.

Java8 seems much better - i.e. some modern mainstream AMD graphic cards seem better supported. Under Java8 this sample application is working fine with a Radeon HD 7700 series video card on Linux.
