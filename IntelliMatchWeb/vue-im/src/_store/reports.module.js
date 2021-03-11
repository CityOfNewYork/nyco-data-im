import { userService } from '../_services';

export const reports = {
    namespaced: true,
    state: {
        all: {}
    },
    actions: {
        getAll({ commit }) {
            commit('getAllRequest');
            userService.getReports()
                .then(
                	reports => commit('getAllSuccess',reports),
                    error => commit('getAllFailure', error)
                );
        }
    },
    mutations: {
        getAllRequest(state) {
            state.all = { loading: true };
        },
        getAllSuccess(state, reports) {
            state.all = { items: reports };
        },
        getAllFailure(state, error) {
            state.all = { error };
        }
    }
}
