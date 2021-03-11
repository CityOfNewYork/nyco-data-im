import Vue from 'vue';
import Vuex from 'vuex';

import { alert } from './alert.module';
import { authentication } from './authentication.module';
import { users } from './users.module';
import { files } from './files.module';
import { jobs } from './jobs.module';
import { reports } from './reports.module';
import { dict } from './dict.module';

Vue.use(Vuex);

export const store = new Vuex.Store({
    modules: {
        alert,
        authentication,
        users,
        files,
        jobs,
        reports,
        dict
    }
});
