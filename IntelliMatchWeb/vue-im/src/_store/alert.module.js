export const alert = {
    namespaced: true,
    state: {
        type: null,
        message: null
    },
    actions: {
        success({ commit }, message) {
            commit('success', message);
        },
        error({ commit }, message) {
            commit('error', message);
        },
        clear({ commit }) {
            commit('clear');
        }
    },
    mutations: {
        success(state, message) {
            state.type = 'alert-success';
            state.message = message;
        },
        error(state, message) {
        	console.log('alter error ' + JSON.stringify(state) + ":" + JSON.stringify(message)); // TODO
        	console.log(state);
        	console.log(message);
            state.type = 'alert-danger';
            state.message = "Login failed, Username or Password is incorrect.";
        	console.log('alter error ' + state.type + ": state.message" + state.message); // TODO
        },
        clear(state) {
            state.type = null;
            state.message = null;
        }
    }
}
