// utils to delay promise
function wait(ms) {
    return (x) => {
        return new Promise(resolve => setTimeout(() => resolve(x), ms));
    };
}

function urlBase64Decode(str) {
    var output = str.replace('-', '+').replace('_', '/');
    switch (output.length % 4) {
        case 0:
            break;
        case 2:
            output += '==';
            break;
        case 3:
            output += '=';
            break;
        default:
            throw 'Illegal base64url string!';
    }
    return window.atob(output);
}

function getClaimsFromToken(token) {
    var user = {};
    if (typeof token !== 'undefined') {
        var encoded = token.split('.')[1];
        user = JSON.parse(urlBase64Decode(encoded));
    }
    return user;
}

var serviceType = () => JSON.parse(localStorage.getItem('claims')).serviceType;
var atg = () => JSON.parse(localStorage.getItem('claims')).atg;
var axwayurl = () => JSON.parse(localStorage.getItem('claims')).axwayurl;
var outputAxwayId = () => JSON.parse(localStorage.getItem('claims')).outputAxwayId;

var analyingRecordsPerSecond = () => JSON.parse(localStorage.getItem('claims')).analyingRecordsPerSecond;
var matchingRecordsPerSecond = () => JSON.parse(localStorage.getItem('claims')).matchingRecordsPerSecond;

function uploadFilesizelimit(){
	var limit = null;
	if(serviceType() === 'MDM'){
		limit = JSON.parse(localStorage.getItem('claims')).mdmSizeLimit;
	}else {
		limit = JSON.parse(localStorage.getItem('claims')).awsSizeLimit;
	}	
	
	return limit;
}

//count file status
const FILE_STATUS_FAIL = "Failed";
const FILE_STATUS_ANALYZING = "Analyzing";
const FILE_STATUS_READY = "Ready to Match";
const FILE_STATUS_MATCHING = "Matching";
const FILE_STATUS_COMPLETED = "Processed";

function fileStatusCount (files){
	//console.log('service fileStatusCount called');

	var cnt = {
	    failed     : 0,
	    analyzing   : 0,
		ready      : 0,
		matching   : 0,
		processed  : 0
	}
	for(var f in files.items){
		const sts =  files.items[f].status;
		if(sts === FILE_STATUS_FAIL ){
			cnt.failed = cnt.failed + 1;
		}
		if(sts === FILE_STATUS_ANALYZING ){
			cnt.analyzing = cnt.analyzing + 1;
		}
		if(sts === FILE_STATUS_READY ){
			cnt.ready = cnt.ready + 1;
		}
		
		if(sts === FILE_STATUS_MATCHING ){
			cnt.matching = cnt.matching + 1;
		}
		
		if(sts === FILE_STATUS_COMPLETED ){
			cnt.processed = cnt.processed + 1;
		}
	
	}
	return cnt; 		
}

//job status count
const JOB_STATUS_PENDING = "Pending";
const JOB_STATUS_RUNNING = "Running";
const JOB_STATUS_COMPLETED = "Completed";
const JOB_STATUS_FAILED = "Failed";
function jobStatusCount (jobs){

	var cnt = {
		pending    : 0,
		running    : 0,
		completed  : 0,
	    failed     : 0
	}
	
	const maxMatchRun = getMaxMatchRun(jobs);
	
	for(var j in jobs.items){
		const job = jobs.items[j];
		
		//console.log(maxMatchRun);
		//console.log(job.matchRun);
		if(job.matchRun === maxMatchRun){
			const sts =  job.status;
			if(sts === JOB_STATUS_PENDING ){
				cnt.pending = cnt.pending + 1;
			}
			if(sts === JOB_STATUS_RUNNING ){
				cnt.running = cnt.running + 1;
			}
			if(sts === JOB_STATUS_COMPLETED ){
				cnt.completed = cnt.completed + 1;
			}
			
			if(sts === JOB_STATUS_FAILED ){
				cnt.failed = cnt.failed + 1;
			}
		}
		//console.log('in loop called');
		//console.log(sts);			
	}
	return cnt; 		
}

function filesGroupAnalyzeMessage(origAllFiles){
	
	// Copy data from store
	// let allFiles = JSON.parse(JSON.stringify(this.$store.state.files.all));
	let allFiles = origAllFiles;

	let allItems = [];
	for(var f in allFiles.items){
		var newFile = allFiles.items[f];
		let errorMessage = newFile.errorMessage;
		
		// construct error message array
		if(errorMessage != null && errorMessage.length > 0 ){
			const ERROR = 'ERROR:';
			const WARNING = 'WARN:';
			
			let prefix = '';
			let origErrorMessage = errorMessage;
			if(errorMessage.startsWith(ERROR)){
				prefix = ERROR;
				origErrorMessage = errorMessage.replace(ERROR, '');
			}
			if(errorMessage.startsWith(WARNING)){
				prefix = WARNING;
				origErrorMessage = errorMessage.replace(WARNING, '');
			}

			// split error message
			var messageList = [];
			var errorlist = origErrorMessage.split('\n');
			for (var i in errorlist ){
				var num = parseInt(i)+1;
				var key = ((prefix === ERROR)?'Error ': 'Suggestion ') + num ;
				var msg = errorlist[i].trim();
				
				if(msg.length > 0){
					var newObj = {
						id  : key,
						msg : msg
					}
					messageList.push(newObj);
					//console.log(newObj);
				}
			}
			//console.log(messageList);

			newFile.hasError = (prefix === ERROR);
			newFile.hasWarn = (prefix === WARNING);

			if(newFile.hasError){
				newFile.errors = messageList;
			}else if(newFile.hasWarn){
				newFile.suggestions = messageList;
			}					
			
			//console.log(newFile);
			allItems.push(newFile);
			
			//allFiles.items[f].error = origErrorMessage;
		}else{
			
			// check estimation time for analyzing
			// console.log('File status : ' + newFile.status);
			if( newFile.status === FILE_STATUS_ANALYZING){
				var eta = newFile.lines/analyingRecordsPerSecond();
				
				//console.log('Estimated analyzing time in seconds : ' + eta);
				eta = Math.max(eta,10);
				//console.log('Estimated analyzing time in seconds : ' + eta);
				
				newFile.totalEtaTime = eta;
			}
			
			newFile.hasError = false;
			newFile.hasWarn = false;
			allItems.push(newFile);
		}      		
	}
	return {items : allItems} ;	
}

function getRunList(origAllReports){
	//console.log('getRunList called.');
	let runs = [];
	for (var r in origAllReports.items){
		const rpt = origAllReports.items[r];
		if(!runs.includes(rpt.run)){
			runs.push(rpt.run);
		}
	}
	return runs.sort();
}

function reportGroupByRun(origAllReports){
	//console.log('reportGroupByRun called.');

	const runs = getRunList(origAllReports);
	//console.log(runs);
	
	let allReports = [];
	for(var r in runs){
		//console.log('run = ' + runs[r]);
		const runid = runs[r];
		var department = null;
		var createdDate = null;
		
		let runItems = [];
		for(var i in origAllReports.items){
			const itm = origAllReports.items[i];
			
			if(runid === itm.run){
				runItems.push(itm);
			}
			
			// take one for title
			department = itm.department;
			createdDate = itm.createdDate;
			
		}
		
		allReports.push(
			{
				runid       : runid,
				department  : department,
				createdDate : createdDate,
				runItems    : runItems
			}
		);
	}
	
	return (
		{
			items : allReports
		}	
	);
}

// get Latest match run
function getMaxMatchRun(allJobs){
	var max = 0;
	for(var j in allJobs.items){
		max = Math.max(max, allJobs.items[j].matchRun);
	}
	//console.log(allJobs);
	return max;
}

//get Latest match run
function getMaxMatchRun(allJobs){
	var max = 0;
	for(var j in allJobs.items){
		max = Math.max(max, allJobs.items[j].matchRun);
	}
	//console.log(allJobs);
	return max;
}


//get job start time
function getJobStartTime(allFiles, run){
	//console.log('getJobStartTime called');

	const start_now = Date.now();
	var start = start_now;
	
	//console.log(start);
	
	for(var f in allFiles.items){
		const file = allFiles.items[f];
		if(file.matchRun === run){
			//console.log('status ' + file.status);
			
			// matching not start yet
			if(FILE_STATUS_READY == file.status){
				start = Math.min(start, Date.parse(file.updatedDate));
			}
			// matching started
			else if(FILE_STATUS_MATCHING == file.status){
				start = Math.min(start, Date.parse(file.updatedDate));
			}
			// matching completed
			else if(FILE_STATUS_COMPLETED == file.status){
				//console.log('FILE_STATUS_COMPLETED ');
				//console.log(file.updatedDate);
				//console.log(Date.parse(file.updatedDate));
				//console.log(file.elapsed);
				start = Math.min(start, Date.parse(file.updatedDate) - file.elapsed );
			}
		}
	}
	
	// check if started
	if (start_now === start){
		// not started
		start = null;
	}

	return start;
}

// get total records
function getTotalRecords(allFiles, run){
	var total = 0;
	for(var f in allFiles.items){
		const file = allFiles.items[f];
		if(file.matchRun === run){
			total += file.lines;
		}
	}
	return total;
}

function getTotalEtaTime(records){
	// set at least 10 seconds
	var eta = records/matchingRecordsPerSecond();
	return Math.max(eta,10);
}

function isMatchingStarted (allFiles, run){
	var start = false;
	for(var f in allFiles.items){
		const file = allFiles.items[f];
		if(file.matchRun === run && file.status != FILE_STATUS_READY){
			start = true;
			break
		}
	}
	return start;
}

function isMatchingCompleted (allFiles, run){
	var end = true;
	for(var f in allFiles.items){
		const file = allFiles.items[f];
		if(file.matchRun === run && file.status != FILE_STATUS_COMPLETED){
			end = false;
			break
		}
	}
	return end;
}

//get job created time
function getJobCreatedTime(allJobs, run){
	const start_now = Date.now();
	var created = start_now;
	
	for(var j in allJobs.items){
		const job = allJobs.items[j];
		if(job.matchRun === run){
			created = Math.min(created, Date.parse(job.createdDate));
		}
	}
	return created;
}

// to fix undefined property error
function convertDict(items){
	const dict = items;
	var jsonDict = {};
	for(var k in dict){
		jsonDict[k] = dict[k];
	}
    return jsonDict;	
}

export { 
	wait,
	urlBase64Decode,
	getClaimsFromToken,
	serviceType,
	atg,
	axwayurl,
	outputAxwayId,
	uploadFilesizelimit,
	fileStatusCount,
	jobStatusCount,
	filesGroupAnalyzeMessage,
	reportGroupByRun,
	getMaxMatchRun,
	getJobStartTime,
	analyingRecordsPerSecond,
	matchingRecordsPerSecond,
	getTotalRecords,
	getTotalEtaTime,
	isMatchingStarted,
	isMatchingCompleted,
	getJobCreatedTime,
	convertDict
	
}

/////////////////////////////////////////