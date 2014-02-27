Quizz.us
========

For development we use Java 7, and and Maven 3.1.

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

  $ cd quizz-ear/
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

  $ cd quizz-ear/
  $ mvn appengine:update

It will open new tab in your browser with page containing generated code.
You have to paste this code into console in order to proceed with deploy.


