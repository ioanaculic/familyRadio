var SerialPort = require ('serialport').SerialPort;

var serialPort = new SerialPort("/dev/ttyACM0", {
  baudrate: 9600
}, false); // this is the openImmediately flag [default is true]

serialPort.open(function () {
  console.log('open'); 
  sendName ("FamilyRadio");   
  sendNews ("Online")
  });

serialPort.on ('data', function (data)
{
	// console.log (data)
});


function sendName (name)
{
	var buf = new Buffer (name.length+2);
	buf.writeUInt8 (253, 0);
	buf.write (name, 1, name.length, 'ascii');
	buf.writeUInt8 (0, buf.length-1);
	serialPort.write (buf, function (err)
		{
			console.log (err);
		});
}

function sendNews (news)
{
	var buf = new Buffer (news.length+2);
	buf.writeUInt8 (254, 0);
	buf.write (news, 1, news.length, 'ascii');
	buf.writeUInt8 (0, buf.length-1);
	serialPort.write (buf, function (err)
		{
			console.log (err);
		});
}

exports.sendName = sendName;
exports.sendNews = sendNews;

