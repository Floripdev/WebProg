var socket;
var readySend = false;
var isSuperuser = false;

// Variablen für die RFC-Type Codes
var RECV_LOGINREQUEST_TYPE = 1;
var RECVCATALOGCHANGE_TYPE = 2;
var RECVSTART_GAME_TYPE = 3;
var RECVPLAYERLIST_TYPE = 6;
var RECVQUESTIONREQUEST_TYPE = 9;
var RECVGAMEOVER_TYPE = 11;
var RECVQUESTION_ANSWERED_TYPE = 12;
var RECVSUPERUSER_TYPE = 20;
var RECVTIMEOUT_TYPE = 27;
var RECVQUESTION_EMPTY_TYPE = 90;
var ERROR_MSG = 255;

//WebSocket kommunikation initialisieren
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
	
	switch(parseInt(msgServer.type))// Switch auf den Message Type vom Server
	{
	case RECV_LOGINREQUEST_TYPE: 
		//mainarea clearen damit neue Elemente angezeigt werden können
	    var ausgabe = document.getElementById("mainarea");
		var loginButton = document.getElementById("button_login");
		var p_uname = document.getElementById("p_uname");
		var usrname_txt = document.getElementById("usrname_txt");
		loginButton.remove();
		p_uname.remove();
		usrname_txt.remove();
		
		//Wenn der user SuperUser ist wird ein Startbutton eingefügt und die Listener für Kataloge initalisiert
	 	if(isSuperuser)
			{   
			    var startButton = document.createElement("input");
			    startButton.type = "button";
			    startButton.id = "start_button";
			    startButton.value = "Spiel Starten";
			    ausgabe.appendChild(startButton);
				document.getElementById("start_button").disabled = true;
				document.getElementById("start_button").addEventListener("click",sendStartGame,false);
				
				for(var x = 1; x < wert.length+1; x++)
				{
					var cat = document.getElementById("cat"+x);
					cat.addEventListener('click', catalogSelected);
				}
			}
		else //Normaler User -> bekommt Nachricht dass der Spielleiter das Spiel noch nicht gestartet hat
			{
				var waitScreen = document.createElement("p");
				waitScreen.id = "p_wait";
				waitScreen.appendChild(document.createTextNode("Warten auf Spielleiter..."));
				ausgabe.appendChild(waitScreen);
			}
	 	break;
	 	//Verarbeiten des Superuser Types
	case RECVSUPERUSER_TYPE:
		isSuperuser = true; //SuperUser == true
		break;
		
		//Verarbeitung bei veränderung des Katalogs
	case RECVCATALOGCHANGE_TYPE:
		//SuperUser Button aktivieren 
		if(isSuperuser)
			{
				document.getElementById("start_button").disabled = false;
			}
		else //Clients bekommen den aktuell ausgewählten Katalog farblich hinterlegt
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
		
		//Verarbeitung des StartGames
	case RECVSTART_GAME_TYPE:
		//Entfernen des StartButtons und der WaitScreens
		var startbutton = document.getElementById("start_button");
		
		var pwait = document.getElementById("p_wait");
		if(isSuperuser)
		{	
			startbutton.remove();
		}
		else
		{
			pwait.remove();
		}
		//Erste frage holen
		sendQuestionRequest();
		break;
		
		//Verarbeitet die Fragen die vom Client kommen
	case RECVQUESTIONREQUEST_TYPE:
		var del = document.getElementById("questSektion");
		
		//Wenn die QuestionSektion existiert löschen und neu erstellen
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
		
		//Verarbeitet dei PlayerList(Broadcast)
	case RECVPLAYERLIST_TYPE:
		//Erstellen der Spielerliste Struktur
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
			
			//Name und Score aus der ServerMessage in die tabelle einfügen
			cell1.innerHTML = msgServer.players[i].name; 
			cell2.innerHTML = msgServer.players[i].score;
		}
		break;
		
		//Verarbeitet Leere Questions
	case RECVQUESTION_EMPTY_TYPE:
		//Aktuelle Frage löschen und Warte Screen erstellen
		var remQuest = document.getElementById("questSektion");
		remQuest.remove();
		var output = document.getElementById("mainarea");
		var end = document.createElement("h1");
		end.id = "wait";
		end.appendChild(document.createTextNode("Warten auf andere Spieler"));
		output.appendChild(end);
		break;
		
		//Verarbeitet die beantwortete Frage
	case RECVQUESTION_ANSWERED_TYPE:
		var correctAnswer = msgServer.correct;
		console.log("CorrrectAnswer: " + correctAnswer);
		//Wenn die Frage keinen Timeout hatte werden die Richtigen und Falschen Antworten Grün/Rot angezeigt, Listener entfernen
		if(correctAnswer != -1){
			for(var i = 0; i < 4; i++){
				var listener = document.getElementById(i);
				listener.removeEventListener("click", mouseClickListener, false);
				var correctRadio = document.getElementById(i);
				var label = document.getElementById("label"+i);
				if(correctRadio.id === correctAnswer){
					label.style.color = "green";
					
				}else{
					label.style.color = "red";
					
				}
				
			}
			
			
		}else{
			//Alle Fragen Rot anzeigen da Timeou, Listener entfernen
			for(var i = 0; i < 4; i++){
				var label = document.getElementById("label"+i);
				label.style.color = "red";
				
				var listener = document.getElementById(i);
				listener.removeEventListener("click", mouseClickListener, false);
				
			}	
		}
		//3 Sekunden Warten und neue Frage holen
		setTimeout(function(){
			sendQuestionRequest();
			}, 3000);
		break;
		
	case RECVGAMEOVER_TYPE:	
		var output = document.getElementById("mainarea");
		var clear = document.getElementById("wait");
		var restart = document.createElement("input");
		clear.remove();
		if(msgServer.score === '0'){
			var noWinner = document.createElement("h1");
			noWinner.appendChild(document.createTextNode("Kein Spieler hat Gewonnen!"));
			output.appendChild(noWinner);
			
		}else{
			var endGame = document.createElement("h1");
			endGame.appendChild(document.createTextNode("Spieler " + msgServer.name + " hat gewonnen mit einem Score von: " +msgServer.score));
			output.appendChild(endGame);	
			
		}
		
		restart.id = "restart";
		restart.value = "Neues Spiel starten";
		restart.type = "button";
		restart.addEventListener("click", restartGame, false);
		if(isSuperuser){
			output.appendChild(restart);
			
		}
		break;
		//Verarbeitung von ErrorMessages vom Server mit alert
	case ERROR_MSG:
		alert("Fehler vom Server erhalten mit dem SubType: " + msgServer.subtype + ". Fehler Message: " + msgServer.msg);
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

