<template>
<div>
    <div class="color-dark-background p-8 pt-0 border-blue serviceIntro aws">
      <div class="mx-auto mb-3">
        <h2 class="m-0" v-if="dict.awslandingpage_item_01_head">{{dict.awslandingpage_item_01_head}}</h2>
        <h2 class="m-0" v-else>IntelliMatch AWS Research</h2>
        
        <p class="flex">
          by<span class="sr-only">NYC Opportunity</span>
          <svg class="mis-1">
            <use xlink:href="#icon-logo-nyco-secondary"></use>
          </svg>
        </p>
        <p v-if="dict.awslandingpage_item_01">{{dict.awslandingpage_item_01}}</p>
        <p v-else>IntelliMatch AWS Research is Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
      </div>
    </div>

    <div class="w-full color-light-background p-8 text-center">
      <h2 class="mb-5">Types of Services</h2>
      <div class="layout-wrap mx-auto grid grid-cols-1 px-8 mb-6 serviceIconList">
        <div class="px-7">      
          <img class="iconimg" src="images/icon_aws.png" alt="aws console">
          
          <h3 class="mt-4" v-if="dict.awslandingpage_item_02_head">{{dict.awslandingpage_item_02_head}}</h3>
          <h3 class="mt-4" v-else>AWS Console</h3>

          <p class="mb-5" v-if="dict.awslandingpage_item_02">{{dict.awslandingpage_item_02}}</p>
          <p class="mb-5" v-else>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet.</p>
          <a class="btn btn-secondary inline-block mb-3" href="https://csc01.awsapps.com/start/#/" target="_blank" >Launch AWS Console</a> <br />
          <a class="text-base-black" href="contactus.html" target="popup" onclick="window.open('contactus.html','popup','width=400,height=100'); return false;" >Request an account</a>
        </div>
        <div class="px-7">
          <img class="iconimg" src="images/icon_upload-cloud.png" alt="aws data import">
          
          <h3 class="mt-4" v-if="dict.awslandingpage_item_03_head">{{dict.awslandingpage_item_03_head}}</h3>
          <h3 class="mt-4" v-else>AWS Data Import</h3>
          
          <p class="mb-5" v-if="dict.awslandingpage_item_03">{{dict.awslandingpage_item_03}}</p>
          <p class="mb-5" v-else>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet.</p>
          
          <a class="btn btn-secondary inline-block mb-3" v-on:click="aws();">Log in to Import Portal</a> <br />
          <a class="text-base-black" href="contactus.html" target="popup" onclick="window.open('contactus.html','popup','width=400,height=100'); return false;" >Request an account</a>
        </div>
      </div>
    </div>

    <div class="w-full p-8 bg-blue-10t color-light-background">
      <div class="layout-wrap mx-auto">
        
        <h1 v-if="dict.awslandingpage_item_04_head">{{dict.awslandingpage_item_04_head}}</h1>
        <h1 v-else>What is IntelliMatch?</h1>
        
        <p class="mb-5" v-if="dict.awslandingpage_item_04">{{dict.awslandingpage_item_04}}</p>
        <p class="mb-5" v-else>
          Have data that you want to analyze in the research platform? Use the IntelliMatch service to move your text, Excel or csv file into a table in your personal schema. Just select the file and load. It's that easy. But wait, that's not all! Use IntelliMatch to automate record matching projects or link against the NYCO workforce data sets. No file is too large and no data is too data!
        </p>
      </div>
    </div>
    <div class="w-full p-8 bg-blue-20t color-light-background">
      <div class="layout-wrap mx-auto">
        
        <h1 v-if="dict.awslandingpage_item_05_head">{{dict.awslandingpage_item_05_head}}</h1>
        <h1 v-else>How should I request the service?</h1>
        
        <p class="mb-5" v-if="dict.awslandingpage_item_05">{{dict.awslandingpage_item_05}}</p>
        <p class="mb-5" v-else>
          Have data that you want to analyze in the research platform? Use the IntelliMatch service to move your text, Excel or csv file into a table in your personal schema. Just select the file and load. It's that easy. But wait, that's not all! Use IntelliMatch to automate record matching projects or link against the NYCO workforce data sets. No file is too large and no data is too data!
        </p>
      </div>
    </div>
    <div class="w-full p-8 bg-blue-30t color-light-background">
      <div class="layout-wrap mx-auto">
        
        <h1 v-if="dict.awslandingpage_item_05_head">{{dict.awslandingpage_item_05_head}}</h1>
        <h1 v-else>What is IntelliMatch?</h1>
        
        <p class="mb-5" v-if="dict.awslandingpage_item_06">{{dict.awslandingpage_item_06}}</p>
        <p class="mb-5" v-else>
          Have data that you want to analyze in the research platform? Use the IntelliMatch service to move your text, Excel or csv file into a table in your personal schema. Just select the file and load. It's that easy. But wait, that's not all! Use IntelliMatch to automate record matching projects or link against the NYCO workforce data sets. No file is too large and no data is too data!
        </p>
      </div>
    </div>
</div>
</template>

<script>
import { convertDict } from '../_helpers/utils';
export default {
	  computed: {
	    dict () {
	    	return convertDict(this.$store.state.dict.all.items);
	    }
	  },
	  created () {
	        this.$store.dispatch('dict/getAll');
	  },
	  methods: {
		aws() {
			localStorage.setItem('serviceType', 'AWS');
			this.$router.go();
		},
	  }
}
</script>
