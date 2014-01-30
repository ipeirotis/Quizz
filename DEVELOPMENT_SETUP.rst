
Quizz.us
========

Whole service can be setup using Maven.
Recommended version is 3.1 (according to GAE documentation) and on this version it was tested.
Also Java 7 is recommended.

To check your Maven version run::

  $ mvn --version

It should also present your Java version.
If it didn't than you can run this command to check it::

  $ java -version


Local version
~~~~~~~~~~~~~

Cleaning content (removing generated content, etc.)::

  $ mvn clean install -U

Running local dev server::

  $ mvn appengine:devserver

Now you have your server running at `localhost:8888`.
It will use (create if needed) local data storage located in main folder and named `local_db.bin`.
It won't be versioned by git and delted after restart.

If you would like to make JS on website work correctly with your local instance you have to modify file `src/main/webapp/js/quizz.js`.
Change URL returned by methods `getBaseURL()` and `getWebURL()` to `http://localhost:8888/`.


If you are getting *Out of memory* then you should change option *instances* to much smaller value (5 should be fine) in `src/main/webapp/WEB-INF/backends.xml`.


Deploy
~~~~~~

In order to deploy version that is in current dir just type::

  $ mvn appengine:update
  $ mvn appengine:backends_update

It will open new tab in your browser with page containing generated code.
You have to paste this code into console in order to proceed with deploy.


IDEs
~~~~

Eclipse
-------

The newest Eclipse version had Maven 3.0 installed.
However, we need Maven 3.1.
Install new version in your system and then enable in Eclipse.
Last step can be done by going to *Window|Preferences* and in *Maven|Installations* adding new installation by giving proper path leading to Maven 3.1 installation.

Go to File|Import ... and from options select Maven|Existing Maven Projects.
In new view provide path to project (where `pom.xml` file is located), mark proper fields and click *Next*.
Ignore 3 errors that might be listed in next view and click *Finish*.

Now navigate through project files into `eclipse-launch-profiles` folder, R-Click on `DevAppServer.launch`, *Run as* and click *DevAppServer*.
If it didn't work than you need to open this file and change line containing *M2_RUNTIME* key into::

  <stringAttribute key="M2_RUNTIME" value="YOUR PATH TO MAVEN 3.1"/>

I think that it is highly recommended that you don't change those files but just make copy and use it instead of common one.
I will organize it better later.

  
