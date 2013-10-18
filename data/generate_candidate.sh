#!/bin/sh

# PARAM1: The KP relation
# PARAM2: The KV predicate
# PARAM3: Friendly quiz name
# PARAM4: Question count
echo '=========================================='
echo 'KP Relation   : "'$1'"'
echo 'KV Predicate  : "'$2'"'
echo 'Quiz name     : "'$3'"'
echo 'Max questions : "'$4'"'
echo '=========================================='

sed "s|xxxxxxxxxx|$1|;s|yyyyyyyyyy|$2|;s|question_count|$4|" < dremel.qa_pair | dremel --output=csv > kg_questions/$3-qa_pair.csv
