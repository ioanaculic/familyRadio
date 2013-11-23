var SerialPort = require ('serialport').SerialPort;

var serialPort = new SerialPort("/dev/ttyACM0", {
  baudrate: 9600
}, false); // this is the openImmediately flag [default is true]

serialPort.open(function () {
  console.log('open'); 
  // sendName ("javascript");   
  });


function sendName (name)
{
	serialPort.write (new Buffer([253,49,0]), function (err)
		{
			console.log (err);
		});
}
