<template>
    <div>
    
        <!--<div v-if="alert.message" :class="`alert ${alert.type}`">{{alert.message}}</div>-->
        
        <router-view/>
        <!--<IMFooter/>-->
    </div>
</template>

<script>
import IMFooter from '../app/IMFooter.vue'

export default {
    name: 'app',
    components: {
	    IMFooter
	  },
    computed: {
        alert () {
            return this.$store.state.alert
        }
    },
    watch:{
        $route (to, from){
            // clear alert on location change
            this.$store.dispatch('alert/clear');
        }
    } 
};
</script>