var SAAgent = null;
var SASocket = null;
var CHANNELID = 109;
var ProviderAppName = "FindMyPhoneTizenService";

function setText(text) {
	textbox.innerHTML = text;
}

function onerror(err) {
	console.error("err [" + err + "]");
}

var agentCallback = {
		onconnect : function(socket) {
			SASocket = socket;
			setText("Connected.");
			SASocket.setSocketStatusListener(function(reason) {
				console.error("Service connection lost, Reason : [" + reason + "]");
				disconnect();
			});
			SASocket.setDataReceiveListener(onreceive);
			
			enableButtons();
		},
		onerror : onerror
};

var peerAgentFindCallback = {
		onpeeragentfound : function(peerAgent) {
			try {
				if (peerAgent.appName == ProviderAppName) {
					SAAgent.setServiceConnectionListener(agentCallback);
					SAAgent.requestServiceConnection(peerAgent);
				} else {
					setText("Not expected app!! : " + peerAgent.appName);
				}
			} catch (err) {
				console.error("exception [" + err.name + "] msg[" + err.message + "]");
			}
		},
		onerror : onerror
};

function onsuccess(agents) {
	try {
		if (agents.length > 0) {
			SAAgent = agents[0];
			SAAgent.setPeerAgentFindListener(peerAgentFindCallback);
			SAAgent.findPeerAgents();
		} else {
			setText("Not found SAAgent!!");
		}
	} catch (err) {
		console.error("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function connect() {
	if (SASocket) {
		setText('Already connected!');
		return false;
	}
	try {
		webapis.sa.requestSAAgent(onsuccess, function(err) {
			console.error("err [" + err.name + "] msg[" + err.message + "]");
		});
	} catch (err) {
		console.error("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function disconnect() {
	try {
		if (SASocket != null) {
			SASocket.close();
			SASocket = null;
			setText("Disconnected.");
		}
	} catch (err) {
		console.error("exception [" + err.name + "] msg[" + err.message + "]");
	}
	disableButtons();
}

function onreceive(channelId, data) {
	setText(data);
}

function play() {
	try {
		SASocket.sendData(CHANNELID, "play");
	} catch (err) {
		console.error("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function stop() {
	try {
		SASocket.sendData(CHANNELID, "stop");
	} catch (err) {
		console.error("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function enableButtons() {
	var buttons = document.getElementsByTagName("button");
	for (var i = 0; i < buttons.length; i++) {
		buttons[i].disabled = false;
	}
}

function disableButtons() {
	var buttons = document.getElementsByTagName("button");
	for (var i = 0; i < buttons.length; i++) {
		buttons[i].disabled = true;
	}
}