var webssocket;

function sendLoginRequest(socket){
	var playerName = document.getElementById('usrname_txt').value;
	console.log("Sending Login with Name: " + playerName + " length: " + playerName.length + " / rfc.js");
	var msg = {
			"type":"1",
			"length": playerName.length,
			"name":playerName
			
	}
	socket.send(JSON.stringify(msg));
	
}

function sendQuestionRequest(socket){
	var msg ={
			"type":"8",
			"length":"0"
			
	}
	socket.send(JSON.stringify(msg));
	
}