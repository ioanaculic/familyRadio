
"use strict";

var child_process = require ('child_process'),
	fs = require ('fs'),
	http = require ('http'),
	echoprint = require ('echojs'),
	mysql = require ('node-mysql');

var echoserver = echoprint ({
	key: 'NXRZR3ZEUQBYXOFUU'
});

var fileslist = [];
var scanning = false;

var connection = mysql.createConnection 

var MUSIC = '../music/';

function addSong (artist, song, songid)
{

}

function scanSongs (filenames)
{
	filenames.forEach (function (filename)
	{
		fileslist.push (filename);
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
			var filename = fileslist[0];
			console.log ('scanning filename: '+filename);
			fileslist.splice (0, 1);
			scanfile (filename, removefile);
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
		if (dbid)
		{
			fs.rename (filename, MUSIC+dbid, function (err)
			{
				if (!err)
				{

				}
				else
				{
					fs.unlink (filename, function (err)
					{

					});
					console.log ('error move: ['filename+'] '+err);
				}
				scan ();
			});	
		}
		else
		{
			fs.unlink (filename, function (err)
					{

					});
			scan ();
		}
	}
}

function scanfile (filename, done)
{
	child_process.exec ('../echoprint-codegen/echoprint-codegen '+filename, function (error, stdout, stderr)
	{
		if (!error)
		{
			console.log (stdout);
			echoserver ('song/identify').post ('application/octet-stream', stdout, function (err, json)
			{
				console.log ('err: '+err);
				console.log ('json: '+JSON.stringify(json));
				if (json)
				{
					// console.log (json.songs[0].error);
					if (json.response.songs && !(json.response.songs[0].error))
					{
						console.log ('searching genres');
						echoserver ('artist/terms').get ({
							id: json.response.songs[0].artist_id,
							sort: 'weight'
						}, function (err, json)
						{
							if (json.response.terms)
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

fs.readdir ('../music', function (err, files)
{
	console.log (files);
	scanSongs (files);
});

exports.scanSongs = scanSongs;
