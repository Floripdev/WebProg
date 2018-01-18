var request = null;

function loadCatalogs(){
	request = new XMLHttpRequest;
	request.open("GET", "catalogLoadServlet", true);
	request.onreadystatechange = getCatalogNames;
	request.send(null);
	
}

function getCatalogNames(){
	//Daten vollständig erhalten (readystat = 4) == Catalogs einfügen
	if(request.readyState == 4){
		
		//XML-String mit den Katalogen vom Servlet holen
		var wert=request.responseXML.getElementsByTagName("catalogname");
		
		//Cataloge einfügen
		for(var i = 0; i < wert.length; i++){
			createCat(wert[i].firstChild.nodeValue, i);	
			
		}
		
		//Catalog Listenern deklarieren
		for(var x = 1; x < wert.length+1; x++){
			var catTMP = document.getElementById("cat"+x);
			catTMP.addEventListener('click', catalogSelected);
			
			
		}
		
	}
	
}

//Funktion zum erzeugen der Catalog <div>-Boxen
function createCat(catName, count){
	var ausgabe = document.getElementById("catalogarea");
	var myDiv = document.createElement('div');
	count+=1;
	myDiv.id = "cat" + count;
	myDiv.setAttribute("name", "catalogs");
	myDiv.setAttribute("class", "catas");
	
	//Einfügen der Div Box
	myDiv.appendChild(document.createTextNode(catName));
	ausgabe.appendChild(myDiv);

}