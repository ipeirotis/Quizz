unit tests:

1.Windows only: install Git Bash, http://git-scm.com/downloads

2.Install Karma, http://karma-runner.github.io/

3.Install NodeJS, http://nodejs.org/

We use maven-karma-plugin that provides the ability to run tests via Karma as a part of maven build.

e2e tests:

1.Install Protractor, https://github.com/angular/protractor

2.Install Selenium
$ ./node_modules/protractor/bin/webdriver-manager update

3.Run script /quizz-web/src/main/scripts/example.sh

4.Run e2e tests
$ mvn appengine:devserver
$ protractor {project dir}/src/main/webapp/test/config/protractor.conf.js
