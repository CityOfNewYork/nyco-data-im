<template>
<div>
    <div class="color-dark-background p-8 pt-0 border-navy portalIntro">
      <div class="mx-auto grid grid-cols-1 serviceIconList mb-8">
        <div class="border-r-2 p-4 border-blue">
 
          <h1 class="h1 m-0" v-if="dict.landingpage_item_01_head">{{dict.landingpage_item_01_head}}</h1>
          <h1 class="h1 m-0" v-else>IntelliMatch</h1>
          
          <p class="flex items-end">
            by<span class="sr-only">NYC Opportunity</span>
            <svg class="mis-1">
              <use xlink:href="#icon-logo-nyco-secondary"></use>
            </svg>
          </p>
        </div>
        <div class="p-4">
          <p v-if="dict.landingpage_item_01">{{dict.landingpage_item_01}}</p>
          <p v-else>Our mission is Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
        </div>
      </div>
    </div>

    <div class="w-full color-light-background p-8">
      <div class="layout-wrap mx-auto grid grid-cols-3 px-8 mb-6">
        <div>
          <img src="images/portalim.png" alt="IntelliMatch" />
        </div>
        <div class="col-span-2 mt-4">
        
          <h1 v-if="dict.landingpage_item_02_head">{{dict.landingpage_item_02_head}}</h1>
          <h1 v-else>IntelliMatch</h1>
          
          <p v-if="dict.landingpage_item_02">{{dict.landingpage_item_02}}</p>
          <p v-else>Have data that you want to analyze in the research platform? Use the IntelliMatch service to move your text, Excel or csv file into a table in your personal schema. Just select the file and load. It's that easy.</p>
          <button class="btn btn-primary" v-on:click="mdm();">Learn More</button>
        </div>
      </div>
      <div class="layout-wrap mx-auto grid grid-cols-3 px-8 mb-6">
        <div>
          <img src="images/portalaws.png" alt="IntelliMatch" />
        </div>
        <div class="col-span-2 mt-4">
          
          <h1 v-if="dict.landingpage_item_03_head">{{dict.landingpage_item_03_head}}</h1>
          <h1 v-else>IntelliMatch AWS Research</h1>
          
          <p v-if="dict.landingpage_item_03">{{dict.landingpage_item_03}}</p>
          <p v-else>Use this link to access the Equity Data Research Platform. Get direct access to your personal equity research workspace and data. Be sure to stay up to date on the latest announcements, training and data.</p>
          <button class="btn btn-primary" v-on:click="aws();">Learn More</button>
        </div>
      </div>
      <div class="layout-wrap mx-auto grid grid-cols-3 px-8 mb-6">
        <div>
          <img src="images/portaldc.png" alt="IntelliMatch" />
        </div>
        <div class="col-span-2 mt-4">
          
          <h1 v-if="dict.landingpage_item_04_head">{{dict.landingpage_item_04_head}}</h1>
          <h1 v-else>Data Catalog</h1>
          
          <p v-if="dict.landingpage_item_04">{{dict.landingpage_item_04}}</p>
          <p v-else>Click here for the data catalog to beat all catalogs. Search to find extensive information about the data sets currently in the Equity Data Research Platform.</p>
          <button class="btn btn-primary">Export Data Assets</button>
        </div>
      </div>
    </div>

    <div class="w-full p-8 bg-blue color-dark-background">
      <div class="layout-wrap mx-auto">
        
        <h1 v-if="dict.landingpage_item_05_head">{{dict.landingpage_item_05_head}}</h1>
        <h1 v-else>Make Data More Accessible</h1>
        
        <p v-if="dict.landingpage_item_05">{{dict.landingpage_item_05}}</p>
        <p v-else class="mb-5">{{ourmission}}</p>
        <a class="btn btn-secondary" href="contactus.html" target="popup" onclick="window.open('contactus.html','popup','width=400,height=100'); return false;" >Contact Us</a>
      </div>
    </div>
</div>
</template>


<script>
import ourmission from 'raw-loader!./ourmission.txt';
import { convertDict } from '../_helpers/utils';

export default {
	  name: 'landingPage',
	  data: function () {
	      return {
	          ourmission: ourmission.replace('export default','').replace(/"/g,'').replace(/;/g,'').replace(/\\r\\n/g,''),
	      }
	  },
      computed: {
        dict () {
        	return convertDict(this.$store.state.dict.all.items);
        }
      },
	  created () {
	        this.$store.dispatch('dict/getAll');
	  },
	  methods: {
		mdm() {
			localStorage.setItem('serviceType', '_MDM');
			this.$router.go();
		},
		aws() {
			localStorage.setItem('serviceType', '_AWS');
			this.$router.go();
		},
	}
}
</script>
