const obfuscationCharacters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
const obfuscationCharactersLength = obfuscationCharacters.length;

function doSignObfuscation() {
	for (const el of document.getElementsByClassName("obfuscated")) {
		if (!el.checkVisibility()) continue;
		const length = el.innerHTML.length;
		if (length === 0) continue;
		let randomText = "";
		for (let i = 0; i < length; i++) {
			randomText += obfuscationCharacters.charAt(Math.floor(Math.random() * obfuscationCharactersLength));
		}
		el.innerHTML = randomText;
	}
}

setInterval(doSignObfuscation, 1000);
