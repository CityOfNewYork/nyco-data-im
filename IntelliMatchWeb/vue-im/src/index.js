import Vue from 'vue';

import { store } from './_store';
import { router } from './_helpers';
import App from './app/App';
import moment from 'moment';

Vue.filter('formatSize', function (size) {
	  if (size > 1024 * 1024 * 1024 * 1024) {
	    return (size / 1024 / 1024 / 1024 / 1024).toFixed(2) + ' TB'
	  } else if (size > 1024 * 1024 * 1024) {
	    return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
	  } else if (size > 1024 * 1024) {
	    return (size / 1024 / 1024).toFixed(2) + ' MB'
	  } else if (size > 1024) {
	    return (size / 1024).toFixed(2) + ' KB'
	  }
	  return size.toString() + ' B'
	})
;

Vue.filter('formatDate', function(value) {
  if (value) {
    return moment(value).format('MMM DD, YYYY, hh:mm A')
  }
})
;

Vue.filter('formatTime', function(ms){
    var d, h, m, s;
    s = Math.floor(ms / 1000);
    m = Math.floor(s / 60);
    s = s % 60;
    h = Math.floor(m / 60);
    m = m % 60;
    d = Math.floor(h / 24);
    h = h % 24;
    h += d * 24;
    return h + ':' + m + ':' + s;
});

new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App)
});
