
    $ mvn clean package
    $ cf login -a api.run.pivotal.io
    $ cf push categolj3 -p target/hello-pws-categolj3-0.0.1-SNAPSHOT.jar -m 512m --no-start
    $ cf create-service searchly starter demo-categolj3
    $ cf bind-service categolj3 demo-categolj3
    $ cf start categolj3