<template>
    <div v-if="isJobUser">
    	<div style="align=center">
			<!-- {{jobs}} -->
			
			<div v-for="job in jobs.items" :key="job.id">
			
				<article class="c-card hover:shadow-up">
				  <header class="c-card__header">
				    <h2 class="c-card__title">{{job.command}}</h2> 
				    <h4> <span v-if="job.command==='LOAD'">PORT : {{job.agncyId}} - {{job.fname}} - </span> RUN:{{job.matchRun}} </h4>
				    <mark class="badge flex items-center">
						<div v-if="job.status === JOB_STATUS_PENDING">
					      <svg aria-hidden="true" class="icon-ui text-green mie-1">
					        <use xlink:href="#feather-check-circle"></use>
					      </svg>{{job.status}}
						</div>
						<div v-else-if="job.status === JOB_STATUS_RUNNING">
							<svg class="spinner icon-4 text-marigold" version="1.1" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
							  <circle class="spinner__path" cx="12" cy="12" fill="none" r="10"></circle>
							</svg>{{job.status}}
						</div>
						<div v-else-if="job.status === JOB_STATUS_COMPLETED">
					      <svg aria-hidden="true" class="icon-ui text-green mie-1">
					        <use xlink:href="#feather-check-circle"></use>
					      </svg>{{job.status}}
						</div>
						<div v-else>
				            <svg aria-hidden="true" class="icon-ui text-red mie-1">
			                	<use xlink:href="#feather-alert-circle"></use>
				            </svg>{{job.status}}
						</div>
						
				    </mark>
				  </header>

				  <footer class="c-card__footer">
				    <dl class="c-card__description-list">
				      <dt>Create Date</dt>
				      <dd>{{job.createdDate | formatDate}} Execution Time : {{job.elapsed | formatTime}}</dd>
				    </dl>
				    <h4>Script</h4>
					<p>{{job.script}}</p>
				    <h4>Stdout</h4>
					<p>{{job.message}}</p>
				    <h4>Stderr</h4>
					<p>{{job.errorMessage}}</p>
				  </footer>
				</article>
				</br>
			</div>		
			
    	</div>
    </div>
    
</template>


<script>
export default {
	data:function () {
		return {
			uname : JSON.parse(localStorage.getItem('claims')).username,
			
			JOB_STATUS_PENDING : "Pending",
			JOB_STATUS_RUNNING : "Running",
			JOB_STATUS_COMPLETED : "Completed",
			JOB_STATUS_FAILED : "Failed",
			
		}
	},
    computed: {
        isJobUser(){
			const serviceType = JSON.parse(localStorage.getItem('claims')).type;  
			return serviceType.includes('JOB');
        },
    	username (){
    		return JSON.parse(localStorage.getItem('claims')).username;
    	},
        jobs () {
            return this.$store.state.jobs.all;
        }
    },
    mounted () {
        //this.$store.dispatch('jobs/getAll' );
    },
    
    methods:{
		reload: function(){
			//this.$store.dispatch('jobs/getAll');
		}
	}
};
</script>

