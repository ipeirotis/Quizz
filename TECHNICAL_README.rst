
Overview
========

The system is built using Google App Engine. It has a Java-based backend, and
a Javascript-based frontend. 

The frontend communicates with the backend using, mainly, REST-based API calls
that are created using Google Endpoints. (You will see that there are still calls
that are pointing to servlets; our goal is to replace all, or most, of the 
servlets and use only API calls created through Google Endpoints. This will 
allow for better separation of the backend and the frontend, and will lead to 
more consistent code.)


Uploading Data
~~~~~~~~~~~~~~

We create quizzes using the API. (While we used to have a web page for uploading
data, we have since depracated it.)

The scripts/curl_example.sh file has a simple example on how to create a quiz.

  $ curl "$WEB_URL/addQuiz" --data "quizID=$QUIZ_ID&name=Some Quiz name for id $QUIZ_ID&text=This will be ignored and removed in future"

