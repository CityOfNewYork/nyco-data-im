<template>
	  <div>
	    <header class="o-header-app tablet:layout-sidebar tablet:flex">
		      <div class="color-dark-background text-base-white flex flex-col items-start py-2 tablet:p-3">
		        <h1 class="h3 m-0">{{appName}}</h1>
		        <p class="small flex items-end">
		          by<span class="sr-only">NYC Opportunity</span>
		          <svg class="o-header-nyco-secondary__icon mis-1">
		            <use xlink:href="#icon-logo-nyco-secondary"></use>
		          </svg>
		        </p>
		      </div>
		      
		      <div class="color-primary-background justify-between py-2">
		        <div class="mb-0">
		          <h2 class="mb-1">{{user.project}}</h2>
		          <p class="inline mr-3">{{user.firstName + ' ' + user.lastName}}</p>
		          <p class="inline mr-3">{{user.department}}</p>
		          <p class="inline mr-3">Expire on {{user.expireDateFormated}}  </p>
		          <p class="inline mr-3"><router-link to="/web/login">Log out</router-link></p>
		        </div>
		
		        <div class="mb-0 flex" v-if="isMdmUser && isAdminUser ">
		        		<!-- PurgeAll button -->
				       <div v-if="isAblePurge && !showJobProgress">    
				          <button class="btn  mx-1" title="New Action" aria-controls="aria-c-modal-purge" aria-expanded="false" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="purgeConfirm">
				            <svg class="icon-ui tablet:mie-1">
				              <use xlink:href="#feather-trash-2"></use>
				            </svg>
				            <span class="hidden tablet:inline">Purge All</span>
				          </button>
			           </div>
			           <div v-else>
				          <button disabled class="btn  mx-1" title="New Action" aria-controls="aria-c-modal-purge" aria-expanded="false" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="purgeConfirm">
				            <svg class="icon-ui tablet:mie-1">
				              <use xlink:href="#feather-trash-2"></use>
				            </svg>
				            <span class="hidden tablet:inline">Purge All</span>
				          </button>
			           </div>

						<!-- StartMatch button -->				          
				       <div v-if="isAbleMatch && !showJobProgress">    
				          <button class="btn btn-secondary mx-1" title="New Action" aria-controls="aria-c-modal-match" aria-expanded="false" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="startMatchConfirm"  >
				            <svg class="icon-ui tablet:mie-1">
				              <use xlink:href="#feather-plus-square"></use>
				            </svg>
				            <span class="hidden tablet:inline">Start Match</span>
				          </button>
			           </div>
			           <div v-else>
				          <button disabled class="btn btn-secondary mx-1" title="New Action" aria-controls="aria-c-modal-match" aria-expanded="false" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="startMatchConfirm"  >
				            <svg class="icon-ui tablet:mie-1">
				              <use xlink:href="#feather-plus-square"></use>
				            </svg>
				            <span class="hidden tablet:inline">Start Match</span>
				          </button>
			           </div>
		        </div>
		      </div>
	    </header>

		<div v-if="isMdmUser && isAdminUser">
				<!--------------MATCH START---------------------->
				<div v-if="showMatchConfirm">
					<div aria-describedby="aria-db-modal-body" aria-hidden="true" aria-labelledby="aria-lb-modal-header" aria-modal="true" class="fixed z-10 inset-0 overflow-y-auto hidden" id="aria-c-modal-match" role="dialog" style="background-color: rgba(0,0,0,0.25)">
					  <div class="layout-content flex items-center min-h-screen px-2 small:px-4">
					    <div class="bg-base-white mx-auto border-4 p-3 small:p-4 animated fadeInUp">
		
					      <div class="flex mb-2">
					        <div aria-hidden="true" class="flex-shrink-0 pie-2">
					          <svg class="icon-ui icon-4 text-red" tabindex="-1">
					            <use xlink:href="#feather-alert-circle"></use>
					          </svg>
					        </div>
					        <div class="col-span-10 small:col-span-11">
					          <h3 id="aria-lb-modal-header">Confirm Match</h3>
					          <p id="aria-db-modal-body">
					          	You're about to match the following files. Please confirm there is no missing or extra file. Once the match begins, you can not stop the process.
					          </p>
					          
					          <ul class="p-0 list-none" v-for="file in files.items" :key="file.id">
					            <li class="flex items-center mb-1" v-if="file.status === FILE_STATUS_READY">
					              <svg class="icon-ui mie-1" tabindex="-1">
					                <use xlink:href="#feather-file"></use>
					              </svg>
					              <b>{{file.name}}</b>
					            </li>
					          </ul>
					          
					        </div>
					      </div>
					      <div class="small:flex justify-end">
					        <button aria-controls="aria-c-modal-match" aria-expanded="false" class="btn w-full small:w-auto mb-2 small:mb-0 mie-2" data-js="dialog" tabindex="-1">Cancel</button>
					        <button aria-controls="aria-c-modal-match" aria-expanded="false" class="btn btn-secondary mx-1" data-js="dialog" tabindex="-1" v-on:click="startMatch(); enableShowJobProgress('Match');"  style="background-color:#FC5D52" >Begin Matching
					        </button>
					      </div>

					    </div>
					  </div>
					</div>
				</div>	
				<!--------------MATCH END---------------------->

				<!--------------PURGE START---------------------->
				<div v-if="showPurgeConfirm">
					<div aria-describedby="aria-db-modal-body" aria-hidden="true" aria-labelledby="aria-lb-modal-header" aria-modal="true" class="fixed z-10 inset-0 overflow-y-auto hidden" id="aria-c-modal-purge" role="dialog" style="background-color: rgba(0,0,0,0.25)">
					  <div class="layout-content flex items-center min-h-screen px-2 small:px-4">
					    <div class="bg-base-white mx-auto border-4 p-3 small:p-4 animated fadeInUp">
					      <!--
					      <div class="flex justify-end">
					        <button aria-controls="aria-c-modal-purge" aria-expanded="false" class="flex items-center" data-dialog="close" data-js="dialog">
					          <svg class="icon-ui" tabindex="-1">
					            <use xlink:href="#feather-x"></use>
					          </svg>Dismiss
					        </button>
					      </div>
					      -->
					      <div class="flex mb-2">
					        <div aria-hidden="true" class="flex-shrink-0 pie-2">
					          <svg class="icon-ui icon-4 text-red" tabindex="-1">
					            <use xlink:href="#feather-alert-circle"></use>
					          </svg>
					        </div>
					        <div class="col-span-10 small:col-span-11">
					          <h3 id="aria-lb-modal-header">Confirm Purge</h3>
					          <p id="aria-db-modal-body">
					          	You will forever lose the following files:
					          </p>
					          
					          <ul class="p-0 list-none">
					            <li class="flex items-center mb-1">
					              <svg class="icon-ui mie-1" tabindex="-1">
					                <use xlink:href="#feather-file"></use>
					              </svg>
					              <b>Loaded files</b>
					            </li>
					            <li class="flex items-center mb-1">
					              <svg class="icon-ui mie-1" tabindex="-1">
					                <use xlink:href="#feather-file"></use>
					              </svg>
					              <b>Match ready files</b>
					            </li>
					            <li class="flex items-center mb-1">
					              <svg class="icon-ui mie-1" tabindex="-1">
					                <use xlink:href="#feather-file"></use>
					              </svg>
					              <b>Reports</b>
					            </li>
					          </ul>

					        </div>
					      </div>
					      <div class="small:flex justify-end">
					        <button aria-controls="aria-c-modal-purge" aria-expanded="false" class="btn w-full small:w-auto mb-2 small:mb-0 mie-2" data-js="dialog" tabindex="-1">Cancel</button>
					        <button aria-controls="aria-c-modal-purge" aria-expanded="false" class="btn w-full small:w-auto mb-2 small:mb-0 mie-2" data-js="dialog" tabindex="-1" v-on:click="purge(); enableShowJobProgress('Purge');" style="background-color:#FC5D52">Yes, purge all</button>
					      </div>
	
					      
					    </div>
					  </div>
					</div>
				</div>	
				<!--------------PURGE END---------------------->

		</div>

  </div>
 </template>

<script>
import { userService } from '../_services';
import { wait, fileStatusCount, jobStatusCount} from '../_helpers/utils';

export default {
	data:function () {
		return {
			FILE_STATUS_READY     : "Ready to Match",
			uname : JSON.parse(localStorage.getItem('claims')).username,
			appName : JSON.parse(localStorage.getItem('claims')).appName,
			showMatchConfirm:true,
			showPurgeConfirm:true,
			
		}
	},
    computed: {
    	username (){
    		return JSON.parse(localStorage.getItem('claims')).username;
    	},
        user () {
            // return this.$store.state.authentication.user;
            return JSON.parse(localStorage.getItem('claims'));
        },
        users () {
            return this.$store.state.users.all;
        },
        
        isAdminUser(){
			const type = JSON.parse(localStorage.getItem('claims')).type;  
			return type.includes('MDM-ADMIN');      	
        },
        isMdmUser(){
			const serviceType = JSON.parse(localStorage.getItem('claims')).serviceType;  
			return serviceType.includes('MDM');
        },
		
        files () {
            return this.$store.state.files.all;
        },
		fileStatusCount(){
			return fileStatusCount( this.files );
		},
     
        jobs () {
            return this.$store.state.jobs.all;
        },
		jobStatusCount(){
			return jobStatusCount( this.jobs );
		},
        
        isAbleMatch(){
        	// no job in pending/runing status, has file in ready
        	const jobCnt = this.jobStatusCount.pending + this.jobStatusCount.running;
        	const fileReadyCnt = this.fileStatusCount.ready;
        	const fileAnalyzingCnt = this.fileStatusCount.analyzing;
        	
        	//console.log('File count :' + fileCnt);
        	//console.log('Job count :' + jobCnt);
        	return (jobCnt === 0 && fileReadyCnt > 0 && fileAnalyzingCnt == 0) ; 
        },
        
        isAblePurge(){
        	// no job in pending/runing status, has file in ready/analyzing/matching
        	const jobCnt = this.jobStatusCount.pending + this.jobStatusCount.running;
        	const fileCnt = this.fileStatusCount.ready + this.fileStatusCount.analyzing + this.fileStatusCount.matching;
        	const otherFileCnt = this.fileStatusCount.failed + this.fileStatusCount.processed;
        	//console.log('File count :' + fileCnt);
        	//console.log('Job count :' + jobCnt);
        				
        	return (jobCnt === 0 && fileCnt === 0 && otherFileCnt > 0) ;
        },
        showJobProgress(){
		    return localStorage.getItem('showJobProgress') != null;
        }
        
        
    },
    created () {
    	// todo try pass parameter
    	//let uname =JSON.parse(localStorage.getItem('claims')).username;
        //this.$store.dispatch('users/getAll', {username:this.username} );
        //this.$store.dispatch('users/getAll', {username:this.uname} );
        this.reload();
    },
    methods:{
     	reload: function(){
     	    //this.$forceUpdate();
     	    if(this.isMdmUser){
	     		this.$store.dispatch('users/getAll', {username:this.uname} );
	    		this.$store.dispatch('files/getAll');
	    		this.$store.dispatch('jobs/getAll');
	    		this.$store.dispatch('reports/getAll');
    		}
    	},
        startMatchConfirm: function(){
        	//console.log('startMatchConfirm');
        },
        startMatch: function(){
	        userService.createJob('LOAD,MATCH')
	          .then(x => {
				  this.reload();
	          })
	          .catch(err => {
	              this.error = err.response;
	          });
        } ,   

        purgeConfirm: function(){
        	//console.log('purgeConfirm');
			
        },
        purge: function(){
	        userService.createJob('PURGE')
	          .then(x => {
				  this.reload();
	          })
	          .catch(err => {
	              this.error = err.response;
	          });
        },    
        enableShowJobProgress: function(cmd){
			localStorage.setItem('showJobProgress',cmd); 
			localStorage.setItem('loading',true); 
			this.$router.go();
        }
    }
    
};
</script>

