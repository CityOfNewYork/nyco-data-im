<template>
    <div>
    	<div style="align=center">
<!--
    		  <div v-for="run in reportGroupByRun.items" :key="run.runid">
				{{run.runid}}
				{{run.department}} 		
				{{run.createdDate| formatDate }} 		

	    		<div v-for="rpt in run.runItems" :key="rpt.id">
	    			{{rpt.name}}
	    			{{rpt.department}}
	    			{{rpt.type}}
	    			{{rpt.createdDate| formatDate}}
	    		</div>
    		  </div>
-->			
		      <h2 class="mt-8 pt-8 font-bond">Match &amp; Report</h2>
		      <p>
		        Reports are available on Axway SFTP client: <a :href="axwayurl" class="text-blue" target="_blank">{{axwayurl}}</a> <br />
		        Use the credentials provided to you when we set up your account {{outputAxwayId}} for this project.
		      </p>
			
			  <div v-for="run in reportGroupByRun.items" :key="run.runid">

			      <article class="c-card p-0 mb-3 bg-blue-20t">
			        <header class="c-card__header justify-start mb-1 pt-3 px-4">
			          <h2 class="c-card__title mr-2">Match {{run.runid}}</h2>
			        </header>
			        <div class="c-card__body mb-3">
			          <div class="grid grid-cols-3 gap-3 px-4">
			            <div>Initiated by {{run.department}}</div>
			            <div>{{run.createdDate| formatDate}}</div>
			            
			            <div class="text-right">
							<div v-if="run.runid == idShow">
				                <a class="text-blue" v-on:click="toggle(run.runid)" >Hide Report Information
				                  <svg aria-hidden="true" class="icon-ui align-middle" >
									<use xlink:href="#feather-chevron-up" ></use>
				                  </svg>
				                </a>
							</div>
							<div v-else>
				                <a class="text-blue" v-on:click="toggle(run.runid)" >View Report Information
				                  <svg aria-hidden="true" class="icon-ui align-middle" >
									<use xlink:href="#feather-chevron-down" ></use>
				                  </svg>
				                </a>
							</div>
						</div>
						 
			          </div>
			        </div>
			        
				    <div v-if="idShow === run.runid"> 
				        <footer class="bg-base-white p-4" >
				          <dl class="description-list suggestions" v-for="rpt in run.runItems" :key="rpt.id">
				            <dt>{{rpt.name}}</dt>
				            <dd>{{rpt.type}}</dd>
				          </dl>
				        </footer>
	    		    </div>
	    		    
			      </article>
			  </div>
			  <div v-if="reportGroupByRun.items.length === 0">
			  	No report available.
			  </div>
			
    	</div>
    </div>
    
</template>


<script>
import { reportGroupByRun,axwayurl,outputAxwayId } from '../_helpers/utils';

export default {
	data:function () {
		return {
			uname    : JSON.parse(localStorage.getItem('claims')).username,
			axwayurl : axwayurl(),
			outputAxwayId : outputAxwayId(),
			idShow   : -1
		}
	},
    computed: {
    	username (){
    		return JSON.parse(localStorage.getItem('claims')).username;
    	},
        reports () {
            return this.$store.state.reports.all;
        },
        reportGroupByRun() {
            return reportGroupByRun(this.$store.state.reports.all);
        }
        
    },
    mounted () {
        this.reload();
    },
    
    methods:{
		reload: function(){
			this.$store.dispatch('reports/getAll');
		},
		
        toggle: function(id){
        	if(id === this.idShow){
        		this.idShow = -1;
        	}else{
        		this.idShow = id;
        	}
        },
		
	}
};
</script>

