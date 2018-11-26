# Large Cluster Load Test

#### Overview:
* This is a Benchmark intended to test mirror datagrid under load

#### Project structure:
##### The project consist of the following pu's:
* mirror
* space (defaults topology is 2,1)
* crew-members-feeder - populating the space with 100 crew members with each deployment (evenly spread to all datagrids)
* flights-feeder - reading the crewmembers from space, then populating the space with 1000 with each deployment (evenly spread to all datagrids)
* benchmark - client that constantly queries the space for flights and crew members every 1 sec and print a summery of the queries duration every 30 sec

#### Running intructions:
1. Build the project (cd into project directory and run: ```mvn clean install```)
2. Deploy the pu's - here you have 2 options:
* **option1(this option for local deployment only):** run: ```mvn xap:deploy``` from the project root directory to deploy all the pu's in one line using mvn plugin
* **option2:** deploy each pu manually(gs-ui/webui/cli) **at this order**:
  * mirror
  * space
  * crew-members-feeder
  * flights-feeder
  * benchmark
  
  **you don't have to undeploy the feeders because they are not active after they write the objects to space(1 time only)**
3. Have a look at the gsc that the benchmark pu sitting at to see **intermediate reports** like this:
-------------Intermediate Summery Start-------------
.
.
-------------Intermediate Summery End-------------
this intermediate report is printed every 30s
4. To stop the benchmark and see final summary undeploy the benchmark pu and have look at his gcs
