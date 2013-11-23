"use strict";
var OK = 200;
var NOT_OK = 404;
var fs = require('fs');
var CONFIGURE = 'configure';

var CONF1 = '<?xml version=\"1.0\" standalone=\'no\'?><!--*-nxml-*-->\n\
<!DOCTYPE service-group SYSTEM "avahi-service.dtd">\n\
<!-- This file is part of udisks -->\n\n\
<service-group>\n\
  <name replace-wildcards="yes">';

var CONF2 = '</name>\n\n\
  <service>\n\
    <type>_famradio._tcp</type>\n\
    <port>8080</port>\n\
  </service>\n\
</service-group>'

function getName(path, sendName)
{
	fs.readFile(path, function (err, data) {
  	if (err) {
    	sendName(NOT_OK);
    }
 	else
 	{
 		 data = JSON.parse(data);
 		 sendName(OK, data.name);
 	}
});
}
	
function configureRadio(name, sendResult)
{
	var fileData = {name:name};

	fs.writeFile(CONFIGURE, JSON.stringify(fileData), function(err){
	    if(err)
	      sendResult(NOT_OK);
	    else 
	    {
	    	fs.writeFile('/etc/avahi/services/famradio.service',CONF1+name+CONF2, function(err){
	    		if(err)
	    			sendResult(NOT_OK);
	    		else
	    			sendResult(OK);
	    	});
	    }	      
	}); 
}

function routes(app)
{
	app.get('/status', function(req, res){
		fs.exists(CONFIGURE,function(exists){
			if(exists)
			{
				getName(CONFIGURE, function(code,name){
					res.send(code, {status:'running', name:name});
				});
			}
			else
			{
				res.send(OK, {status:'configure'});
			}
		});
	});

	app.post('/configure', function(req, res){
		var name = req.body.name;
		configureRadio(name, function(code){
			res.send(code);
		});		
	});
}

exports.routes = routes;