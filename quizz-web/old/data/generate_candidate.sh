#!/bin/sh

echo '=========================================='
echo 'Relation/Collection Filter   : "'$1'"'
echo 'Predicates                   : "'$2'"'
echo 'Quiz name                    : "'$3'"'
echo 'Max questions                : "'$4'"'
echo 'Dremel script                : "'$5'"'
echo 'Output dir                   : "'$6'"'
echo '=========================================='

sed "s|xxxxxxxxxx|$1|;s|yyyyyyyyyy|$2|;s|question_count|$4|" < $5 | dremel --output=csv > $6/$3-qa_pair.csv
