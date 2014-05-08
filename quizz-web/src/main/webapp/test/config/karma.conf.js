module.exports = function(config){
    config.set({
    basePath : '../../',

    files : [
      'http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js',
      'http://cdnjs.cloudflare.com/ajax/libs/jquery-cookie/1.3.1/jquery.cookie.js',
      'http://ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular.js',
      'http://ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-cookie.js',
      'http://ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-resource.js',
      'http://ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-route.js',
      'http://ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-sanitize.js',
      'test/lib/angular-mocks.js',
      'js/**/*.js',
      'test/unit/**/*.js'
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
