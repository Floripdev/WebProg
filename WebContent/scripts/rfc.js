var socketGlobal;

//Sendet einen LoginRequest an den Server über den Socket
function sendLoginRequest(socket)
{
	var playerName = document.getElementById('usrname_txt').value;
	console.log("Sending Login with Name: " + playerName + " length: " + playerName.length + " / rfc.js");
	var msg = {
			"type":"1",
			"length": playerName.length,
			"name":playerName
			
	}
	socketGlobal = socket;
	socketGlobal.send(JSON.stringify(msg));
	
}

//Sendet einen QuestionRequest an den Server über den Socket
function sendQuestionRequest(){
	var msg ={
			"type":"8",
			"length":"0"
			
	}
	socketGlobal.send(JSON.stringify(msg));
	
}

//sendet einen CatalogChange  an den Server über den Socket
function sendCatalogChange(catalogName){
    var msg = {
    		"type":"5",
    		"catalogName":catalogName
    		
    }
    socketGlobal.send(JSON.stringify(msg));
	
}

//Sendet eine Start game an den Server über den Socket
function sendStartGame()
{
	var msg = 
		{
			"type":"7",
			"length":"0"
		}
	socketGlobal.send(JSON.stringify(msg));
}

//Sendet eine beantwortete Frage an den Server über den Socket
function mouseClickListener(event) 
{
 	
	var msg = 
	{
			"type": "10",
			"selection" : event.target.id
	};



 	socketGlobal.send(JSON.stringify(msg));
}

//Sendet einen RestartGame Request an den Server über den Socket
function restartGame(){
	var msg = {
			"type": "150"
			
	};
	socketGlobal.send(JSON.stringify(msg));
	
	
}

