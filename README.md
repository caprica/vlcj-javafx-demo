![vlcj](https://github.com/caprica/vlcj/raw/master/etc/vlcj-logo.png "vlcj")

vlcj-javafx-demo
================

Demo showing how vlcj can be used to render video in a JavaFX scene.

The new JavaFX `PixelBuffer` is used to avoid a full-frame buffer copy, this is the strongly recommended approach.

With the PixelBuffer the native video buffer is directly shared with JavaFX thereby avoiding full-frame copies for each
video frame.

Performance is *really good* with `PixelBuffer`. This solution is likely to outperform the Swing/Java2D implementation
and likely may be the best approach for a cross-platform media player (even on OSX, which has not supported the
optimal embedded solution for a long time now.)

Java/JavaFX Versions
--------------------

This example project now requires JDK 11 and at least JavaFX 13 (for the new PixelBuffer).

VLC/LibVLC Versions
-------------------

This example uses the new native thumbnailer that comes with LibVLC 4.0.0 - consequently VLC 4.0.0 is the minimum
requirement.

This version of VLC is still in development, you can either build it yourself or try a nightly build from the Videolan
web site.

vlcj Versions
-------------

The latest vlcj-5.0.0-SNAPSHOT version is required.

Additional IDE Setup
--------------------

This project uses Lombok to reduce boilerplate. You may need to install a Lombok plugin for your IDE to make sure the
Lombok annotation processors run each time you make a code change.

Linux Notes
-----------

On Linux, the repaints can be glitchy if your scene graph is "busy", e.g. if you add multiple video views or numerous
other nodes. This manifests itself as flickering painting on the later components (the ones added latest to the scene),
and/or the background fill of the Scene leaking through to some other component.

If this is a problem, it is recommended to pass `-Dprism.dirtyopts=false` as a system property when starting the JVM.

This will incur a performance penalty.

Similarly, if you add more components (especially something like a menu bar) and the repaint of the controls starts
lagging on a dynamic re-size, passing `-Dprism.forceUploadingPainter=true` may help.

See https://github.com/caprica/vlcj-javafx-demo/issues/31.

See http://werner.yellowcouch.org/log/javafx-8-command-line-options for a list of the various JavaFX system properties
available.

This is the command-line I generally use on Linux:

```
-Dprism.dirtyopts=false -Dprism.forceUploadingPainter=true -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC
```

Screenshot
----------

An example showing a 3x3 grid of concurrent media players:

![vlcj](https://github.com/caprica/vlcj-javafx-demo/raw/vlcj-5.x/doc/vlcj-javafx-multiview.jpg "vlcj JavaFX multiview concept")

Videos
------

_These links go to YouTube._

An example showing the basic configuration of this application with a single media player:

[![](http://img.youtube.com/vi/S6MFewgHdn8/0.jpg)](https://www.youtube.com/watch?v=S6MFewgHdn8 "vlcj JavaFX concept")

An example showing the configuration of this application with a 2x2 grid of concurrent media players:

[![](http://img.youtube.com/vi/DUG5qS6dYZE/0.jpg)](https://www.youtube.com/watch?v=DUG5qS6dYZE "vlcj JavaFX multiview concept")

An example showing picture-in-picture:

[![](http://img.youtube.com/vi/k4j3hcJxc6g/0.jpg)](https://www.youtube.com/watch?v=k4j3hcJxc6g "vlcj JavaFX picture-in-picture concept")
