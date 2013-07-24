./run.sh kc:/music/artist:songs \
	/music/artist/track \
	music_artist_song

./run.sh kc:/music/artist:latest\ album \
	/music/artist/album \
	artist_latest_album	

./run.sh kc:/music/artist:albums \
	/music/artist/album \
	artist_album	

./run.sh kc:/people/person:movies \
	/film/actor/film \
	actor_film

./run.sh kc:/film/film:featured\ song \
	/film/film/featured_song \
	film_featured_song

./run.sh kc:/film/film:director \
	/film/film/directed_by \
	film_director
