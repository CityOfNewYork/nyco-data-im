<template>
    <div>
    	<div style="align=center">
      		<div class="matchProgress text-center mx-auto mb-8">
				<circle-counter 
			                    size="10rem" 
			                    :dashCount="myDashCount"
								:activeCount="myActiveCount"
			                    :text="myActiveCountText">
			    </circle-counter>

				<div v-if="isJobRunning || loading || percent == 100">
					
			        <h2 v-if="loading">{{showJobProgress}} in Waiting Queue</h2>
			        <h2 v-else-if="!loading && percent == 0" >{{showJobProgress}} in Waiting Queue</h2>
			        <h2 v-else-if="!loading && percent < 100">{{showJobProgress}} in Progress</h2>
			        <h2 v-else-if="percent==100" >{{showJobProgress}} is Complete!</h2
					<h2 v-else></h2>

					<!-- job not loaded yet-->					
					<div v-if="isJobRunning">
						<h4 v-if="maxMatchRun != 0">RUN : {{maxMatchRun}} </h4>
						<p>Start time : {{jobCreatedTime | formatDate}} </p>
				        <p>Total Estimated Run Time: <strong>{{totalEtaTime*1000 | formatTime}}</strong></p>
					</div>
					<div v-else-if="percent==100">
						<h4 v-if="maxMatchRun != 0">RUN : {{maxMatchRun}} </h4>
						<p>Start time : {{jobCreatedTime | formatDate}} </p>
					</div>
					<div v-else>
						<p>Creating Jobs</p>
					</div>
					<!--
				        <p>Remaining Execution Time: <strong>{{remainTime | formatTime}}</strong> </p>
				        <p>Total Estimated Run Time: <strong>{{totalEtaTime*1000 | formatTime}}</strong></p>
				        <p>Total Records: <strong>{{totalRecords}}</strong></p>
					-->
					<br>
					
					<!-- display from jobs -->
			        <ul class="p-0 list-none">
					  <div v-for="job in jobs.items" :key="job.id">
						  <div v-if="!loading && job.matchRun === maxMatchRun && ((job.command === 'PURGE' && showJobProgress === 'Purge')||(job.command != 'PURGE' && showJobProgress != 'Purge')) ">

								<div v-if="job.status === JOB_STATUS_COMPLETED">
							          <li class="flex items-center mb-1">
							            <svg class="icon-ui mie-1" tabindex="-1">
							              <use xlink:href="#feather-upload-cloud"></use>
							            </svg>
							            <b>
							            	{{job.command === 'LOAD'?job.fname: ((job.command === 'MATCH')?'Generating Reports':'Purging Data')}}
							            </b>
							            <svg class="icon-ui text-green mie-1 ml-auto">
							              <use xlink:href="#feather-check-circle"></use>
							            </svg>
							          </li>
								</div>
								<div v-else-if="job.status === JOB_STATUS_PENDING">
							          <li class="flex items-center mb-1">
							            <svg class="icon-ui mie-1" tabindex="-1">
							              <use xlink:href="#feather-file"></use>
							            </svg>
							            <b>
							            	{{job.command === 'LOAD'?job.fname: ((job.command === 'MATCH')?'Generating Reports':'Purging Data')}}
							            </b>
							            <svg class="icon-ui text-green mie-1 ml-auto">
							              <use xlink:href="#feather-circle"></use>
							            </svg>
							          </li>
								</div>	
								<div v-else-if="job.status === JOB_STATUS_RUNNING">
							          <li class="flex items-center mb-1">
							            <svg class="icon-ui mie-1" tabindex="-1">
							              <use xlink:href="#feather-file"></use>
							            </svg>
							            <b>
							            	{{job.command === 'LOAD'?job.fname: ((job.command === 'MATCH')?'Generating Reports':'Purging Data')}}
							            </b>

										<svg class="spinner icon-4 text-marigold" version="1.1" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
										  <circle class="spinner__path" cx="12" cy="12" fill="none" r="10"></circle>
										</svg>
							            
							          </li>
								</div>
								<div v-else>
							          <li class="flex items-center mb-1">
							            <svg class="icon-ui mie-1" tabindex="-1">
							              <use xlink:href="#feather-file"></use>
							            </svg>
							            <b>
							            	{{job.command === 'LOAD'?job.fname: ((job.command === 'MATCH')?'Generating Reports':'Purging Data')}}
							            </b>
							            <svg class="icon-ui text-red mie-1 ml-auto">
							              <use xlink:href="#feather-alert-circle"></use>
							            </svg>
							            <div :title="job.errorMessage">{{job.status}}</div>

							          </li>
								</div>

						  </div>
					  </div>
			        </ul>
				</div>
				
				<div v-if="percent != 100 || loading">
	        		<p class="mt-5 p-3 border-t border-b">You can close the window or log out while the match is being processed. We will send an email to {{email}} to let you know when the match is complete.</p>
				</div>				
				
				<div v-if="percent == 100 && !loading">
					<div v-if="showJobProgress==='Match'">
			        	<p class="mt-5 p-3 ">Match reports are ready for download. Please find them on SFTP.</p>
					</div>
		        	<button class="btn btn-primary" v-on:click="closeShowJobProgress();">Close</button>
				</div>
      		</div>
    	</div>
    </div>
</template>


<script>

import { wait, getMaxMatchRun,getJobStartTime,getTotalRecords,getTotalEtaTime,	isMatchingStarted,
	isMatchingCompleted,jobStatusCount,getJobCreatedTime } from '../_helpers/utils';
import circleCounter from 'vue-circle-counter';

export default {
	components: {circleCounter},
	data:function () {
		return {
			uname : JSON.parse(localStorage.getItem('claims')).username,
			FILE_STATUS_FAIL      : "Failed",
			FILE_STATUS_ANALYZING : "Analyzing",
			FILE_STATUS_READY     : "Ready to Match",
			FILE_STATUS_MATCHING  : "Matching",
			FILE_STATUS_COMPLETED : "Processed",

			JOB_STATUS_PENDING : "Pending",
			JOB_STATUS_RUNNING : "Running",
			JOB_STATUS_COMPLETED : "Completed",
			JOB_STATUS_FAILED : "Failed",
						
			myDashCount : 60,
			myActiveCount : 0,
			loading : localStorage.getItem('loading') != null,
			
			timer: ''
		}
	},
    computed: {

    	username (){
    		return JSON.parse(localStorage.getItem('claims')).username;
    	},
    	email (){
    		return JSON.parse(localStorage.getItem('claims')).email;
    	},
        jobs () {
            return this.$store.state.jobs.all;
        },
		jobStatusCount(){
			return jobStatusCount( this.jobs );
		},
    	jobCreatedTime(){
    		return getJobCreatedTime(this.jobs,this.maxMatchRun);
    	},		
		jobTotalCount(){
			return this.jobStatusCount.pending + this.jobStatusCount.running + this.jobStatusCount.completed + this.jobStatusCount.failed;
		},
		jobCompletedCount(){
			return this.jobStatusCount.completed + this.jobStatusCount.failed;
		},

		isJobRunning(){
			return this.jobTotalCount > this.jobCompletedCount;
		},
    	maxMatchRun (){
    		return getMaxMatchRun(this.jobs);
    	},
        
        files () {
            return this.$store.state.files.all;
        },
    	jobStartTime(){
    		return getJobStartTime(this.files, this.maxMatchRun);
    	},
    	totalRecords (){
    		return getTotalRecords(this.files, this.maxMatchRun);
    	},
    	totalEtaTime(){
    		return Math.max(getTotalEtaTime(this.totalRecords),60);
    	},
    	matchingStarted(){
  			return isMatchingStarted(this.files, this.maxMatchRun);
    	},
    	remainTime(){
    		return this.totalEtaTime*1000 * (100 - this.percent)/100  
    	},
    	percent(){
    		if(this.loading){
    			localStorage.setItem('percent', 0);
    		}else if(this.jobTotalCount > 0) {
    			const per = Math.round( this.jobCompletedCount*100 / this.jobTotalCount );
    			localStorage.setItem('percent', per);
    		}else if(this.jobCompletedCount == 0 && this.showJobProgress == 'Purge'){
    			localStorage.setItem('percent', 100);
    		}
    		return localStorage.getItem('percent');
    	},
		myActiveCountText(){
			if(this.percent > 0){
				return this.percent + '%';
			}else{
				return '';
			}
		},
		showJobProgress(){
			return localStorage.getItem('showJobProgress');		
		}
    },
    created () {
        this.timer = setInterval(this.fetchEventsList, 1000);
        if(this.loading){
        	localStorage.setItem('percent', 0);
        }
    },
    methods:{
    	reload : function(){
    		this.$store.dispatch('files/getAll');
    		this.$store.dispatch('jobs/getAll');
    		//this.$router.go();
    	},

		fetchEventsList () {
			if(this.isJobRunning || this.loading){
				this.myActiveCount = this.myActiveCount + 1;
				// reload when reach full circle
				if(this.myActiveCount >= 60){
					this.myActiveCount = 0;
					this.reload();
					localStorage.removeItem('loading');
					this.loading = false;
				}
			}else{
				this.myActiveCount = 60;
				this.cancelAutoUpdate();
			}
        },
        
        cancelAutoUpdate () { 
        	clearInterval(this.timer)
         },
         
		closeShowJobProgress(){
			localStorage.removeItem('showJobProgress');
			this.$router.go();
		}
         
	},
	
    beforeDestroy () {
       clearInterval(this.timer)
    }
};
</script>
