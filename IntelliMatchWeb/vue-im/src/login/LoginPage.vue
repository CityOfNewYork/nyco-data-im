<template>
<div>

    <header class="o-header-app layout-sidebar topnav flex">
      <div class="color-dark-background text-base-white">
        <a class="font-bold no-underline" v-on:click="selectService();" >IntelliMatch Portal</a>
      </div>
      <div class="color-dark-background justify-end">
        <nav>
          <a class="mr-3 font-light no-underline" v-on:click="mdm();">IntelliMatch</a>
          <a class="mr-3 font-light no-underline" v-on:click="aws();">AWS Research Facility</a>
          <a class="mr-3 font-light no-underline" href="">Data Catalog</a>
        </nav>
      </div>
    </header>

	<div v-if="serviceType === 'AWS' || serviceType === 'MDM'" class="color-dark-background">
		<div class="login" >
			<div class="layout-content w-full p-8 min-h-screen">
			      <article class="mx-auto color-light-background c-card">
			        <form @submit.prevent="handleSubmit">
				        <header class="c-card__header">
				          <h1 class="mx-auto" v-if="serviceType==='MDM'">IntelliMatch</h1>
				          <h1 class="mx-auto" v-if="serviceType==='AWS'">IntelliMatch:AWS</h1>
				        </header>

				        <div class="c-card__body">
				            <div class="input mb-2">
				              <label class="font-bold" for="user_name">User Name</label>
				              <input class="w-full" type="text" v-model="username" placeholder="username" autocomplete="username"/>
		              		  <div v-show="submitted && !username" class="invalid-feedback  text-red font-bold">Username is required</div>
				            </div>
				            <div class="input">
				              <label class="font-bold" for="password">Password</label>
				              <input class="w-full" type="password" v-model="password" placeholder="password" autocomplete="current-password" />
				              <div v-show="submitted && !password" class="invalid-feedback text-red font-bold">Password is required</div>
				            </div>
				            
							<div v-if="alert.message" class="invalid-feedback text-red font-bold">{{alert.message}}</div>
							
				          <p class="font-bold" v-if="dict.loginpage_item_01">{{dict.loginpage_item_01}}</p>
				          <p class="font-bold" v-else>When you log in, you are confirming that you have read, understood, and agree to the following:</p>
				          
				          <ul>
				          	<li v-if="dict.loginpage_item_02">{{dict.loginpage_item_02}}</li>
				            <li v-else>You will only use the platform for the legally authorized use associated with your role and work unit.</li>
				            
				            <li v-if="dict.loginpage_item_03">{{dict.loginpage_item_03}}</li>
				            <li v-else>You will treat all information in the platform as confidential and will not share it with anyone who is not authorized to view IntelliMatch data, including your co-workers.</li>
				            
				            <li v-if="dict.loginpage_item_04">{{dict.loginpage_item_04}}</li>
				            <li v-else>Your use of IntelliMatch may be monitored.</li>
				            
				            <li v-if="dict.loginpage_item_05">{{dict.loginpage_item_05}}</li>
				            <li v-else>Unauthorized or inappropriate use may be reported to the appropriate NYC officials and may subject you to disciplinary action.</li>
				            
				            <li v-if="dict.loginpage_item_06">{{dict.loginpage_item_06}}</li>
				            <li v-else>Any improper use or disclosure of IntelliMatch information will subject you to your agency's disciplinary process, and may subject you to civil or criminal penalties.</li>
				          </ul>
				        </div>
				        <footer class="w-full text-center">
				           <button class="btn btn-secondary">Sign in</button>
				        </footer>

			        </form>
			      </article>
			</div>
		</div>
	</div>
	<div v-else-if="serviceType === '_MDM'">
			<MdmLandingPage/>
	</div>
	<div v-else-if="serviceType === '_AWS'">
			<AwsLandingPage/>
	</div>
	<div v-else>
			<LandingPage/>
	</div>
	
	
	
	<IMFooter/>
</div>
</template>

<script>
import LandingPage from './LandingPage.vue'
import MdmLandingPage from './MdmLandingPage.vue'
import AwsLandingPage from './AwsLandingPage.vue'
import IMFooter from '../app/IMFooter.vue'
import { userService } from '../_services';
import { convertDict } from '../_helpers/utils';
  
export default {
	components: {
		LandingPage,
		MdmLandingPage,
		AwsLandingPage,
		IMFooter
	},
    data () {
        return {
            username: '',
            password: '',
            submitted: false
        }
    },
    computed: {
        loggingIn () {
            return this.$store.state.authentication.status.loggingIn;
        },
        serviceType () {
        	return localStorage.getItem('serviceType');
        },
        alert () {
            return this.$store.state.alert
        },
        
        dict () {
        	return convertDict(this.$store.state.dict.all.items);
        }
        
    },
    created () {
        // reset login status
        this.$store.dispatch('authentication/logout');
        this.$store.dispatch('dict/getAll');
    },
    methods: {
        handleSubmit (e) {
            this.submitted = true;
            const { username, password } = this;
            const { dispatch } = this.$store;
            if (username && password) {
                dispatch('authentication/login', { username, password }) ;
            }
        },
        selectService() {
			localStorage.removeItem('serviceType');   
			this.$router.go();     
        },
        mdm() {
			localStorage.setItem('serviceType', '_MDM');
			this.$router.go();
		},
		aws() {
			localStorage.setItem('serviceType', '_AWS');
			this.$router.go();
		},
    }
};
</script>



