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
function receive()
{
	var msgServer = JSON.parse(message.data);
	if(parseInt(msgServer.Type) == 2)
		{
			msgServer.clientid;
			console.log("ID: " + clientid);
			if(clientid == 0)
			{
				window.document.getElementById("start_button").disabled = false;
				document.getElementById("start_button").addEventListener("click", startGame,false);
				var loginButton = document.getElementById("Login_Button");
				loginButton.parentNode.removeChild(loginButton);
				loginButton = document.getElementById("usrname_txt")
				loginButton.parentNode.removeChild(loginButton);
			}
			
			if(clientid != 0)
			{
				var input = document.getElementById("input");
				input.parentNode.removeChild(input);

			

			}
}
}

function login(){
	if(readySend == true && socket != null){
		sendLoginRequest(socket);
		
	}else{
		console.log("Websocket noch nicht bereit zum senden /login.js");
		
	}
	
}
