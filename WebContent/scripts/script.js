var text="Webquiz                                                                    ";
var begin = 0;
var end = text.length;


window.addEventListener("load", lauftext);
document.addEventListener("DOMContentLoaded", init); //Ruft die Init Funktion auf, sobald der DOMContent geladen ist


//Initialisiert die Websockets und lädt die Kataloge
function init(){
	window.addEventListener("load", loadCatalogs);
    window.addEventListener("load", initLogin); //WebSockets beim load initalisieren
    
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

//Funktion um den ausgewählten Katalog farblich zu hinterlegen (only SuperUser)
function catalogSelected(elem){
    var allCatalogs = document.getElementsByName('catalogs');
    for(var i=0; i < allCatalogs.length; i++){
        allCatalogs[i].style.backgroundColor = 'lightgray';
        allCatalogs[i].style.color = 'black';
        
    }
    elem.target.style.backgroundColor ='darkturquoise';
    elem.target.style.color = 'white';
    sendCatalogChange(elem.target.innerText); //Aktuellen Katalog in die RFC senden und Server übergeben
        
}

//Für die einfärbung der Kataloge für die Clients die nicht superUser sind
function catalogSelected2(elem){
    var allCatalogs = document.getElementsByName('catalogs');
    for(var i=0; i < allCatalogs.length; i++){
        allCatalogs[i].style.backgroundColor = 'lightgray';
        allCatalogs[i].style.color = 'black';
        
    }
    elem.style.backgroundColor ='darkturquoise';
    elem.style.color = 'white';
	
}

//Radio Button mit der dazugehörigen Antwort erstellen und einfügen
function createRadio(answer,cnt)
{
	var out = document.getElementById("questSektion");
    //Element erstellen (input, p, label)
    var myInput = document.createElement('input');
    var myPar = document.createElement('p');
    var myLabel = document.createElement('span');
    var myPar2 = document.createElement('p');
    

    
    
    //Dem input Tage werden die Werte für die Radio Buttons zugewiesen.
    myInput.type = "radio";
    myInput.name = 'question_radio';
    myInput.value = answer;
    myInput.id = cnt;
    myLabel.id = "label"+cnt;
    
    
     
    var text = myInput.value;
    
    //Dem label den Input und einen Text zuweisen
    myLabel.appendChild(myInput);
    myLabel.appendChild(document.createTextNode(text));
    
    //Paragraph und Label in Website laden
    out.appendChild(myPar);
    out.appendChild(myLabel);
    
}

//Erstellen der Frage und einfügen in die Website
function createQuestion(questString)
{		
	var quest = document.createElement("h1");
	var out = document.getElementById("questSektion");
	quest.appendChild(document.createTextNode(questString));
	out.appendChild(quest);
}

