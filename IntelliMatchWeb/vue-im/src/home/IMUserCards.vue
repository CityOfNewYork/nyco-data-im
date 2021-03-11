<template>
    <div>
    	<div style="align=center">
	        <h1>Hi {{user.firstName + ' ' + user.lastName}}!</h1>
	        
	        <p>You're logged in IntelliMatch</p>
	        <h3>Users from secure api end point:</h3>
	        
	        <em v-if="users.loading">Loading users...</em>
	        
	        <span v-if="users.error" class="text-danger">ERROR: {{users.error}}</span>
	        
	        <ul v-if="users.items">
	            <li v-for="user in users.items" :key="user.id">
	                {{user.firstName + ' ' + user.lastName}}
	            </li>
	        </ul>
	        
	        {{uname}}
	        {{username}}
    	</div>
    </div>
    
</template>


<script>
export default {
	data:function () {
		return {
			uname : JSON.parse(localStorage.getItem('claims')).username
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
        }
    },
    created () {
    	// todo try pass parameter
    	//let uname =JSON.parse(localStorage.getItem('claims')).username;
        //this.$store.dispatch('users/getAll', {username:this.username} );
        this.$store.dispatch('users/getAll', {username:this.uname} );
    }
};
</script>

