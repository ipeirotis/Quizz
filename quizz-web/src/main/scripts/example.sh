#!/bin/bash

# Please list below the appspot URLs for your app
# even if you have created an alias
WEB_URL="crowd-power.appspot.com/" # without trailing /
API_URL="https://crowd-power.appspot.com/_ah/api/quizz/v1" # without trailing /
QUIZ_ID="NYTimesPopQuiz"

echo "Creating the quiz"
curl $API_URL/insertQuiz --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "name": "Steven Pinker’s Mind Games", "kind": "MULTIPLE_CHOICE", "numChoices": "4"}'

#TODO: Check that the quiz was properly created
# That is is in the list of returned quizzes
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/listQuiz
# and that we can get back the quiz
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/getQuiz?id=testQuizId


curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "According to Leon Festinger’s theory of cognitive dissonance, why do the office workers in the comic strip think they have learned something? <br> <img src=\"http://i1.nyt.com/images/2014/04/13/education/QUIZ-2/QUIZ-2-articleLarge-v2.jpg\">", "feedback": "Cognitive dissonance theory holds that when people hold contradictory beliefs (“I’m a rational, autonomous person” yet “I just did something pointless”), they experience an unpleasant state, cognitive dissonance, which they mitigate by bringing one of the beliefs into consistency with the other.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "People who have been manipulated into a pointless task will rationalize their behavior and conclude that it must have been worthwhile.", "kind": "GOLD" }, { "text": "The team-building exercise forces them to cooperate, and they realize they cannot accomplish a task if they bicker and fight.", "kind": "INCORRECT" }, { "text": "Teams of people learn more after they have set aside their differences and shown signs of solidarity (hugging) and shared emotion (crying).", "kind": "INCORRECT" }, { "text": "People who succeed with one difficult task will be more confident in taking on new ones.", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "Did you hear the joke about the statistician who drowned while wading across a river whose average depth was three feet? What did the statistician forget to take into account?", "feedback": "Standard deviation is a measure of variability around a mean, in this case variations in the depth of a river.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "The standard deviation", "kind": "GOLD" }, { "text": "The correlation coefficient", "kind": "INCORRECT" }, { "text": "The median", "kind": "INCORRECT" }, { "text": "Inferential statistics", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "Imagine a movie called “Lamb to the Slaughter,” in which the heroine bludgeons the victim with a frozen leg of lamb. She then cooks the lamb and feeds it to the police, who are searching her house for a baseball bat, which they think is the murder weapon. The fact that the police never considered the leg of lamb as a weapon is a perfect example of:", "feedback": "Functional fixedness is the cognitive blind spot in which people think about an object in terms of its function and forget about its physical properties. In the textbook example, subjects are given a candle, matchbook and box of tacks and are asked to affix the candle to the wall. Few reach the best solution, which is to empty the box, tack it to the wall and place the candle on it, because they think of the box as a container rather than a shelf.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Functional fixedness", "kind": "GOLD" }, { "text": "The availability bias", "kind": "INCORRECT" }, { "text": "The effect of language on reasoning", "kind": "INCORRECT" }, { "text": "The representativeness heuristic", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "A husband and wife are considering divorce. Under what circumstances would they be said to be in a “prisoner’s dilemma”?", "feedback": "In the prisoner’s dilemma, a scenario from game theory that illustrates a paradox of cooperation, two people acting selfishly end up worse off than if they had cooperated, but neither dares cooperate out of fear the other will act selfishly.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Neither spouse wants to hire an expensive lawyer, but each ends up doing so out of fear that the other spouse will do so and leave them at a disadvantage.", "kind": "GOLD" }, { "text": "Each spouse wants to hire an inexpensive lawyer, but both hire expensive lawyers because they realize the outcome will be better for both of them.", "kind": "INCORRECT" }, { "text": "The lawyers’ fees are so expensive that they forgo divorce and remain trapped in the unhappy marriage.", "kind": "INCORRECT" }, { "text": "When one spouse hires an expensive lawyer, less money is available to the other for a lawyer, so whoever hires the more expensive lawyer first has an advantage.", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "Pete Townshend, lead guitarist of The Who, has publicized his ailment to warn fans of the negative long-term effects of too much:", "feedback": "The course textbook, Peter Gray’s “Psychology,” notes that Pete Townshend, who suffers severe hearing loss, has campaigned to warn young people of the danger posed by prolonged exposure to loud music. Sensation and perception — including the workings of the sense organs that convert environmental energy into neural impulses — have been topics in psychology for as long as the discipline has existed.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Rock ’n’ roll", "kind": "GOLD" }, { "text": "Sex", "kind": "INCORRECT" }, { "text": "Drugs", "kind": "INCORRECT" }, { "text": "Alcohol", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "Consider this cartoon. How would the man in the chair and a contemporary psychologist, respectively, explain the dog’s problem?<br><img src=\"http://i1.nyt.com/images/2014/04/13/education/QUIZ-1/QUIZ-1-master315.jpg\">", "feedback": "The man in the chair is a psychoanalyst, so he would apply Freud’s theory that some people never outgrow a childhood stage in which their desires are automatically gratified by breast-feeding. Contemporary psychologists would recognize the dog’s allusion to Pavlov’s classic experiment in which dogs learned to salivate to a bell paired with food.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Oral fixation; classical conditioning", "kind": "GOLD" }, { "text": "Primary process thinking; operant conditioning", "kind": "INCORRECT" }, { "text": "Transference; stimulus discrimination", "kind": "INCORRECT" }, { "text": "Defense mechanisms of the ego; stimulus generalization", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "<blockquote>Higgledy-piggledy,<br>Hamlet of Elsinore<br>Ruffled the critics<br>by dropping this bomb:<br>“Phooey on Freud<br>And his psychoanalysis.<br>Oedipus, schmoedipus,<br>I just loved Mom.”</blockquote> In the double dactyl above, Hamlet crucially disagrees with Freud by denying that his feelings for his mother were:", "feedback": "Freud’s Oedipal complex, in which a man struggles to resolve a sexual desire for his mother and rivalry with his father, has been applied to Hamlet’s reluctance to slay his stepfather, with whom he supposedly identified unconsciously.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Sexual", "kind": "GOLD" }, { "text": "Conditioned", "kind": "INCORRECT" }, { "text": "Confined to the “phallic stage” of psychosexual development", "kind": "INCORRECT" }, { "text": "Unresolved", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "Robert Trivers’s argument that evolution can select for self-deception is similar to which aphorism?", "feedback": "Robert Trivers argued in “The Folly of Fools: The Logic of Deceit and Self-Deception in Human Life,” published in 2011, that evolution does not always select for honesty. We try to win allies by exaggerating our competence and generosity, but then they try to see through these exaggerations by noticing signs of nervousness or other tells. If we’re wired to believe our own exaggerations, we can’t betray ourselves.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "The best liar is the one who believes his own lies.", "kind": "GOLD" }, { "text": "Knowledge is power.", "kind": "INCORRECT" }, { "text": "Honesty is the best policy.", "kind": "INCORRECT" }, { "text": "A liar must have a good memory.", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "If Jimi Hendrix’s song “Manic Depression” had been written by a clinical psychologist, what would its title be?", "feedback": "Many vernacular terms for psychological disorders that have negative or misleading connotations have been replaced with neutral alternatives. Manic depression is now called bipolar disorder, the two “poles” pertaining to the opposite moods the patient swings between.", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "“Bipolar Disorder”", "kind": "GOLD" }, { "text": "“Nervous Breakdown”", "kind": "INCORRECT" }, { "text": "“Anhedonia”", "kind": "INCORRECT" }, { "text": "“Borderline Personality Disorder”", "kind": "INCORRECT" }] }'

curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data '{ "quizID": "'$QUIZ_ID'", "text": "What would an evolutionary biologist say about Calvin’s use of the word “altruist”? <img src=\"http://i1.nyt.com/images/2014/04/13/education/413/413-articleLarge-v2.jpg\">", "feedback": "Biologists define “altruism” as behavior that benefits another organism at a cost to the self (sharing food, offering protection or, in this case, taking a message). Unlike its everyday definition as an unselfish regard for others, biological altruism pertains only to behavior and its effects, not to the actor’s motives; if a sacrifice benefits the organism or its genes in the long run, a biologist will still call it “altruistic.”", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "He is using it correctly, because he is being asked to confer a benefit to someone else at a cost to himself.", "kind": "GOLD" }, { "text": "He is using it incorrectly, because he shares half his genes with his father.", "kind": "INCORRECT" }, { "text": "He is using it incorrectly, because the cost to himself is small and the benefit to his father is large.", "kind": "INCORRECT" }, { "text": "He is using it correctly, because his motives are selfish and he is withholding a favor accordingly.", "kind": "INCORRECT" }] }'




for i in {1..10}
do
   echo "Adding calibration question #$i"
   curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "text": "Calibration Question '$i'", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1 - gold", "kind": "GOLD" }, { "text": "Answer 2", "kind": "INCORRECT" }, { "text": "Answer 3", "kind": "INCORRECT" }, { "text": "Answer 4", "kind": "INCORRECT" }] }'
done

for i in {1..10}
do
   echo "Adding collection question #$i"
   curl $API_URL/insertQuestion --header 'Content-Type: application/json' --data  '{ "quizID": "'$QUIZ_ID'", "text": "Collection Question '$i'", "kind": "MULTIPLE_CHOICE", "weight": 1, "answers": [{ "text": "Answer 1 - silver", "kind": "SILVER", "probability": 0.75 }, { "text": "Answer 2", "kind": "INCORRECT" }, { "text": "Answer 3", "kind": "INCORRECT" }, { "text": "Answer 4", "kind": "INCORRECT" }] }'
done

echo "Updating the statistics for the Quiz"
curl $API_URL/updateQuizCounts?quizID=testQuizId
echo "Done..."

#TODO: Check that the updated numbers are ok
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/getQuiz?id=testQuizId

# Check that we get back calibration and collection questions
# GET https://crowd-power.appspot.com/_ah/api/quizz/v1/quizquestions/testQuizId

# Caching the survival probabilities to make the values available 
# We use the background tasks module, just in case it needs more time
# but this seems fast enough and often finishes within 20 secs or so.
echo "Computing survival probabilities..."
curl quizz-tasks.$WEB_URL/api/cacheSurvivalProbability?now=true
echo "Done..."

# Caching the explore-exploit values for all combinations of values
# from a=0..10, b=0..10, c=0..5 (a=correct, b=incorrect, c=exploit)
# and for multiple choice quizzes with N=4 multiple choice options.
# This call generates a large number of individual calls (a*b*c calls)
# that are placed in an execution queue
# The results are then being used by the ProcessAnswer endpoint
# to return whether the next action is explore of exploit
echo "Computing exploration/exploitation values..."
curl quizz-tasks.$WEB_URL/api/cacheExploreExploit?'a=10&b=10&c=5&N=4'
echo "Done..."

echo "We are now ready to serve the quiz..."

read -p "Press [Enter] key to remove the quiz..."
echo "Removing the test quiz"
curl -i -H "Accept: application/json" -X DELETE $API_URL/removeQuiz?id=testQuizId



