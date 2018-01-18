var text="Webquiz                                                                    ";
var begin = 0;
var end = text.length;
var playerCnt = 0;
var startButton = false;


window.addEventListener("load", lauftext);
document.addEventListener("DOMContentLoaded", init); //Ruft die Init Funktion auf, sobald der DOMContent geladen ist


//Initialisiert die EventListener für die Buttons und die Kataloge beim Start der Website
function init(){
    //document.getElementById("button_login").addEventListener('click', getPlayerName);
	window.addEventListener("load", loadCatalogs);
    window.addEventListener("load", initLogin);
    
}


//Kreiert einen Lauftext für die Überschrift
function lauftext(){
    document.getElementsByName("Webquiz")[0].value = "" + text.substring(begin, end) + " " + text.substring(0, begin);
    begin++;
    if(begin >= end){
        begin = 0;
        
    }
    window.setTimeout("lauftext()", 150);
    
}

//Nimmt sich den Namen aus dem Login Fenster und übergibt ihn einer Anderen Funktion sind mehr als 2 Spieler angemeldet wird ein Start Button in das Dokument geladen
function getPlayerName(){
    var name = document.getElementById("usrname_txt");
    console.log(name.value);
    if(playerCnt >= 4){
        alert("Zu viele Spieler");
    } else if(name.value === ""){
              alert("Bitte geben Sie einen Namen ein!");
              
              }else{
                addChild(name);
                  playerCnt++;
       
    }
    //Code zum Laden des Start Buttons in das Dokument
    if(playerCnt >= 2 && !startButton){
        var myInput = document.createElement('input');
        
        //Input Element mit Werten für den StartButton füllen
        myInput.value = 'Start Game';
        myInput.type = 'button';
        myInput.id = 'start_button'
        var ausgabe = document.getElementById('login_form');
        ausgabe.appendChild(myInput);
        startButton = true;
        document.getElementById("start_button").addEventListener('click', startButtonPressed);
        
        
    }
    
}

//Name in das Highscore Area einfügen
function addChild(n){
    var myInput = document.createElement('input');
    var myPar = document.createElement('p');
    
    //Input Wert aus n.value übergeben (Name des Spielers)
    myInput.value = n.value;
    
    var ausgabe = document.getElementById('scorefr');
    ausgabe.appendChild(myInput);
    ausgabe.appendChild(myPar);
    
}

//Funktion um den ausgewählten Katalog farblich zu hinterlegen
function catalogSelected(elem){
    var allCatalogs = document.getElementsByName('catalogs');
    for(var i=0; i < allCatalogs.length; i++){
        allCatalogs[i].style.backgroundColor = 'lightgray';
        allCatalogs[i].style.color = 'black';
        
    }
   elem.target.style.backgroundColor ='darkturquoise';
    elem.target.style.color = 'white';
    console.log(elem);
    sendCatalogChange(elem.target.innerText);
    
}

//Überprüft ob der Start Button gedrückt wurde und fügt 4 Radio Elemente hinzu
function startButtonPressed(){
    var ausgabe = document.getElementById('login_form');
    for(var i = 1; i < 5; i++ ){
        createRadio(i, ausgabe); //Aufrufen der Funktion um Radio Buttons in die Website zu laden.
        
    }
    
}

function createRadio(quest, ausgabe){
    //Element erstellen (input, p, label)
    var myInput = document.createElement('input');
    var myPar = document.createElement('p');
    var myLabel = document.createElement('label');
    
    
    //Dem input Tage werden die Werte für die Radio Buttons zugewiesen.
    myInput.type = "radio";
    myInput.name = 'question_radio';
    myInput.value = 'Frage ' + quest + '?';
    myInput.id = 'q' + quest;
    
    var text = myInput.value;
    
    //Dem label den Input und einen Text zuweisen
    myLabel.appendChild(myInput);
    myLabel.appendChild(document.createTextNode(text));
    
    //Paragraph und Label in Website laden
    ausgabe.appendChild(myPar);
    ausgabe.appendChild(myLabel);
    
}