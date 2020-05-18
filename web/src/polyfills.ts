/***************************************************************************************************
 * Load `$localize` onto the global scope - used if i18n tags appear in Angular templates.
 */
import '@angular/localize/init';
(window as any).global = window;

import 'classlist.js';
import 'core-js/es6';
import 'core-js/es7/reflect';
require('zone.js/dist/zone');
require('zone.js/dist/long-stack-trace-zone');
