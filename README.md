![vlcj](https://github.com/caprica/vlcj/raw/master/etc/vlcj-logo.png "vlcj")

vlcj-javafx-demo
================

IMPORTANT!

This branch of the vlcj-javafx demo project uses the new experimental PixelBuffer to avoid a full-frame
buffer copy.

You may instead switch to the [master](https://github.com/caprica/vlcj-javafx) for the more stable
version.

It is a bit rough and ready but is confirmed working on 64-bit Linux.

See:
 * https://github.com/caprica/vlcj/issues/883
 * https://mail.openjdk.java.net/pipermail/openjfx-dev/2019-June/023347.html

This project will not work immediately when you clone it, you will need to do some additional things:
 * You will need to clone and manually build the latest snapshot of vlcj from its master (4.2.0-SNAPSHOT)
   and use this in your project or launcher rather than a vlcj release version
 * You will need to go to that mailing list link and download the experimental JavaFX package from there.
 * You will then need to manually add the JavaFX jars and native libraries to your project or launcher.


The main class is `JavaFxLauncher` which is a simple wrapper around the main `Application` class for
uninteresting Java Module System avoidance reasons.

![demo](https://github.com/caprica/vlcj-javafx/blob/pixelbuffer-test/doc/vlcj-javafx-pixelbuffer.png?raw=true "demo")

Caveats
-------

The implementation may not be optimal, the code is a quickly hacked-up version of the original vlcj
JavaFX demo wtih minimal changes necessary to get it working with the new PixelBuffer.

Some optimisations will require changes to vlcj itself and may become part of a future vlcj release.

Comments and Feedback
---------------------

Please use the vlcj github issue for comments and feedback:

 * https://github.com/caprica/vlcj/issues/883
