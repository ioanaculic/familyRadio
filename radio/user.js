"use strict";

var OK = 200;
var ERR = 404;

var mysql = require('node-mysql/node_modules/mysql');
var app = require('./app.js');

var connection = null;



function addUser(id, name, sendResult)
{
		app.connect(function(connection){
			var sql = "insert into users (id,name) values (?,?)";
			var inserts = [id, name];
			sql = connection.format(sql, inserts);
			console.log('sql after format = '+sql);
			connection.query(sql, function(err, result){
				if(err)
				{
					console.log('error = '+err);
					console.log('result = '+result);
					sendResult(ERR);
				}
				else
					sendResult(OK);
			});
		});	
}

function renameUser(id,name, sendResult)
{
	app.connect(function(connection){
		var sql = "update users set name=? where id=?";
		var inserts = [name,id];
		sql = connection.format(sql, inserts);
		connection.query(sql, function(err, result){
			if(err)
				sendResult(ERR);
			else
				sendResult(OK);
		});
	});		
}


function routes(app)
{
	app.post('/add_user', function(req, res){
		var name = req.body.name;
		var id = req.body.id;
		addUser(id,name, function(code){
			res.send(code);
		});
	});

	app.post('/rename_user', function(req, res){
		var name = req.body.name;
		var id = req.body.id;
		renameUser(id,name, function(code){
			res.send(code);
		});
	});
}

exports.routes = routes;

// connection = mysql.createConnection({
// 	  host     : 'localhost',
// 	  user     : 'root',
// 	  password : ''
// 	});

// 	connection.connect(function(err) {
// 		if(!err)
// 			console.log('database connected');
// 	});

// 	connection.query('use familyradio', function(err){
// 		console.log('use database = '+err);
// 	});
// setTimeout(function(){
// // 	addUser('124edf', 'ioana', function(err){console.log('add err = '+err)});
// // addUser('1256', 'iulia', function(err){console.log('add err = '+err)});
// renameUser('1256', 'alex', function(err){console.log('rename err = '+err)});
// }, 2000);

