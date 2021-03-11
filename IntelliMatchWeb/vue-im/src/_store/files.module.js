import { userService } from '../_services';

export const files = {
    namespaced: true,
    state: {
        all: {}
    },
    actions: {
    	// pass parameter from payload
        getAll({ commit }) {
            commit('getAllRequest');
            userService.getFiles()
                .then(
                	files => commit('getAllSuccess', files),
                    error => commit('getAllFailure', error)
                );
        }
    },
    mutations: {
        getAllRequest(state) {
            state.all = { loading: true };
        },
        getAllSuccess(state, files) {
            state.all = { items: files };
        },
        getAllFailure(state, error) {
            state.all = { error };
        }
    }
}
