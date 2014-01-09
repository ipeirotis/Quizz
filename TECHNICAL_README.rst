
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

The scripts/curl_example.sh file has a simple example on how to create a quiz::

	$ curl http://www.quizz.us/addQuiz --data 'quizID=$QUIZ_ID&name=Some Quiz name for id $QUIZ_ID'

Each quiz contains questions. Each question can be of two types: 

a. A multiple choice question

b. A free-text entry question

Here is an example of creating a multiple choice entry::

	$ curl http://www.quizz.us/addQuestion --data '{
		"quizID": "'$QUIZ_ID'",
		"text": "Question text goes here",
		"weight": 1,
		"answers": [{ 
			"text": "Answer 1",
			"kind": "selectable_not_gold"
			}, {
			"text": "Answer 2 - it is gold one but do not tell anyone",
		"kind": "selectable_gold"
		}]}'

Here is an example of creating a free-text entry. You will notice that we can put arbitrary HTML code in the question, and that we also upload a (set of) answers that are correct.::

	$ curl http://www.quizz.us/addQuestion --header 'Content-Type: application/x-www-form-urlencoded; charset=utf-8'
		--data '{ 
			"quizID": $QUIZ_ID, 
			"text": "How would you say <a target=\"_blank\" href=\"http://freebase.com/m/07ylj‎\">Venezuela</a> in Greek?", 
			"weight": 1, 
			"answers": [{ 
				"text": "Βενεζουέλα", 
				"kind": "input_text", 
				"isGold": true 
				}, { 
				"text": "Μπολιβαριανή Δημοκρατία της Βενεζουέλας", 
				"kind": "input_text", 
				"isGold": true 
			}, 
		]}'
	      	
After uploading all the data, we also need to update the internal counters for 
Quizz (since we want to have a very responsive interface, we try to do a lot of
computations 'offline') so that we can serve the quiz. So we call::

    $ curl http://www.quizz.us/api/getQuizCounts?quizID=$QUIZ_ID&cache=no

 