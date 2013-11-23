"use strict";
var Media = require('simple-mplayer');
var events = require('events');
var util = require('util');

module.exports = function Player(song)
{
	events.EventEmitter.call(this);
	this.music = new Media(song);
}
util.inherits(module.exports, events.EventEmitter);
module.exports.prototype.play = function()
{
	this.music.play();
	this.music
}

function stop(music)
{

}

