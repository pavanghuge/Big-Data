# Big-Data

FLIGHT DATA ANALYSIS

INTRODUCTION:
In this project, we have analyzed the Airline On-time Performance data set (flight data set) from the period of October 1987 to April 2008 from the following website https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/HG7NV7 
We have designed and implemented an Oozie workflow for that to solve following 3 problems: 
1.	the 3 airlines with the highest and lowest probability, respectively, for being on schedule
2.	the 3 airports with the longest and shortest average taxi time per flight (both in and out)
3.	the most common reason for flight cancellations. 

The oozie workflow is implemented with three map-reduce jobs that run in fully distributed mode. As per the diagram, the jobs get killed when an error is encountered.
 
ALGORITHMS:

First Map-Reduce: On Schedule Airlines:

1.	Mapper <key,value>:<UniqueCarrier,1 or 0> 
2.	The Mapper reads the data line by line, ignoring the first line and the NA data. If the data of the ArrDelay column which is less than or equal to 10 minutes, 
output: <UniqueCarrier,1>, otherwise output: <UniqueCarrier,0> 
3.	Reducer <key,value>:<UniqueCarrier,probability> 
4.	Probability = (# of 1) / (# of 1 and 0) 
5.	Reducer sums the values from the mapper of the same key, the sum will be the number of this airline when it is on-schedule. Calculate the total number of 0’s and 1’s, then calculate the on-schedule probability of this airline. 
6.	Reducer then uses the comparator function do the sorting. After sorting, output the 3 airlines with the highest and lowest probability. 
7.	If the data is NULL, then output: There is no value that can be used, so no output. 



Second Map-Reduce: Airports Taxi Time:

1.	Mapper <key,value>: <IATA airport code, TaxiTime>: <Origin,TaxiOut> or <Dest,TaxiIn> 
2.	The Mapper reads the data line by line, ignore the first line. If the data of the TaxiIn or the 
TaxiOut column is not NA, output: <IATA airport code, TaxiTime> 
3.	Reducer <key,value>: <IATA airport code, Average TaxiTime> 
4.	Reducer sums the value from the mapper of the same key (normal) and calculates the total number of times this key is found. Then do the equation: normal/all to calculate the average TaxiTime of each key. 
5.	Reducer then uses the comparator function do the sorting. After sorting, output the 3 airports with the longest and shortest average taxi time. 
6.	If the data is NULL, then output: There is no value can be used, so no output. 


Third Map-Reduce: Cancellation Reasons 

1.	Mapper <key,value>: < CancellationCode, 1> 
2.	The Mapper read the data line by line, ignore the first line. If the value of the Cancelled is 1 and the CancellationCode is not NA, output: < CancellationCode, 1> 
3.	Reducer <key,value>: < CancellationCode, sum of the 1s> 
4.	Reducer sums the value from the mapper of the same key. 
5.	Reducer then uses the comparator function do the sorting. After sorting, output the most common reason for flight cancellations. 
6.	If the data is NULL, then output: The most common reason for flight cancellations does not exist.


PERFORMANCE MEASUREMENT
a) A performance measurement plot that compares the workflow execution time in response to an increasing number of VMs used for processing the entire data set (22 years) and an in-depth discussion on the observed performance comparison results.


We can infer from the above figure that as the number of VM’s increases, the execution time of OOZIE workflow will decrease. As the number of VMs increase, the processing ability of the Hadoop cluster will also increase,
because then data can be processed in parallel on multiple data nodes. Then the execution time of every map-reduce job will be shorter than 
before, thus the execution time of the oozie workflow will be shorter than before too. However, the execution time to deal with the same data size 
will not always decrease by increasing the number of VMs. When the execution time decreases to a certain range, even if we try increasing the number of 
VMs, the execution time will no longer decrease. The reason for this is that more VMs means more information interaction time between the data nodes of a
Hadoop cluster. Information interaction time of a Hadoop cluster increases when the number of VMs increases. 
b) A performance measurement plot that compares the workflow execution time in response to an increasing data size (from 1 year to 22 years) 
and an in-depth discussion on the observed performance comparison results.

From the above figure we can infer that, as the size of the data increases, the execution time of the oozie workflow will also 
increase. At the beginning, there is time-consuming increase with the increase in the amount of data, but the time-consuming increasing
is slow, this is due to the fact that there is not much of an increase in the data size. However, after 1998 we see that the time-consuming 
increase is very fast that is execution time is very high. Hence, the slope becomes much steeper compared to the initial years. The reason for 
this is that the flight data between 1998 - 2008 has seen a sharp increase. We also see that more number of people have chosen to travel by plane.



