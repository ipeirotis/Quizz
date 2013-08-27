#!/bin/bash

set -e

URL="http://www.quizz.us/"

function upload {
    # PARAM1: The KP relation
    # PARAM2: The KV predicate
    # PARAM3: Friendly quiz name
    echo '=========================================='
    echo 'KP Relation  : "'$1'"'
    echo 'KV Predicate : "'$2'"'
    echo 'Quiz name    : "'$3'"'
    echo '=========================================='

    echo 'Making quizz'
    curl --data-urlencode "relation=$1" \
         --data-urlencode "name=$3" \
         --data-urlencode "text=tekscik_$1" \
         --data-urlencode "budget=1000" \
         $URL/addQuiz


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
    curl --get --data-urlencode "cache=no" \
         --data-urlencode "quiz=$1" \
         $URL/api/getQuizCounts
}

upload kc:/music/artist:songs \
	/music/artist/track \
	music_artist_song

upload kc:/music/artist:latest\ album \
	/music/artist/album \
	artist_latest_album

upload kc:/music/artist:albums \
	/music/artist/album \
	artist_album

upload kc:/people/person:movies \
	/film/actor/film \
	actor_film

upload kc:/film/film:featured\ song \
	/film/film/featured_song \
	film_featured_song

upload kc:/film/film:director \
	/film/film/directed_by \
	film_director
