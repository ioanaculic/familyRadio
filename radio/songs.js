"use strict";

var mysql = require('node-mysql');
var fs = require('fs');
var scanner = require('scanner');

var connection = null;

function routes(app)
{
	connection = connection;
	app.post('/add_song', function(req, res){
		console.log('add song');
		console.log (JSON.stringify (req.files, null, 4));
		scanner.scanSongs(req.file.songfile.path);
	});

}

exports.routes = routes;