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
It won't be versioned by git and deleted after restart.


Deploy
~~~~~~

To deploy the version onto the AppEngine as a dev version, use deploy_dev.sh:

  $ ./src/main/scripts/deploy_dev.sh $VERSION_ID

where $VERSION_ID is a new version id (not existing in the AppEngine yet) to be
used to identify the development version.

It might open new tab in your browser with page containing generated code.
You have to paste this code into console in order to proceed with deploy.

Once you have tested that the dev version works as expected, you can switch
the default version to be the dev version on AppEngine admin interface.

To test & switch to default version, use the admin interface at:
  Main -> version.
Make sure you switch both the "default" version and the "quizz-tasks" version.
