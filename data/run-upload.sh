URL="http://www.quizz.us"

# PARAM1: The KP relation
# PARAM2: The KV predicate
# PARAM3: Friendly quiz name
echo '=========================================='
echo 'KP Relation  : "'$1'"'
echo 'KV Predicate : "'$2'"'
echo 'Quiz name    : "'$3'"'
echo '=========================================='

echo "Uploading questions..."
echo `cat $3-questions.csv | wc -l` "questions to upload"
# Skip the header line
tail -n+2 $3-questions.csv > tmp
while read NAME
do
    weight=`echo "$NAME" | rev | cut -d, -f1 | rev`
    mid=`echo "$NAME" | rev | cut -d, -f2 | rev`
    name=`echo "$NAME" | rev | cut -d, -f3- | rev`
    curl --data-urlencode "relation=$1" \
         --data-urlencode "name=$name" \
         --data-urlencode "mid=$mid" \
         --data-urlencode "weight=$weight" \
         $URL/addQuizQuestion
done < tmp
rm tmp


# echo "Extracting gold answers...."
# sed "s|xxxxxxxxxx|$1|" < dremel.gold     | dremel --output=csv > $3-gold.csv
# echo `cat $3-gold.csv | wc -l` "gold answers extracted"


# echo "Extracting silver answers...."
# sed "s|xxxxxxxxxx|$2|" < dremel.silver    | dremel --output=csv > $3-silver.csv
# echo `cat $3-silver.csv | wc -l` "silver answers extracted"

# Upload file to quizz.us using curl
