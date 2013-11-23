
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

my

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
			scanfile (filename, scan);
		}
		else
		{
			scanning = false;
		}
	}
}

function scanfile (filename, done)
{
	child_process.exec ('../echoprint-codegen/echoprint-codegen ../music/'+filename+' 10 30', function (error, stdout, stderr)
	{
		if (!error)
		{
			// console.log (stdout);
			echoserver ('song/identify').post ('application/octet-stream', stdout, function (err, json)
			{
				console.log ('err: '+err);
				console.log ('json: '+JSON.stringify(json));
				if (json.songs && !json.songs[0].error)
				{
					
				}
				else
				{
					done (true);
				}
			});
		}
		else
		{
			console.log ('error: '+error);
			done (true);
		}
	});
}



// fs.readdir ('../music', function (err, files)
// {
// 	console.log (files);
// 	scanSongs (files);
// });
