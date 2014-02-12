module.exports = function(config){
    config.set({
    basePath : '../../',

    files : [
      'lib/jquery.min.js',
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
/*Maven usage
<plugin>
<groupId>com.kelveden</groupId>
<artifactId>maven-karma-plugin</artifactId>
<version>1.1</version>
<executions>
    <execution>
        <goals>
            <goal>start</goal>
        </goals>
    </execution>
</executions>
<configuration>
<configFile>full/path/to/the/file/my.custom.karma.conf.js</configFile>
</configuration>
</plugin>
*/