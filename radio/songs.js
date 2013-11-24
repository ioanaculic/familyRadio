"use strict";

var mysql = require('node-mysql');
var fs = require('fs');
var scanner = require('./scanner');

var connection = null;

function routes(app)
{
	connection = connection;
	app.post('/upload/:user', function(req, res){
		//console.log('add song');
		//console.log (JSON.stringify (req.files, null, 4));
		scanner.scanSongs([req.files.songfile.path], req.params.user);
		res.send(200);
	});

}

exports.routes = routes;