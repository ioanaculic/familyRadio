"use strict";

var Media = require('simple-mplayer');
var songs = [];
var scanner = require('./scanner');


var music = null;
var action = null;
var s = null;
var user;
var id;

function routes(app)
{
	app.get("/stop",function(req, res){
		action = 'stop';
		if(music)
		{
			music.stop();
		}
		res.send(200);
	});

	app.get("/next/:user", function(req, res){
		action = 'next';
		console.log('next');
		if(music)
		{
			console.log('music');
			music.stop();
			//scanner.nextPressed('aksdkf',user)
		}
		res.send(200);
	});

	app.get("/play/:user/:id",function(req,res){
		action = 'play';
		user = req.params.user;
		id = req.params.id;
		music.stop();
		res.send(200);
	});

	app.get("/get_song/:user",function(req,res){
		console.log('s='+s);
		scanner.song(s,function(song){
			console.log(song);
			console.log(song.artist+'-'+song.song);
			res.send(200,{name:song.artist+'-'+song.song});
		});
		
	});

	app.get("/list_songs/:user",function(req,res){
		var s=[];
		scanner.songsList(function(songs){
			if(songs)
			{
				for(var i=0; i<songs.length; i++)
				{
					s.push({name:songs[i].artist+'-'+songs[i].song,id:songs[i].id});
				}
				res.send(200,s);
			}
			else
			{
				res.send(200,[]);
			}

		});
	});
}

function play(oldSong,user)
{
	console.log("song = "+oldSong);
	if(!music)
	{
		var song = '../music/'+oldSong+'.mp3';
		s=oldSong;
		console.log("!music"+s);
		music = new Media(song);
		music.play();
		scanner.play(s,user);
		scanner.nextSong(function(song){
			if(song)
				play(song);
		});
		
		music.on('complete',function(){
			console.log('complete');
			if(action == 'next')
			{
				music = null;
				songs.splice(0,1);
				if(songs.length > 0)
				{
					s=songs[0];
					play(songs[0]);
				}
				else
					scanner.nextSong(function(song){
						play(song);
						s = song;
						scanner.play(song,user);
					});
			}
			else if(action == 'stop')
			{
				scanner.stop(s);
				songs.splice(0);
				music = null;
			}
			else if(action == 'play')
			{
				music = null;
				songs.splice(0);
				play(id, user);
			}
			else
			{
				music = null;
				if(songs.length > 0)
				{
					play(songs[0]);
					s=songs[0];
					songs.splice(0,1);
				}
			}
			
		});

		music.on('stop',function(){
			console.log('stop event');
			
			
		});	
	}
	else
	{
			songs.push(oldSong);
	}	
}
// var k=0;
// function nextSong(callback)
// {
// 	if(k==0){
// 	callback("/home/pi/familyRadio/music/121.mp3"); k=1;}
// }

// play("/home/pi/familyRadio/music/linuxeyes.mp3");

exports.play = play;
exports.routes = routes;