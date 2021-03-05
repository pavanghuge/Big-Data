
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TaxiTime{

    public static void main(String[] args) throws Exception 
	{   
	    Configuration conf=new Configuration();
		Job wordFreq = new Job(conf,"Longest and shortest average taxi time per airport.");
		wordFreq.setJarByClass(TaxiTime.class);

		wordFreq.setMapperClass(Map.class);
		wordFreq.setCombinerClass(Combiner.class);
		wordFreq.setReducerClass(Reduce.class);
		
		FileInputFormat.addInputPath(wordFreq, new Path(args[0]));
		FileOutputFormat.setOutputPath(wordFreq, new Path(args[1]));

		wordFreq.setMapOutputKeyClass(Text.class);
		wordFreq.setMapOutputValueClass(LongWritable.class);
		wordFreq.setOutputKeyClass(Text.class);
		wordFreq.setOutputValueClass(LongWritable.class);

		wordFreq.waitForCompletion(true);

	}
	
	public static TreeSet<MyDataType> airportWithHighestAverage = new TreeSet<MyDataType>();
	public static TreeSet<MyDataType> airportWithLowestAverage = new TreeSet<MyDataType>();
	
	
	public static class MyDataType implements Comparable<MyDataType> 
	{
		double average;
		String key;

		MyDataType(double average, String key) 
		{
			this.average = average;
			this.key = key;
			  
		}

		@Override
		public int compareTo(MyDataType myDataType) 
		{
			
			if(this.average<=myDataType.average)
			{
				return 1;
			}
			else 
			{
				return -1;
			}
			
		}
	}
	
	
	
	
	public static class Map extends Mapper<LongWritable, Text, Text, LongWritable>
		{
                
                private LongWritable value_1=new LongWritable(1);
				public static boolean isInteger(String s) {
			        boolean isValidInteger = false;
			    try {
				    Integer.parseInt(s);
				    isValidInteger = true;
			    } catch (NumberFormatException ex)
			    {
			     }
			    return isValidInteger;
		        }

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
			{
	
			    			    
                String column[] =value.toString().split(",");
				
				if (isInteger(column[19])||isInteger(column[20])) 
				{
					Text origin_1 = new Text("* "+column[16]);
					context.write(origin_1, new LongWritable(Long.parseLong(column[19])));					
					Text destination_1 = new Text("* "+column[17]);
					context.write(destination_1, new LongWritable(Long.parseLong(column[20])));
				}
				Text origin = new Text(column[16]);
				context.write(origin, value_1);
				Text destination = new Text(column[17]);
				context.write(destination, value_1);

		    
            }
		}
    

	public static class Combiner extends Reducer<Text, LongWritable, Text, LongWritable>
		{
			private LongWritable value_1=new LongWritable();
			
		    public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
			{
				long sum = 0;
				
				for (LongWritable val : values)
					{
						sum = sum + val.get();
					}
				
				value_1.set(sum);
				
				//collecting output
				context.write(key, value_1);
			}

		}
    
	public static class Reduce extends Reducer<Text, LongWritable, Text, Text> 
	   {

		private DoubleWritable average = new DoubleWritable();

		private HashMap<String,Double> hashMap = new HashMap<String,Double>();
		
		
		private double getCount(Iterable<LongWritable> values) 
		{
			double count = 0;
			
			for (LongWritable value : values) 
			{
				count += value.get();
			}
			return count;
		}

		public void reduce(Text key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException 
			{

			if (key.toString().split(" ")[0].equals("*")) 
			{
				double taxiTime =0.0;
				String airportName = key.toString().split(" ")[1];

				taxiTime= getCount(values);  

				hashMap.put(airportName,taxiTime);
			}
			else 
			{
				String airportName = key.toString().split(" ")[0];

				double count = getCount(values);   
				double totalTaxiTime = hashMap.get(airportName);
				average.set( totalTaxiTime / count);  
				Double averageD = average.get();
                airportWithHighestAverage.add(new MyDataType(averageD, key.toString()));
				airportWithLowestAverage.add(new MyDataType(averageD, key.toString()));
				if (airportWithHighestAverage.size() > 3) 
					{
					airportWithHighestAverage.pollLast(); 
					} 
				if (airportWithLowestAverage.size() > 3) 
					{
					airportWithLowestAverage.pollFirst(); 
					}
			
			} 
		}
			
		protected void cleanup(Context context) throws IOException, InterruptedException 
		{   
		    context.write(new Text("Airports with Highest average taxi time:  "),null);
			while (!airportWithHighestAverage.isEmpty()) 
				{
					MyDataType pair = airportWithHighestAverage.pollFirst();
	        		context.write(new Text(pair.key), new Text(Double.toString(pair.average)));
				}
			context.write(new Text("Airports with Lowest average taxi time:  "),null);
			while (!airportWithLowestAverage.isEmpty()) 
				{
					MyDataType pair = airportWithLowestAverage.pollLast();
	        		context.write(new Text(pair.key), new Text(Double.toString(pair.average)));
				}
		}

	}

	

}
