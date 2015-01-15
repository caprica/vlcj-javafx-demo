![vlcj](https://github.com/caprica/vlcj/raw/master/etc/vlcj-logo.png "vlcj")

vlcj-javafx-demo
================

Demo showing how vlcj can be used to render video to a JavaFX 2.x Canvas.

This uses the vlcj direct rendering media player component. It can not hope to perform as well as the native heavyweight rendering using an AWT Canvas, but nevertheless smooth full HD playback is possible.

There are three tests showing how to play video in JavaFX, each extends a common test-case with a specific timer implementation:

 - JavaFX Timeline;
 - Bespoke "NanoTimer" using a JavaFX ScheduledService;
 - JavaFX AnimationTimer 

All examples work acceptably.

The AnimationTimer is the simplest to implement.

Getting Started
---------------

At the moment this is rough and ready.

You will need to edit the test class itself to specify the video file name.
 
Patches/pull-requests are welcome to improve this example.


GPU Support
-----------

GPU support even for modern video cards is pretty poor in JavaFX under Java7.

nVidia cards seem to be better supported at the moment.

If your video card is not supported then JavaFX will fall back to a software renderer. This will hobble your video playback performance.

Java8 seems much better - i.e. some modern mainstream AMD graphic cards seem better supported. Under Java8 this sample application is working fine with a Radeon HD 7700 series video card on Linux.

Java/JavaFX Versions
--------------------

This example project now requires JDK 1.8.

Note that it is still possible in your own projects to use the same approach as this project on JDK 1.7 if you need to support that.

Notes
-----

Contemporary versions of JavaFX provide a SwingNode so that it is possible to embed Swing components inside a JavaFX scene - so why not use this for vlcj?

Well, vlcj still would require a heavyweight AWT Canvas, and heavyweight components do not work with the SwingNode component.

So whichever way you look at it, you're stuck with direct rendering, as per the test cases provided by this project.

What might be an option for you is to go the otherway, using Swing/AWT for your vlcj video window and embedding a JavaFX scene inside your Swing application.

Memory Profile
--------------

Using the standard JVM settings (default garbage collector):

![Standard JVM Settings Memory Profile](https://github.com/caprica/vlcj-javafx/raw/master/doc/memory-profile-default-options.png "Standard Options Memory Profile")

This test case plays a DVD ISO.

I can't really explain the behaviour of the garbage collector, it seems erratic and to change behaviour over time.

Nevertheless, there is clearly no memory leak, and it can run consistently in under 100Mb of heap memory.
