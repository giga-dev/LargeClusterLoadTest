# Large Cluster Load Test

#### Running intructions:
1. Build the project (cd into project directory and run: ```mvn clean install```)
2. Deploy the pu's - here you have 2 options:
* **option1:** run: ```mvn xap:deploy``` from the project root directory to deploy all the pu's in one line using mvn plugin
* **option2:** deploy each pu manually(gs-ui/webui/cli) at this order:
  * mirror
  * space
  * crew-members-feeder
  * flights-feeder
  * benchmark
3. Have a look at the gsc that the benchmark pu sitting at to see intermediate reports
4. To stop the benchmark and see final summary undeploy the benchmark pu and have look at his gcs
