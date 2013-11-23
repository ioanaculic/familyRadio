var serial = require ('serialport');

var serialPort = new SerialPort("/dev/ttyACM0", {
  baudrate: 9600
}, false); // this is the openImmediately flag [default is true]

serialPort.open(function () {
  console.log('open');

  serialPort.write("ls\n", function(err, results) {
    
  });
});