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

public class OnTime{


	public static void main(String[] args) throws Exception 
	{   
	    Configuration conf=new Configuration();
		Job wordFreq = new Job(conf,"this Program provide 3 airlines with the highest and lowest probability");
		wordFreq.setJarByClass(OnTime.class);

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

	
	public static TreeSet<MyDataType> flightWithHighestProbablity = new TreeSet<MyDataType>();
	public static TreeSet<MyDataType> flightWithLowestProbablity = new TreeSet<MyDataType>();
	
	public static class MyDataType implements Comparable<MyDataType> 
	{
		double probablity;
		String key;

		MyDataType(double probablity, String key) 
		{
			this.probablity = probablity;
			this.key = key;
			  
		}

		@Override
		public int compareTo(MyDataType myDataType) 
		{
			
			if(this.probablity<=myDataType.probablity)
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
	
			    			    
                String column[] = value.toString().split(",");
			    String flight = column[8];
				
				if (isInteger(column[14])||isInteger(column[15])) {
				if (Integer.parseInt(column[14]) >= 10||Integer.parseInt(column[15]) >= 10) 
				{
					Text delayedCarrier = new Text("* "+flight);
					context.write(delayedCarrier, value_1);
				}
				 }
				Text countTotFlights = new Text(flight);
				context.write(countTotFlights, value_1);

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
				context.write(key, value_1);
			}

	}

	public static class Reduce extends Reducer<Text, LongWritable, Text, Text> 
	{

		private DoubleWritable probablity = new DoubleWritable();

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
				double flightCount =0.0;
				String flightName = key.toString().split(" ")[1];

				flightCount= getCount(values);        

				hashMap.put(flightName,flightCount);
			}
			else 
			{
				String flightName = key.toString().split(" ")[0];

				double count = getCount(values);   
				double countDelay = hashMap.get(flightName);
				probablity.set( countDelay / count); 
				Double probablityD = probablity.get();
                flightWithHighestProbablity.add(new MyDataType(probablityD, key.toString()));
				flightWithLowestProbablity.add(new MyDataType(probablityD, key.toString()));
				if (flightWithHighestProbablity.size() > 3) 
					{
					flightWithHighestProbablity.pollLast(); 
					} 
				if (flightWithLowestProbablity.size() > 3) 
					{
					flightWithLowestProbablity.pollFirst(); 
					}
			
			} 
		}
			
		protected void cleanup(Context context) throws IOException, InterruptedException 
		{   
		    context.write(new Text("Airline with Highest delay Probablity:  "),null);
			while (!flightWithHighestProbablity.isEmpty()) 
				{
					MyDataType pair = flightWithHighestProbablity.pollFirst();
	        		context.write(new Text(pair.key), new Text(Double.toString(pair.probablity)));
				}
			context.write(new Text("Airline with Lowest delay Probablity:  "),null);
			while (!flightWithLowestProbablity.isEmpty()) 
				{
					MyDataType pair = flightWithLowestProbablity.pollLast();
	        		context.write(new Text(pair.key), new Text(Double.toString(pair.probablity)));
				}
		}

	}

	

}

