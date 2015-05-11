# Introduction #

This page explains how to checkout Androjena source code, build it and deploy an Android-includable Androjena library.
Androjena has been developed using [Eclipse IDE](http://www.eclipse.org) 3.5 (Galileo), so in this tutorial I'll assume that it is the development environment of your choice.

# Source checkout #

  1. If you don't have it already, download and install the [Android SDK](http://d.android.com/sdk/index.html) following the instructions on the site.
  1. Open Eclipse. If you don't already have a workspace, or don't want to use it, create a new one in a location of your choice.
  1. If you don't have it already, install the [ADT Eclipse plugin](http://d.android.com/sdk/eclipse-adt.html). Then, configure the plugin to point to the Android SDK installation folder.
  1. Your Android SDK **MUST** be equipped with the **SDK Platform Android 1.5, API 3, [revision 4](https://code.google.com/p/androjena/source/detail?r=4)**. If it's not, open the **Android SDK and AVD Manager**, download and install that package.
  1. If you don't have one already, install a [Subversion](http://subversion.tigris.org) client Eclipse plugin. I use [Subclipse](http://subclipse.tigris.org), and I'll just give instructions for checking out the source with that plugin, but it's just the same process with any other plugin.
  1. Now that your Eclipse installation is Subversion-enabled, click **File**->**Import**. In the **SVN** folder, choose **Checkout projects from SVN**, and click **Next**.
  1. Select **Create a new repository location** and click **Next**.
  1. Enter the url of Androjena SVN repository (you can find it under the **Source** tab on this site). As of this writing, the url is https://androjena.googlecode.com/svn/trunk/. Click **Next**.
  1. Select all the projects under the trunk folder and click **Finish**.
  1. The checkout process will take a looong while. When it's finished, you will find the **androjena** project and all the other projects it depends on in your workspace. Eclipse will mark lots of errors, but it's ok, they'll be sorted out during the build step.


# Androjena projects #

By now, Androjena consists of four different projects:
  * **androjena**: this is the "main" project. It contains all the original Jena 2.6.2 source code, patched to run on Android.
  * **androjena.test**: this project contains all the source code and test resources from the original Jena 2.6.2 test suite. This code too has been patched to comply with Android's "stripped-down" classpath and to solve other issues.
  * **androjena.test.android**: this is an Android application project, which contains an Activity to run the tests in **android.test** and, optionally, send the results to the Androjena development team. The ADT plugin provides some nice testing facilities, but I didn't manage to run Jena tests with them. After many tries, I think the reason is that the AndroidTestRunner cannot run test cases with an overridden runTest method (and Jena tests are mostly of that kind). Maybe in future releases of the SDK there will be no need for this application, but by now it comes pretty handy. It consists of a single Activity with a "start tests" button, and a panel where test results and statistics are shown in real time. Tests can be stopped at any time, and detailed test results are logged, so they can be easily captured with adb logcat or Eclipse's LogCat view. Test results are also sent to our server for benchmarking, bug finding and testing support on different devices. There are also two checkboxes: one for disabling all logs except for test failures and errors, and the other for enabling/disabling result sending.

The projects have been listed in "dependency order": every project depends on the previous ones.
To build and deploy Androjena, only the first two projects are needed. The next paragraph explains how to do that. If you want to build the test suite and the testrunner application too, all the projects are needed, see below for details.

# Building  and deploying Androjena #

So, let's start where the checkout ended.

Eclipse will probably mark a lot of errors, most of them due to something like "missing 1.5 code compliance". I don't know why this happens, but it can be solved quite easily: just click **Project**->**Clean**, choose **Clean all projects** and then click **Ok**. When the build finishes, only **androjena.test.android** should contain errors. See paragraph **Building and running Androjena test suite** for details on how to solve them.

This is pretty much all you need to build Androjena classes. Anyway, in order to use Androjena inside an Android project, you need to deploy it, i.e. "package" Androjena's classes and resource files inside a jar archive, which can be referenced and included inside Android projects.
Androjena deployment is accomplished via an Ant build file (build.xml) included inside the **androjena** project. To run it, just right-click it inside the **Package Explorer** and choose **Run As**->**Ant Build**. This build creates the **dist** folder inside the project, containing two files:
  * **androjena`_`_version_.jar**: this jar archive contains all Androjena classes and resource files, but doesn't contain classes and resources from other libraries Androjena depends on, such as icu4j and iri.
  * **androjena`_`_version_.zip**: this is the same release archive you will find under the **Downloads** section of this site. Inside it there's a **lib** folder containing **androjena`_`_version_.jar** and jars for all the libraries Androjena depends on, a **licenses** folder containing license copies for all the software components used inside Androjena (including Androjena itself, Jena, Apache Xerces, ICU4J etc), and a **README** file containing usage and license info.
That's it: to use Androjena inside a project, place all the jars inside the **lib** folder of **androjena`_`_version_.zip** somewhere, and then include them in your Android project. To do that in Eclipse, right-click your project in the **Package Explorer**, choose **Build Path**->**Configure Build Path**, and, under the **Libraries** tab, add all the jar archives. This way you will be able to reference Androjena's classes inside the project, and all Androjena code and resources will be included in the project's APK archive, and installed on the target device.


# Building and running Androjena test suite #

The last (optional) step is building and running Androjena tests. The **androjena.test.android** project should contain various errors: this is because its build path is configured to reference some jars inside the **lib** folder, which have not been created yet. Those jars are packaged versions of **androjena** and **androjena.test** code and resources, to be included inside **androjena.test.android** final APK package. In order to create/update that libraries, you should run the Ant build file (build.xml) contained in the **androjena.test.android** project, following these steps:
  1. Open the **Extenal tools** context menu and choose **External tools configurations**.
  1. Double click **Ant build** to create a new configuration, and name it **androjena.test.android**.
  1. Click **Browse Workspace** under **Buildfile**, choose the **androjena.test.android** folder, and the **build.xml** file inside it. Click **Ok**.
  1. Under the **Build** tab, uncheck **Build before launch**, then click **Run**.
  1. When the build is done, click **Project**->**Build Project**, and when it's finished all errors should be gone.

The Ant buildfile under androjena.test.android does the following: it calls the buildfiles under the other projects in order to package androjena and androjena.test in jar archives under the respective **dist** folders, then copies those jars (and other needed jars) inside androjena.test.android **lib** folder. You may be asking yourself why not to reference the projects directly so that their code is resolved and included in the final package. This can't be done for two reasons: Android doesn't include referenced project's code inside the application package, and, most importantly, it doesn't include non-java resources which may be needed by the other libraries. The only way to do this, is to reference the jar files in the buildpath (and androjena.test.android is already configured to do that): this way, all code and resources are packaged inside the final apk package, and can be referenced inside the android application.

Once you've done all this, the androjena.test.android application package (bin/androjena.test.android.apk) should be ready to be installed. This can be done manually with adb, or just selecting the androjena.test.android project in the **Package Explorer**, and clicking **Run As**, **Android Application**. Depending on your ADT configuration, you might be asked for a device to run the application on, and you can run it in an AVD or in a real device.

For a more detailed description of Androjena's test suite support on the emulator and real devices, please refer to AndrojenaTestingStatus.