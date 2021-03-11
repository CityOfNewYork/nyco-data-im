// browser no activity timeout
var myTimeout = null;
function setMyTimeout(timeout) {
	myTimeout = setTimeout(function(){
		location.replace("/web/index.html#/web/login");
		}, timeout*1000);
}
function clearMyTimeout() {
	if(myTimeout != null){
		 clearTimeout(myTimeout);
	}
}

export function authHeader() {
    // return authorization header with jwt token
    let user = JSON.parse(localStorage.getItem('user'));

    if (user && user.jwt) {
    	// check time out
        const timeout = JSON.parse(localStorage.getItem('claims')).timeout; 

        if(timeout != null && timeout > 0){

        	// clear last one
        	clearMyTimeout();

        	// only run once
        	setMyTimeout(timeout);
        }
        
        return { 'Authorization': 'Bearer ' + user.jwt };
    } else {
        return {};
    }
}



