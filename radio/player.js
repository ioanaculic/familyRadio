"use strict";
var Media = require('simple-mplayer');

function play(song)
{
	var music = new Media(song);
	music.play();
	music.on('terminate',function(){
		nextSong();
	});
}

exports.play = play;
