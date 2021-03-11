import config from 'config';
import { authHeader } from '../_helpers';
import { urlBase64Decode,getClaimsFromToken,serviceType,atg } from '../_helpers/utils';

export const userService = {
    login,
    logout,
    getUsers,
    getFiles,
    getJobs,
    getReports,
    upload,
    remove,
    createJob,
    viewed,
    getDict
};

function login(username, password) {
	const serviceType = localStorage.getItem('serviceType');
	
    const requestOptions = {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, serviceType })
    };

    return fetch(`${config.apiUrl}/authenticate`, requestOptions)
        .then(handleResponse)
        .then(user => {
            // login successful if there's a jwt token in the response
            if (user.jwt) {
                // store user details and jwt token in local storage to keep user logged in between page refreshes
                localStorage.setItem('user', JSON.stringify(user));
                localStorage.setItem('claims', JSON.stringify(getClaimsFromToken(user.jwt)));
            }
            return user;
        } );
}

function logout() {
	if( localStorage.getItem('user') != null && localStorage.getItem('claims') ){
	    const requestOptions = {
	            method: 'GET',
	            headers: authHeader()
	        };
	    
	    let url = `${config.apiUrl}/logout`;
	    return fetch(url, requestOptions).then(()=> {
	        // remove user from local storage to log user out
	        localStorage.removeItem('user');
	        localStorage.removeItem('claims');
	    });
	}else{
        // remove user from local storage to log user out
        localStorage.removeItem('user');
        localStorage.removeItem('claims');
	}
}

function getUsers() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    let url = `${config.apiUrl}/users`;
    return fetch(url, requestOptions).then(handleResponse);
}

function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);
        if (!response.ok) {
            if (response.status === 401 || Date.now() > JSON.parse(localStorage.getItem('claims')).exp * 1000 ) {
                // auto logout if 401 response returned from api, or expired
                logout();
                location.reload(true);
            }

            const error = (data && data.message) || response.statusText;
            return Promise.reject(error);
        }

        return data;
    });
}


function upload(formData) {

	let url = `${config.apiUrl}`;
	if(serviceType() === 'MDM'){
		url += '/mdmUpload';
	}else {
		url += '/awsUpload';
	}
	
    const requestOptions = {
            method: 'POST',
            headers: authHeader(),
            body: formData
        };

    return fetch(url, requestOptions)
        .then(handleResponse)
        .then(data => {
        	//console.log("data " + JSON.stringify(data));
        });	
}
	
function getFiles() {
	//console.log('getFiles called');
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    let url = `${config.apiUrl}/files/` + serviceType() + '/' + atg();
	if(serviceType() === 'AWS'){
		url = `${config.apiUrl}/awsFiles/`;
	}
    
    return fetch(url, requestOptions).then(handleResponse);
}

function getJobs() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    const url = `${config.apiUrl}/jobs/` + atg();
    //console.log(url);
    
    return fetch(url,  requestOptions ).then(handleResponse);
}

function getReports() {
    const requestOptions = {
        method: 'GET',
        headers: authHeader()
    };

    const url = `${config.apiUrl}/axwayreports/` + atg();
    return fetch(url,  requestOptions ).then(handleResponse);
}

function remove(id) {
	var formData = new FormData();
	formData.append("id", id);
    
	let url = `${config.apiUrl}` + '/delFile/' ;
	
    const requestOptions = {
            method: 'POST',
            headers: authHeader(),
            body: formData
        };

    return fetch(url, requestOptions)
        .then(handleResponse)
        .then(data => {
        	//console.log("data " + JSON.stringify(data));
        });	
}

function createJob(cmd) {
	var formData = new FormData();
	formData.append("cmd", cmd);
    
	let url = `${config.apiUrl}` + '/addJob/' ;
	
    const requestOptions = {
            method: 'POST',
            headers: authHeader(),
            body: formData
        };

    return fetch(url, requestOptions)
        .then(handleResponse)
        .then(data => {
        	//console.log("data " + JSON.stringify(data));
        });	
}

function viewed(id) {
	var formData = new FormData();
	formData.append("id", id);
    
	let url = `${config.apiUrl}` + '/viewed/' ;
	
    const requestOptions = {
            method: 'POST',
            headers: authHeader(),
            body: formData
        };

    return fetch(url, requestOptions)
        .then(handleResponse)
        .then(data => {
        	//console.log("data " + JSON.stringify(data));
        });	
}

function getDict(){
    const requestOptions = {
            method: 'GET',
            headers: authHeader()
        };
    
	let url = `${config.dictUrl}`;
	return fetch(url,requestOptions).then(handleResponse);
}


// count file status
const FILE_STATUS_FAIL = "Failed";
const FILE_STATUS_ANALYZING = "Analyzing";
const FILE_STATUS_READY = "Ready to Match";
const FILE_STATUS_MATCHING = "Matching";
const FILE_STATUS_COMPLETED = "Processed";

