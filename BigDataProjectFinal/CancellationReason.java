import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CancellationReason
{
		
	public static void main(String[] args) throws Exception 
	{   
	    Configuration conf=new Configuration();
		Job wordFreq = new Job(conf,"The most common reason for flight cancellations.");
		wordFreq.setJarByClass(CancellationReason.class);

		wordFreq.setMapperClass(Map.class);
		wordFreq.setReducerClass(Reduce.class);
		
		FileInputFormat.addInputPath(wordFreq, new Path(args[0]));
		FileOutputFormat.setOutputPath(wordFreq, new Path(args[1]));

		wordFreq.setMapOutputKeyClass(Text.class);
		wordFreq.setMapOutputValueClass(LongWritable.class);
		wordFreq.setOutputKeyClass(Text.class);
		wordFreq.setOutputValueClass(LongWritable.class);

		wordFreq.waitForCompletion(true);

	}
	public static TreeSet<MyDataType> topCancellationReason = new TreeSet<MyDataType>();
	
	public static class MyDataType implements Comparable<MyDataType> 
	{
		String reason ;
		int count;
		MyDataType(String reason , int count)
			{
			this.reason  =  reason ;
			this.count = count;
			}

		public int compareTo(MyDataType myDataType) 
			{
			if(this.count <= myDataType.count)
				return 1;
			else
				return -1;
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
				
				String code = column[22];
				String reason="no reason given";
				String cancelledOrNot=column[21];
				if(cancelledOrNot.equals("1")&&!code.equals("NA"))
		     	{ 

					switch(code){
						case "A" : reason = "carrier";
									break;
						case "B" : reason = "weather";
									break;
						case "C" : reason = "NAS";
									break;
						case "D" : reason = "security";
									break;

					}
					context.write(new Text(reason), new LongWritable(1));
					
				}

		}

	}
    
    public static class Reduce extends Reducer<Text, LongWritable, Text, Text> 
    {
		public void reduce(Text key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException 
		{

		    int total = 0;
		    for(IntWritable val : values)
		    	{
		    	total = total + val.get();
		    	}
				
		    topCancellationReason.add( new MyDataType(key.toString(), total) );
		    if(topCancellationReason.size() > 1)
		    {
		    	topCancellationReason.pollLast();
            }
        }   
            protected void   cleanup (Context context) throws IOException, InterruptedException 
            {
		    	context.write(new Text("Top Reason for Cancellation:  "),null);  
    	    	while (!topCancellationReason.isEmpty())
		    	{
		    			MyDataType topReason = topCancellationReason.pollFirst();
		    			context.write(new Text(topReason.reason), new LongWritable(topReason.count));
		    	}
		    }
		    
		
	}



}
