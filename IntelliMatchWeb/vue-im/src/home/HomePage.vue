<template>
    <div>
		<IMHeader/>
    	<div class="layout-wrap min-h-screen w-full shadow-up-2 p-6 mx-auto">
	      	<div class="bg-base-white pt-7 my-7 mx-auto">
				<div v-if="showJobProgress && isMdmUser ">
					<MatchProgressCard/>
				</div>
				<div v-else>
		      		<FileUpload/>
		      		<IMFileCards/>
				</div>

		      		<!--
		      		<IMUserCards/>
					<IMJobCards/>
					-->
					
				<div v-if="isMdmUser">
					<IMJobCards/>
					<IMReportCards/>
				</div>
					
	      	</div>
      	</div>
      	<IMFooter/>
    </div>
</template>

<script>
import IMHeader from './IMHeader.vue'
import FileUpload from './FileUpload.vue'
import IMUserCards from './IMUserCards.vue'
import IMFileCards from './IMFileCards.vue'
import IMJobCards from './IMJobCards.vue'
import IMReportCards from './IMReportCards.vue'
import MatchProgressCard from './MatchProgressCard.vue'
import IMFooter from '../app/IMFooter.vue'

import { jobStatusCount,isMatchingCompleted,getMaxMatchRun } from '../_helpers/utils';
import circleCounter from 'vue-circle-counter';
export default {
    components: {
	    IMHeader,
	    FileUpload,
	    IMUserCards,
	    IMFileCards,
	    IMJobCards,
	    IMReportCards,
	    MatchProgressCard,
	    circleCounter,
	    IMFooter
	},
	data:function () {
		return {
			myDashCount : 60,
			myActiveCount : 60,
			myActiveCountText : '100%',
		}
	},
	computed: {
        jobs () {
            return this.$store.state.jobs.all;
        },
		jobStatusCount(){
			return jobStatusCount( this.jobs );
		},
		isJobRunning(){
			const jobCnt = this.jobStatusCount.pending + this.jobStatusCount.running;
			return jobCnt > 0;
		},
		matchingCompleted(){
  			return isMatchingCompleted(this.$store.state.files.all, getMaxMatchRun(this.jobs));
    	},
        isMdmUser(){
			const serviceType = JSON.parse(localStorage.getItem('claims')).serviceType;  
			return serviceType.includes('MDM');
        },
        showJobProgress(){
        	return localStorage.getItem('showJobProgress');
        }
 	}
	
};
</script>