function ensureframes() {
	if (parent.location.href == self.location.href) {
		self.location.href = "index_" + location.pathname.substring(location.pathname.lastIndexOf('/')+1) ;
		
	} 
}

function ensureMainFrames() {
}