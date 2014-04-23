module.exports = function(config){
    config.set({
    basePath : '../../',

    files : [
      'lib/channel.js',
      'lib/jquery.min.js',
      'lib/jquery.cookie.js',
      'lib/angular.js',
      'lib/angular-*.js',
      'test/lib/angular-mocks.js',
      'js/**/*.js',
      'test/unit/**/*.js'
    ],

    exclude : [
      'lib/angular-loader.js',
      /*'lib/*.min.js',*/
      'lib/angular-scenario.js'
    ],

    autoWatch : true,

    frameworks: ['jasmine'],

    browsers : ['Chrome'],

    plugins : [
            'karma-junit-reporter',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-jasmine'
            ],

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    }
})}
