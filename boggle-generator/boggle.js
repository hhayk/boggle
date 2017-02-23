function boggle(){
	window.result = "";

	var upperLimit = 10000;
	for (var rrrr = 0; rrrr < upperLimit; rrrr++) {
	   var i = 0, k = 0 ;
	   var ranvar, last, save;
	   var g = new Array(15);
	   var dice = new Array(15);
	   var d = new Array('ARELSC','TABIYL','EDNSWO','BIOFXR',
	                     'MCDPAE','IHFYEE','KTDNUO','MOQAJB',
	                     'ESLUPT','INVTGE','ZNDVAE','UKGELY',
	                     'OCATAI','ULGWIR','SPHEIN','MSHARO');
	   for (i=0; i < 16; i++) {
	      g[i] = i;
	   }
	   last = 15;
	   do {
	      ranvar = Math.round(Math.random()*last);
	      save = g[last];
	      g[last] =  g[ranvar];
	      dice[last] = g[last];
	      g[ranvar] = save;
	      last = last - 1;
	   } while ( last > 0 );
	// dice[0] = g[0];

	// Get the letter from each dice

	   for (i=0; i < 16; i++) {
	      save = g[i];
	      ranvar = Math.round(Math.random()*5);
	      g[i] = d[save].substring(ranvar,ranvar+1);
		}

	   var result = "";
	   k = -4;
	   for (i=0; i < 4; i++) {
	      k = k + 4;
	      // if (g[k] == 'Q') { g[k] = 'Qu' }
	      // if (g[k+1] == 'Q') { g[k+1] = 'Qu' }
	      // if (g[k+2] == 'Q') { g[k+2] = 'Qu' }
	      // if (g[k+3] == 'Q') { g[k+3] = 'Qu' }
	      result += g[k] + g[k+1] + g[k+2] + g[k+3];
	   }

	   document.write(rrrr + " : " + result);
	   document.write("</br>");

	   var suffix = rrrr == upperLimit - 1 ? "" : "\n";
	   window.result += result + suffix;
	}
}

function saveTextAsFile()
{
    var textToSave = window.result;//document.getElementById("inputTextToSave").value;
    var textToSaveAsBlob = new Blob([textToSave], {type:"text/plain"});
    var textToSaveAsURL = window.URL.createObjectURL(textToSaveAsBlob);
    var fileNameToSaveAs = "boggle.txt";//document.getElementById("inputFileNameToSaveAs").value;
 
    var downloadLink = document.createElement("a");
    downloadLink.download = fileNameToSaveAs;
    downloadLink.innerHTML = "Download File";
    downloadLink.href = textToSaveAsURL;
    document.body.appendChild(downloadLink);
 
    downloadLink.click();
}

window.saveTextAsFile = saveTextAsFile;
boggle()