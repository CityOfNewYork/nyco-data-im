<template>
    <div>
		<!--{{files.items}}-->
    	<div v-if="files.items">
    		<div v-for="file in files.items" :key="file.id">
		        <article class="c-card p-0 mb-3 bg-blue-20t" v-if="file.errorViewedInd != '1'">

		            <header class="c-card__header justify-start mb-1 pt-3 px-4">
			            <h2 class="c-card__title mr-2">{{file.name}}</h2>
			            <mark class="badge bg-blue-20t">
								<div v-if="file.status === FILE_STATUS_COMPLETED">
						            <svg aria-hidden="true" class="icon-ui text-green mie-1">
					                	<use xlink:href="#feather-upload-cloud"></use>
						            </svg>
								</div>
								<div v-else-if="file.status === FILE_STATUS_ANALYZING"">
									<svg class="spinner icon-4 text-marigold" version="1.1" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
									  <circle class="spinner__path" cx="12" cy="12" fill="none" r="10"></circle>
									</svg>
								</div>
								<div v-else-if="file.status === FILE_STATUS_READY"">
						            <svg aria-hidden="true" class="icon-ui text-green mie-1">
					                	<use xlink:href="#feather-check-circle"></use>
						            </svg>
								</div>
								<div v-else-if="file.status === FILE_STATUS_MATCHING"">
									<svg class="spinner icon-4 text-marigold" version="1.1" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
									  <circle class="spinner__path" cx="12" cy="12" fill="none" r="10"></circle>
									</svg>
								</div>
								<div v-else-if="file.status === FILE_STATUS_FAIL">
						            <svg aria-hidden="true" class="icon-ui text-red mie-1">
					                	<use xlink:href="#feather-alert-circle"></use>
						            </svg>
								</div>
								<div v-else>
						            <svg aria-hidden="true" class="icon-ui text-green mie-1">
					                	<use xlink:href="#feather-check-circle"></use>
						            </svg>
								</div>
			              
			              		<div>{{file.status}}</div>
			              		
			              		<div v-if="file.status === FILE_STATUS_ANALYZING">
			              			<span> - Estimated time : {{file.totalEtaTime*1000 | formatTime}}</span>
			              		</div>
			              		
			              		<div v-if="file.hasError">
			              		    <button class="btn btn-secondary sm mx-1" title="New Action" aria-controls="aria-c-modal-error" aria-expanded="false" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="toggle(file.id)" style="background-color:#FC5D52">
									  Details
			              		    </button>
								</div>	
			              		
			            </mark>
			
						<!-- enable remove base on status -->
			            <span class="ml-auto">
							<div v-if="(file.status === FILE_STATUS_FAIL || file.status === FILE_STATUS_READY || !isMdmUser) && isAbleUpload">
					              
					              <div v-if="file.status === FILE_STATUS_FAIL">
						              <svg aria-hidden="true" class="icon-ui mr-1">
						                <use xlink:href="#feather-trash-2"></use>
						              </svg>
					              	  <a v-on:click="remove(file.id);">Remove</a>
					              </div>
					              <div v-else>
					              	<button aria-controls="aria-c-modal-remove" aria-expanded="false" class="btn" data-dialog="open" data-dialog-lock="true" data-js="dialog" v-on:click="removeConfirm();" >Remove</button>

									<!-- Remove confirm start-->
								  	<div v-if="removeConfirmShow">
										<div aria-describedby="aria-db-modal-body" aria-hidden="true" aria-labelledby="aria-lb-modal-header" aria-modal="true" class="fixed z-10 inset-0 overflow-y-auto hidden" id="aria-c-modal-remove" role="dialog" style="background-color: rgba(0,0,0,0.25)">
										  <div class="layout-content flex items-center min-h-screen px-2 small:px-4">
										    <div class="bg-base-white mx-auto border-4 p-3 small:p-4 animated fadeInUp">
										      <!--
										      <div class="flex justify-end">
										        <button aria-controls="aria-c-modal-remove" aria-expanded="false" class="flex items-center" data-dialog="close" data-js="dialog">
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
										          <h3 id="aria-lb-modal-header">Confirm Remove</h3>
										          <p id="aria-db-modal-body">The file will be removed from the Data Source File list.</p>
										          <ul class="p-0 list-none">
										            <li class="flex items-center mb-1">
										              <svg class="icon-ui mie-1" tabindex="-1">
										                <use xlink:href="#feather-file"></use>
										              </svg>
										              <b>{{file.name}}</b>
										            </li>
										          </ul>
										        </div>
										      </div>
										      <div class="small:flex justify-end">
										        <button aria-controls="aria-c-modal-remove" aria-expanded="false" class="btn w-full small:w-auto mb-2 small:mb-0 mie-2" data-js="dialog" tabindex="-1">Cancel</button>
										        <button class="btn w-full small:w-auto btn-secondary" aria-controls="aria-c-modal-remove" tabindex="-1" v-on:click="remove(file.id);" style="background-color:#FC5D52">Yes, remove it</button>
										      </div>
										    </div>
										  </div>
										</div>
					              	</div>
									<!-- Remove confirm start-->
											  
								  </div>
							</div>
			            </span>
			            
		            </header>
		          
		            <div class="c-card__body mb-3">
			            <div class="grid grid-cols-5 gap-3 px-4">
			              <div>{{file.department}}</div>
			              <div>{{file.updatedDate | formatDate}}</div>
			              <div>{{file.lines}} Records </div>
			              <div class="flex" v-if="file.status === FILE_STATUS_COMPLETED && file.mdmMatchRecords != null">
			             	 Loaded : {{file.mdmMatchRecords}} <br>Rejected : {{file.mdmRejectRecords}} <br>Duplicated : {{file.mdmDupedRecords}} 
			              </div>
			              <div class="text-right">
			              		<div v-if="file.hasWarn">
									<div v-if="file.id == idShow">
						                <a class="text-blue" v-on:click="toggle(file.id)" >Hide Suggestions
						                  <svg aria-hidden="true" class="icon-ui align-middle" >
											<use xlink:href="#feather-chevron-up" ></use>
						                  </svg>
						                </a>
									</div>
									<div v-else>
						                <a class="text-blue" v-on:click="toggle(file.id)" >View Suggestions
						                  <svg aria-hidden="true" class="icon-ui align-middle" >
											<use xlink:href="#feather-chevron-down" ></use>
						                  </svg>
						                </a>
									</div>
								</div>
			              </div>
			            </div>
		             </div>

			         <div v-if="file.id == idShow">
						  <!-- Error -->		          
				          <footer class="bg-base-white p-4" v-if="file.hasError && file.id == idShow" >
								<div aria-describedby="aria-db-modal-body" aria-hidden="true" aria-labelledby="aria-lb-modal-header" aria-modal="true" class="fixed z-10 inset-0 overflow-y-auto hidden" id="aria-c-modal-error" role="dialog" style="background-color: rgba(0,0,0,0.25)">
								  <div class="layout-content flex items-center min-h-screen px-2 small:px-4">
								    <div class="bg-base-white mx-auto border-4 p-3 small:p-4 animated fadeInUp">
								    
								      <div class="flex mb-2">
								      
									        <div aria-hidden="true" class="flex-shrink-0 pie-2">
									          <svg class="icon-ui icon-4 text-red" tabindex="-1">
									            <use xlink:href="#feather-alert-circle"></use>
									          </svg>
									        </div>
									        
									        <div class="col-span-10 small:col-span-11">
									          <h3 id="aria-lb-modal-header">Fix Errors in the File </h3>
									          <p id="aria-db-modal-body">
									          	Failed to upload the file to the server. We are sending the following error report to your email. Please fix the errors and upload it again.
									          </p>
											  <div>

													<h4>
														<svg width="18" height="22" viewBox="0 0 18 22" fill="none" xmlns="http://www.w3.org/2000/svg">
														<path d="M10 1H3C2.46957 1 1.96086 1.21071 1.58579 1.58579C1.21071 1.96086 1 2.46957 1 3V19C1 19.5304 1.21071 20.0391 1.58579 20.4142C1.96086 20.7893 2.46957 21 3 21H15C15.5304 21 16.0391 20.7893 16.4142 20.4142C16.7893 20.0391 17 19.5304 17 19V8L10 1Z" stroke="black" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
														</svg>
														{{file.name}}
													</h4>
											  
											  </div>
										      <div v-for="err in file.errors">
										      	<h4 class="text-red">{{err.id}}</h4> {{err.msg}}
										      </div>
									        </div>

								      </div>
								      
								      <div class="small:flex justify-end">
								        <button aria-controls="aria-c-modal-error" aria-expanded="false" class="btn w-full small:w-auto mb-2 small:mb-0 mie-2" data-js="dialog" tabindex="-1" v-on:click="viewed(file.id);">Close</button>
								      </div>
								      
								    </div>
								  </div>
								</div>
				          </footer>


						  <!-- Suggestion -->		          
				          <footer class="bg-base-white p-4" v-if="file.hasWarn">
					            <p class="font-bold">Your file passed our scan, but here are some ways that it could be better.</p>
					            <dl class="description-list suggestions" v-for="sugg in file.suggestions" >
					              <dt>{{sugg.id}}</dt>
					              <dd>{{sugg.msg}}</dd>
					            </dl>
				          </footer>
			         </div>
			         
			         
		        </article>

             </div>
        </div> 
        
 
    </div>
</template>

<script>
import { userService } from '../_services';
import { jobStatusCount,filesGroupAnalyzeMessage } from '../_helpers/utils';

export default {
	data: function(){
		return {
			idShow                : -1,
			FILE_STATUS_FAIL      : "Failed",
			FILE_STATUS_ANALYZING : "Analyzing",
			FILE_STATUS_READY     : "Ready to Match",
			FILE_STATUS_MATCHING  : "Matching",
			FILE_STATUS_COMPLETED : "Processed",
			error         		  : null,
			removeConfirmShow     : false
		}
	},

    computed: {
        files () {
        	return filesGroupAnalyzeMessage(this.$store.state.files.all);
        },
        
	    jobs () {
	        return this.$store.state.jobs.all;
	    },
	    
		jobStatusCount(){
			return jobStatusCount( this.jobs );
		},

		isAbleUpload(){
        	// no job in pending/runing status
        	const jobCnt = this.jobStatusCount.pending + this.jobStatusCount.running;
        	//console.log('-----Job count :' + jobCnt);
        	return (jobCnt === 0 ) ;
		},
		
        isMdmUser(){
			const serviceType = JSON.parse(localStorage.getItem('claims')).serviceType;  
			return serviceType.includes('MDM');
        }
    },
    mounted () {
    	// created also works!
        //this.$store.dispatch('files/getAll');
        //this.reload();
    },
    
    methods:{
    	reload: function(){
    		this.$store.dispatch('files/getAll');
    		this.$store.dispatch('jobs/getAll');
    	},
        
        toggle: function(id){
        	if(id === this.idShow){
        		this.idShow = -1;
        		this.$router.go();
        	}else{
        		this.idShow = id;
        	}
        },
        
        removeConfirm: function(){
			this.removeConfirmShow = true;      
        },
        
        remove: function(id){
        	//console.log('remove');
        	//console.log(id);
        	
        	this.removeConfirmShow = false;
        	
	        userService.remove(id)
	          .then(x => {
				  this.reload();
	          })
	          .catch(err => {
	              this.error = err.response;
	          });
        	
        },
        
        viewed: function(id){
        	this.removeConfirmShow = false;
        	
	        userService.viewed(id)
	          .then(x => {
				  this.reload();
	          })
	          .catch(err => {
	              this.error = err.response;
	          });
        }
        
    }
    
};
</script>
