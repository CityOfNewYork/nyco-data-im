import Vue from 'vue';
import Router from 'vue-router';

import HomePage from '../home/HomePage'
import LoginPage from '../login/LoginPage'

Vue.use(Router);

export const router = new Router({
  //  mode: 'history',
  mode: 'hash',
  routes: [
    { path: '/web', component: HomePage },
    { path: '/web/login', component: LoginPage },

    // otherwise redirect to home
    { path: '/error', redirect: '/web' },
    { path: '*', redirect: '/web' }
  ]
});

router.beforeEach((to, from, next) => {
  // redirect to login page if not logged in and trying to access a restricted page
  const publicPages = ['/web/login'];
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('user');

  if (authRequired && !loggedIn) {
     return next('/web/login');
  }

  next();
})