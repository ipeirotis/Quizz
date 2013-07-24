# PARAM1: The KP relation
# PARAM2: The KV predicate
# PARAM3: Friendly quiz name
echo '=========================================='
echo 'KP Relation  : "'$1'"'
echo 'KV Predicate : "'$2'"'
echo 'Quiz name    : "'$3'"'
echo '=========================================='

echo "Extracting questions..."
sed "s#xxxxxxxxxx#$1#" < dremel.questions | dremel --output=csv > $3-questions.csv
echo `cat $3-questions.csv | wc -l` "questions extracted"

echo "Extracting gold answers...."
sed "s|xxxxxxxxxx|$1|" < dremel.gold     | dremel --output=csv > $3-gold.csv
echo `cat $3-gold.csv | wc -l` "gold answers extracted"


echo "Extracting silver answers...."
sed "s|xxxxxxxxxx|$1|;s|yyyyyyyyyy|$2|" < dremel.silver    | dremel --output=csv > $3-silver.csv
echo `cat $3-silver.csv | wc -l` "silver answers extracted"

# Upload file to quizz.us using curl
