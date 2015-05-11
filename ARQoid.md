# Introduction #

ARQoid is a porting of Jena's [ARQ](http://jena.sourceforge.net/ARQ) SPARQL query engine to the Android platform, for use with Androjena.

**ARQoid is still under development and has been tested only with very simple queries, so don't blame us if it doesn't work and use source code and binaries AT YOUR OWN RISK!**

That said, any issue report is greatly appreciated and will help us to speed up the development process.

Currently, ARQoid is based on source code from ARQ 2.8.3. We removed all code that used the javax.management api, which is not available in the Android's classpath.
All removed classes have been moved under the "removed" folder in the versioned project, maintaining original packages.
In the 0.2 version we restored the Apache Lucene dependency (see Lucenoid project), and reimported all LARQ-related code.
The remaining code have been patched to work without the removed classes and dependencies.

We have not imported ARQ tests yet, because they are based on JUnit 4, which conflicts with Android's builtin JUnit library. Maybe in the future we'll refactor the test suite, or find a way to use JUnit 4 inside Android.

You can check out and build ARQoid source code from SVN, or use the precompiled binaries available under the Downloads section.

# Building ARQoid #

First, follow the [instructions for checking out and building Androjena](BuildAndrojena.md). In the same way, check out the arqoid project from the SVN trunk inside the same workspace as Androjena. Once it's been compiled, run the build.xml ant task to generate the binary jar package under the dist folder.

# Usage #

Simply include the generated (or downloaded) binary package as long with Androjena and its dependencies between the libraries under your Android project classpath. All Androjena and ARQoid classes will be translated into Dalvik bytecode and bundled inside your project's apk package.