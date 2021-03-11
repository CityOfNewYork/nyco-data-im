import { userService } from '../_services';

export const dict = {
    namespaced: true,
    state: {
        all: {}
    },
    actions: {
        getAll({ commit }) {
            commit('getAllRequest');
            userService.getDict()
                .then(
                	dict => commit('getAllSuccess', dict),
                    error => commit('getAllFailure', error)
                );
        }
    },
    mutations: {
        getAllRequest(state) {
            state.all = { loading: true };
        },
        getAllSuccess(state, dict) {
            state.all = { items: dict };
        },
        getAllFailure(state, error) {
            state.all = { error };
        }
    }
}
