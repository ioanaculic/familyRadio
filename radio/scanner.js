
"use strict";

var child_process = require ('child_process'),
	fs = require ('fs'),
	http = require ('http'),
	echoprint = require ('echojs'),
	path = require ('path'),
	redis = require("redis"),
    redisclient = redis.createClient(),
    html2text = require ('html-to-text'),
    show = require ('./show.js');

var name = "FamilyRadio";

var echoserver = echoprint ({
	key: 'NXRZR3ZEUQBYXOFUU'
});

var fileslist = [];
var scanning = false;

var MUSIC = '../music/';

function random (n)
{
	return Math.floor((Math.random()*(n-1)));
}

function radio (n)
{
	name = n;
}

function songsList (songs)
{
	var slist = [];
	var size = 0;

	redisclient.zrange (['nexts', 0, -1], function (err, result)
	{
		if (!err)
		{
			size = result.length;
			console.log (result);
			if (size == 0)
			{
				songs (slist);
			}
			else
			result.forEach (function (sno)
			{
				redisclient.hgetall (sno, function (err, sobject)
				{
					sobject.id = sno;
					slist.push (sobject);
					size--;
					if (size == 0)
					{
						songs (slist);
					}
				});
			});
		}
		else
		{
			songs (null);
		}
	});
}

function song (sno, song)
{
	redisclient.hgetall (sno, function (err, sobject)
		{
			sobject.id = sno;
			song (sobject);
		});
}

function stop ()
{
	sendName (name);
	sendNews ("Online");
}

function songsForUsers (users, songs)
{
	var options = ['nexts', users.length+1, 'songs'];
	users.forEach (function (user)
	{
		options.push (user);
	});
	// console.log (options);
	redisclient.zinterstore (options, function (err)
		{
			if (err)
			{
				songs ([]);
			}
			else
			{
				redisclient.zrange (['nexts', 0, -1], function (err, result)
				{
					songs (result);
				});
			}
	}); 
}

function userOnline (user)
{
	redisclient.sadd ('users', user, function (result)
	{
		// console.log (result);
	});
}

function userOffline (user)
{
	redisclient.srem ('users', user, function (result)
	{
		// console.log (result);
	});
}

function nextPressed (songid, user)
{
	redisclient.zincrby (user, 1, songid, function (err)
		{
			// console.log (err);
		});
}

function played (songid)
{
	redisclient.zincrby ('songs', 10, songid, function (err)
		{
			// console.log (err);
		});
}

function play (songid, userid)
{
	console.log ('songid: '+songid);
	redisclient.hgetall (songid, function (err, song)
	{
		if (song)
		{
			if (userid)
			{
				redisclient.zincrby (userid, -1, songid, function (err)
				{
					// console.log (err);
				});
			}
			show.sendName (song.artist+' - '+song.song);
			show.sendNews ('Reading news ...');
			echoserver ('artist/news').get ({
								name: song.artist
							}, function (err, json)
							{
								// console.log ('err: '+err);
								// console.log ('json: '+JSON.stringify(json, null, 3));
								if (json.response.news)
								{
									try
									{
										var sn = json.response.news[0].summary;
										if (sn.length > 100) sn = sn.substring (0, 100)+'...';
										show.sendNews (sn);
									}
									catch (e)
									{
										console.log (e);
										show.sendNews ('no interesting news');
									}
									// show.sendNews (json.response.news[0].summary);
								}
								else
								{
									show.sendNews ('no interesting news');
								}
							});
		}
		else
		{
			show.sendName ('no songs');
			show.sendNews ('Reading news ...');
		}
	});
}

function nextSong (playSong)
{
	// playSong ('song');

	redisclient.smembers ('users', function (err, users)
		{
			var songs = function (songslist)
			{
				if (songslist.length == 0)
				{
					users.splice (0,1);
					songsForUsers (users, songs);
				}
				else
				{
					var song = songslist[Math.min (songslist.length, random(2))];
					played (song);
					playSong (song);
				}	
			}
			if (users.length > 0)
			{
				songsForUsers (users, songs);
			}
			else
			{
				playSong (null);
			}
		});
}

function addSong (artist, song, songid, user, added)
{
	function addSongToUser (user, songid)
	{
		console.log ('song to user');
		redisclient.zadd ([user, 0, songid], function (err, result)
		{
			// console.log (result);
			added ();
		});
	}

	redisclient.hgetall (songid, function (err, result)
	{
		if (!result)
		{
			console.log ('adding song');
			redisclient.hmset (songid, {
				artist:artist,
				song:song
			}, function (result)
			{
				console.log (result);
				redisclient.zadd (['songs', 0, songid], function (err, result)
				{
					console.log (result);
					addSongToUser (user, songid);
				});	
			});
		}
		else
		{
			addSongToUser (user, songid);
		}
	});
}

function scanSongs (filenames, user)
{
	filenames.forEach (function (filename)
	{
		fileslist.push ({filename:filename, user:user});
	});
	console.log (fileslist);
	scan ();
}

function scan (err)
{
	if (!scanning || err!=undefined)
	{
		scanning = true;
		if (fileslist.length > 0)
		{
			var file = fileslist[0];
			console.log ('scanning filename: '+file.filename);
			fileslist.splice (0, 1);
			scanfile (file, removefile);
		}
		else
		{
			scanning = false;
		}
	}
}

function removefile (filename, dbid)
{
	if (filename) 
	{
		console.log ('moving file');
		if (dbid)
		{
			console.log ('accepted file');
			fs.rename (filename, MUSIC+dbid+'.mp3', function (err)
			{
				if (!err)
				{

				}
				else
				{
					console.log ('error, removing file');
					fs.unlink (filename, function (err)
					{

					});
					console.log ('error move: ['+filename+'] '+err);
				}
				scan (filename);
			});	
		}
		else
		{
			console.log ('no dbid, removing file');
			fs.unlink (filename, function (err)
					{

					});
			scan (filename);
		}
	}
}

function scanfile (file, done)
{
	var filename = file.filename;
	var user = file.user;
	child_process.exec ('../echoprint-codegen/echoprint-codegen "'+filename+'" 10 30', function (error, stdout, stderr)
	{
		if (!error)
		{
			// console.log (stdout);
			echoserver ('song/identify').post ('application/octet-stream', stdout, function (err, json)
			{
				// console.log ('err: '+err);
				// console.log ('json: '+JSON.stringify(json));
				if (json)
				{
					// console.log (json.songs[0].error);
					if (json.response.songs && !(json.response.songs[0].error))
					{
						var j = json;
						console.log ('searching genres');
						echoserver ('artist/terms').get ({
							id: json.response.songs[0].artist_id,
							sort: 'weight'
						}, function (err, jsons)
						{
							// console.log ('err: '+err);
							// console.log ('jsons: '+jsons);
							if (jsons.response.terms)
							{
								addSong (j.response.songs[0].artist_name, j.response.songs[0].title, j.response.songs[0].id, user, function (err)
								{
									done (filename, j.response.songs[0].id);
								});
							}
							else
							{
								done (filename);
							}
						});
					}
					else
					{
						done (filename);
					}
				}
				else
				{
					done (filename);
				}
			});
		}
		else
		{
			console.log ('error: '+error);
			done (filename);
		}
	});
}

function init ()
{
}

/*userOnline ('annutza');

nextSong (function (song)
	{
		console.log (song);
	});

fs.readdir ('/home/pi/familyRadio/temp', function (err, files)
{
	files.forEach (function (filename)
	{
		scanSongs (['/home/pi/familyRadio/temp/'+filename], 'annutza');
	});
});*/
// scanSongs (['2.mp3'],'annutza');

// backPressed ('SOIFMMK134FD0DAAE6', 'annutza');

// init ();

exports.init = init;
exports.userOnline = userOnline;
exports.userOffline = userOffline;
exports.play = play;
exports.song = song;
exports.stop = stop;
exports.songsList = songsList;
exports.nextPressed = nextPressed;
exports.nextSong = nextSong;
exports.scanSongs = scanSongs;
