var socket;
var readySend = false;

function initLogin(){
	var loginButton = document.getElementById('button_login');
	var url = 'ws://localhost:8080/Aufgabe5/Echo';
	console.log("URL=" + url);
	
	socket = new WebSocket(url);
	
	socket.onopen = openSend;
	socket.onerror = errorHandling;
	socket.onclose = closingSocket;
	socket.onmessage = recive;
	loginButton.addEventListener('click', login)
	
}

//Funktion um das senden zu erlauben
function openSend(){
	readySend = true;
	console.log("WebSocket | js: Senden nun möglich");
	
}

//Funktion zur Fehlerbehandlung
function errorHandling(event){
	console.log("WebSocket | js: Fehler bei den WebSockets Fehler-->" + event.data);
	
}

//Funktion zum schließen des Sockets,
function closingSocket(event){
	console.log("WebSocket | js: Websocket geschlossen -->" + event.code);
	
}

//Funktion zum Empfangen von Daten
function receive(message)
{
	var msgServer = JSON.parse(message.data);
	
	switch(parseInt(msgServer.Type))
	{
	case loginRequest: 
	    var ausgabe = document.getElementById("mainarea");
		var loginButton = document.getElementById("button_login");
		var p_uname = document.getElementById("p_uname");
		var usrname_txt = document.getElementById("usrname_txt");
		loginButton.remove();
		p_uname.remove();
		usrname_txt.remove();
		
		if(isSuperuser === 1)
			{   
			    var startButton = document.createElement("input");
			    startButton.type = "button";
			    startButton.id = "start_button";
			    startButton.value = "Spiel Starten";
			    ausgabe.appendChild(startButton);
			 	document.getElementById("start_button").addEventListener("click",startGame,false);
			}
		else
			{
				var waitScreen = document.createElement("p");
				waitScreen.id = "p_wait";
				ausgabe.appendChild(waitScreen);
				ausgabe.appendChild(document.createTextNode("Warten auf Spielleiter..."))
			}
	case 20:					//Superuser Type
		isSuperuser = 1;
		break;
		
	
 

	}


}

function login(){
	if(readySend == true && socket != null){
		sendLoginRequest(socket);
		
	}else{
		console.log("Websocket noch nicht bereit zum senden /login.js");
		
	}
	
}