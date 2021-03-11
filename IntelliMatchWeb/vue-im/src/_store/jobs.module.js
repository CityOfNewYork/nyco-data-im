import { userService } from '../_services';

export const jobs = {
    namespaced: true,
    state: {
        all: {}
    },
    actions: {
    	// pass parameter from payload
        getAll({ commit }) {
            commit('getAllRequest');
            userService.getJobs()
                .then(
                	jobs => commit('getAllSuccess', jobs),
                    error => commit('getAllFailure', error)
                );
        }
    },
    mutations: {
        getAllRequest(state) {
            state.all = { loading: true };
        },
        getAllSuccess(state, jobs) {
            state.all = { items: jobs };
        },
        getAllFailure(state, error) {
            state.all = { error };
        }
    }
}
