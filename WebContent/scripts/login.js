var socket;
var readySend = false;
var isSuperuser = 0;
var RECV_LOGINREQUEST_TYPE = 1;
var RECVSUPERUSER_TYPE = 20;
var RECVCATALOGCHANGE = 2;


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
	case RECV_LOGINREQUEST_TYPE: 
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
			    for(var x = 1; x < wert.length+1; x++)
				{
					var cat = document.getElementById("cat"+x);
					cat.addEventListener('click', catalogSelected);
				}
				document.getElementById("start_button").addEventListener("click",startGame,false);
				document.getElementById("start_button").disabled = true;
			}
		else
			{
				var waitScreen = document.createElement("p");
				waitScreen.id = "p_wait";
				ausgabe.appendChild(waitScreen);
				ausgabe.appendChild(document.createTextNode("Warten auf Spielleiter..."))
			}
	case RECVSUPERUSER_TYPE:					//Superuser Type
		isSuperuser = 1;
		break;
		
		
	case RECVCATALOGCHANGE:
		if(isSuperuser === 1)
			{
				document.getElementById("start_button").disabled = false;
			}
		else
			{
				
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
