
/**
 * Module dependencies.
 */
"use strict";

var express = require('express');
var routes = require('./routes');
var user = require('./routes/user');
var http = require('http');
var path = require('path');

var app = express();
var status = require('./status.js');
var user = require('./user.js');
var songs = require('./songs.js');
var scanner = require('./scanner');
var player = require('./player');

var mysql = require('node-mysql/node_modules/mysql');

// all environments

app.set('port', process.env.PORT || 3000);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.bodyParser({uploadDir:'/tmp'}));
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(app.router);
app.use (express.limit (10*1024*1024));
// app.use(express.multipart());
app.use(express.static(path.join(__dirname, 'public')));

// development only
// if ('development' == app.get('env')) {
//   app.use(express.errorHandler());
// }

// app.get('/', routes.index);
// app.get('/users', user.list);

// http.createServer(app).listen(app.get('port'), function(){
//   console.log('Express server listening on port ' + app.get('port'));
// });

function connect(sendResponse)
{
	var connection = mysql.createConnection({
	  host     : 'localhost',
	  user     : 'root',
	  password : ''
	});

	connection.connect(function(err) {
		if(!err)
			console.log('database connected');
	});

	connection.query('use familyradio');

	sendResponse(connection);
}

scanner.init(connect);

app.listen(8080);
console.log('listenint on port 80');
status.routes(app);
user.routes(app);
songs.routes(app);
player.routes(app);

exports.connect = connect;
scanner.nextSong(function(song){
	player.play(song);
});
