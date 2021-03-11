<template>
  <div>
	  <footer class="o-footer color-dark-background border-none">
	    <div class="layout-wrap mx-auto">
	      <div class="o-footer__divider border-navy-70t desktop:flex desktop:layout-sidebar mb-4">
	        <div class="pb-4" v-if="!user">
	          <a class="block" href="http://nyc.gov/opportunity" title="NYC Opportunity&lt;products@nycopportunity.nyc.gov&gt;" target="_blank">
	            <svg aria-hidden="true" class="o-footer__logo">
	              <use xlink:href="#icon-logo-secondary"></use>
	            </svg>
	            <span class="sr-only">The Mayor's Office for Economic Opportunity</span>
	          </a>
			</div>
			
	        <div class="pb-4" v-else>
		        <h1 class="flex items-center mb-0">{{appName}}&nbsp;<mark class="badge bg-transparent text-white border text-html">{{appVersion}}</mark></h1>
		        <p class="small flex items-end">
		          by<span class="sr-only">NYC Opportunity</span>
		          <svg class="o-header-nyco-secondary__icon mis-1">
		            <use xlink:href="#icon-logo-nyco-secondary"></use>
		          </svg>
		        </p>
	        </div>
	        
	        <div class="pb-4 desktop:pl-4 desktop:flex justify-end items-center">
	          <nav class="o-footer__inline-nav">
	            <a href=" " v-on:click="selectService();">Home</a>
	            <a href=" " v-on:click="mdm();">IntelliMatch</a>
	            <a href=" " v-on:click="aws();">AWS Research Facility</a>
	            <a href=" ">Data Catalog</a>
	          </nav>
	        </div>
	      </div>
	      <div class="desktop:grid desktop:grid-cols-2">
	        <div>
	          <p>
	            NYC Opportunity is the <a href="http://nyc.gov/opportunity" target="_blank">New York City Mayor's Office for Economic Opportunity</a>. We are committed to sharing open source software that we use in our products. Feel free to ask questions and share
	            feedback. Follow @nycopportunity on 
	            <a href="https://twitter.com/nycopportunity" target="_blank" rel="nofollow noopener">Twitter</a>, 
	            <a href="https://www.facebook.com/NYCOpportunity/" target="_blank"
	              rel="nofollow noopener">Facebook</a>, <a href="https://medium.com/@nycopportunity" target="_blank" rel="nofollow noopener">Medium</a>, and 
	              <a href="https://github.com/NYCOpportunity" target="_blank" rel="nofollow noopener"
	              title="Viewable by github.com/orgs/CityOfNewYork/ members.">Github</a>.
	          </p>
	        </div>
	        <div class="desktop:text-end">
	          <small>&#169; City of New York, 2021 All Rights Reserved. <br />NYC is a trademark and service mark of the City of New York. </small>
	        </div>
	      </div>
	    </div>
	  </footer>
  </div>
</template>

<script>
export default {

	computed: {
			serviceType () { return localStorage.getItem('serviceType');},
			user() { return	 localStorage.getItem('user');},
			appName() { return JSON.parse(localStorage.getItem('claims')).appName;},
			appVersion() { return JSON.parse(localStorage.getItem('claims')).appVersion;},
	},
    methods: {
        selectService() {
	        localStorage.removeItem('user');
	        localStorage.removeItem('claims');
			localStorage.removeItem('serviceType'); 
			this.$router.go();     
        },
        mdm() {
	        localStorage.removeItem('user');
	        localStorage.removeItem('claims');
			localStorage.setItem('serviceType', '_MDM');
			this.$router.go();        
		},
		aws() {
	        localStorage.removeItem('user');
	        localStorage.removeItem('claims');
			localStorage.setItem('serviceType', '_AWS');
			this.$router.go();        
		},
    }
};
</script>

