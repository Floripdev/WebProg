var socket;
var numberOfPlayers = 0;
var readySend = false;
var isSuperuser = 0;
var RECV_LOGINREQUEST_TYPE = 1;
var RECVSUPERUSER_TYPE = 20;
var RECVCATALOGCHANGE_TYPE = 2;
var RECVSTART_GAME_TYPE = 3;
var RECVQUESTIONREQUEST_TYPE = 9;
var RECVPLAYERLIST_TYPE = 6;
var ERROR_MSG = 255;
var RECVQUESTION_ANSWERED_TYPE = 12;
var RECVQUESTION_EMPTY_TYPE = 90;


function initLogin()
{
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
function openSend()
{
	readySend = true;
	console.log("WebSocket | js: Senden nun möglich");
	
}

//Funktion zur Fehlerbehandlung
function errorHandling(event)
{
	console.log("WebSocket | js: Fehler bei den WebSockets Fehler-->" + event.data);
	
}

//Funktion zum schließen des Sockets,
function closingSocket(event)
{
	console.log("WebSocket | js: Websocket geschlossen -->" + event.code);
	
}

//Funktion zum Empfangen von Daten

			

function recive(message)
{
	var msgServer = JSON.parse(message.data);
	console.log("Message from server type: "+parseInt(msgServer.type)+ "msg: " +JSON.parse(message.data));
	
	switch(parseInt(msgServer.type))
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
				document.getElementById("start_button").disabled = true;
				
				for(var x = 1; x < wert.length+1; x++)
				{
					var cat = document.getElementById("cat"+x);
					cat.addEventListener('click', catalogSelected);
				}
			}
		else
			{
				var waitScreen = document.createElement("p");
				waitScreen.id = "p_wait";
				waitScreen.appendChild(document.createTextNode("Warten auf Spielleiter..."));
				ausgabe.appendChild(waitScreen);
			}
	 	break;
	case RECVSUPERUSER_TYPE:					//Superuser Type
		isSuperuser = 1;
		break;
		
		
	case RECVCATALOGCHANGE_TYPE:
		if(isSuperuser === 1)
			{
				document.getElementById("start_button").addEventListener("click",sendStartGame,false);
				document.getElementById("start_button").disabled = false;
			}
		else
			{
				var sendCat = null;
				var curCat = document.getElementsByName("catalogs");
				for(var i = 0;i < curCat.length;i++)
					{
						if(curCat[i].innerText === msgServer.data)
							sendCat = curCat[i];
					}
				catalogSelected2(sendCat);
			}
			
		break;
		
	case RECVSTART_GAME_TYPE:
		var startbutton = document.getElementById("start_button");
		
		var pwait = document.getElementById("p_wait");
		if(isSuperuser === 1)
		{	
			startbutton.remove();
		}
		else
		{
			pwait.remove();
		}
		sendQuestionRequest();
		break;
		
	case RECVQUESTIONREQUEST_TYPE:
		var del = document.getElementById("questSektion");

		if(del !== null)
		{
			del.remove();
		}

		var div = document.createElement('div');
		div.id = "questSektion";
		var ausgabe = document.getElementById("mainarea");
		ausgabe.appendChild(div);
		createQuestion(msgServer.question);
		
		for(var i = 0;i < 4;i++)
			{
				createRadio(msgServer.answer[i],i)
				var listener = document.getElementById(i);
				listener.addEventListener("click",mouseClickListener,false);
				
			}
		
		break;
		
	case RECVPLAYERLIST_TYPE:
		var table = document.getElementById("table_score");
		table.parentElement.removeChild(table);
		
		table = document.createElement("table");
		table.id="table_score";
		
		var score = document.getElementById("scorefr");
		score.appendChild(table);
		for(var i = 0; i < msgServer.count; i++)
		{
			var row = table.insertRow(0);
			
			var cell1 = row.insertCell(0);
			var cell2 = row.insertCell(1);
			
			
			cell1.innerHTML = msgServer.players[i].name;
			cell2.innerHTML = msgServer.players[i].score;
			
			console.log("Added Player" + i + " from " + msgServer.count + " Players to list " + msgServer.players[i].name );
		}
		break;
	case ERROR_MSG:
		alert("Fehler vom Server erhalten mit dem SubType: " + msgServer.subtype + ". Fehler Message: " + msgServer.msg);
		break;
	
	
	case RECVQUESTION_EMPTY_TYPE:
		var hinweis = document.createTextNode("Warten auf andere Spieler");
		brak;
		
		
	case RECVQUESTION_ANSWERED_TYPE:
		var correctAnswer = msgServer.correct;
		console.log("CorrrectAnswer: " + correctAnswer);
		if(correctAnswer != -1){
			for(var i = 0; i < 4; i++){
				var correctRadio = document.getElementById(i);
				if(correctRadio.id === correctAnswer){
					correctRadio.style.background = "green";
					
				}else{
					correctRadio.style.background = "red";
					
				}
				
			}
			
			
		}
		for(var i = 0;i < 4;i++)
		{
			
			var listener = document.getElementById(i);
			listener.removeEventListener("click",mouseClickListener,false);
			
		}
		setTimeout(sendQuestionRequest(), 3000);
		
		break;
			
			
		
	
}
}


function login()
{
	if(readySend == true && socket != null)
	{
		sendLoginRequest(socket);
	}
	else
	{
		console.log("Websocket noch nicht bereit zum senden /login.js");
		
	}
}

