var socketGlobal;
var curSelection = -2;


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

function sendQuestionRequest(){
	var msg ={
			"type":"8",
			"length":"0"
			
	}
	socketGlobal.send(JSON.stringify(msg));
	
}

function sendCatalogChange(catalogName){
    var msg = {
    		"type":"5",
    		"catalogName":catalogName
    		
    }
    socketGlobal.send(JSON.stringify(msg));
	
}

function sendStartGame()
{
	var msg = 
		{
			"type":"7",
			"length":"0"
		}
	socketGlobal.send(JSON.stringify(msg));
}

function mouseClickListener(event) 
{
 	
	var msg = 
	{
			"type": "10",
			"selection" : event.target.id
	};



 	socketGlobal.send(JSON.stringify(msg));
}

