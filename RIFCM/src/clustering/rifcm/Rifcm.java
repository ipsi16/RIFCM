package clustering.rifcm;

import indexes.DBIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import clustering.rifcm.Cluster;
import clustering.rifcm.DataPoint;

public class Rifcm
{
	public static final int n = 500;
	public static final int noOfClusters = 5;
	public static final float epsolon = 0.5f;
	public static final float del =0.1f;
	public static final float wlower = 0.7f;
	public static final float wupper = 1 - wlower;
	public static final int m = 2;	
	public static ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	public static ArrayList<DataPoint> datapoints = new ArrayList<DataPoint>();
	public static ArrayList<DataPoint> orgDatapoints = new ArrayList<DataPoint>();
	public static float[][] membership = new float[noOfClusters][n];
	public static float[][] oldMembership = new float[noOfClusters][n];
	public static int[][] pointCount = new int[noOfClusters][3];
	public static final int lambda = 2;
		
	public static void main(String[] args)
	{
		//read i/p from file
				try
				{
					fetchData();
					
				} catch (NumberFormatException e) {
					System.err.println("Invalid number entries in the file\nFile should only contan comma seperated numbers");
					return;
				}
				if(datapoints.size()<noOfClusters)
				{
					System.out.println("Insufficient points");
					return;
				}
				
				//normalize datapoints
				normalise();
				
				//allocate cluster centroids			
				for(int i=0;i < noOfClusters; i++)
				{
					Cluster c = new Cluster(datapoints.get(i));
					clusters.add(c);
				}		
				
				//calculate membership value of each point for each clusters
				calculateMembership();
				
				//allocate each point to upper or lower approx of the respective clusters
				allocateClusters();
				
				while(!stopSignal())
				{
					determineNewCentroid();
					calculateMembership();
					allocateClusters();
				}	
				
				//Final Output Display
				System.out.println("Cluster Output : \n");
                for(int i=0;i<noOfClusters;i++)
                {
                    System.out.print("Cluster "+(i+1)+" : ");
                    for(int j=0;j<datapoints.size();j++)
                    {
                        if(membership[i][j]!=0)
                        {
                       System.out.print(membership[i][j]*100+"% of "+datapoints.get(j).point+"\t");
                            
                        }        
                  
                    }
                    System.out.println();
                }
                System.out.println();
	
                 System.out.println("Cluster  Low   Upper   Total");
                 for(int i=0;i<noOfClusters;i++)
                {
                    System.out.println((i+1)+"\t"+pointCount[i][0]+"\t"+ pointCount[i][1]+"\t"+pointCount[i][2]);
                   
                }
                 
                 allotPointsToClusters();
                 
                 //DB Index
                 DBIndex dbindex = new DBIndex(clusters);
                 System.out.println(dbindex.returnIndex());
				
	}
	
	private static void fetchData() throws NumberFormatException
	{		
		try
		{
			FileReader freader = new FileReader("Data.txt");
			BufferedReader breader = new BufferedReader(freader);
			
			
			
			String dataLine = breader.readLine();
			while(dataLine!=null)
			{
				
				ArrayList<Float> dim = new ArrayList<Float>();
				
				String[] dimString = dataLine.split(",");				
				for (String string : dimString)
				{
					string = string.trim();
					dim.add(Float.parseFloat(string));					
				}
				DataPoint datapoint = new DataPoint(dim);
				datapoints.add(datapoint);
				orgDatapoints.add(DataPoint.copyDataPoint(datapoint));
				dataLine = breader.readLine();
				
			}
			breader.close();
		}
		catch(NumberFormatException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static void calculateMembership()
	{
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				oldMembership[i][j]= membership[i][j];
			}
		}
	
		
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				membership[i][j]=-1;
			}
		}
				
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				float dij = DataPoint.distanceBetween(clusters.get(i).centroid,datapoints.get(j) );
								
				if(dij==0.0f)
				{
					membership[i][j]= 1;
					for(int h=0;h<noOfClusters;h++)
					{
						if(h!=i)
						{	
							membership[h][j]=0; 
						}
					}
				}
				else if(membership[i][j]==-1) 
				{
					membership[i][j]=0;
					for(int k = 0; k < noOfClusters; k++)
					{
						membership[i][j]+=Math.pow((dij/DataPoint.distanceBetween(datapoints.get(j),clusters.get(k).centroid)), 2.0/(m-1));
						
					}
					membership[i][j] = 1/membership[i][j];
				}
				
			}
		}
		
		for(int i = 0; i < noOfClusters; i++)
		{
			for(int j=0; j<datapoints.size();j++)
			{
				membership[i][j]= (1+lambda)*membership[i][j]/(1+lambda*membership[i][j]);
			}
		}
	}
	
	private static void allocateClusters()
	{
		System.out.println();
		System.out.println();
		
		//compute membership matrix based on distances
		for(int i=0;i<datapoints.size();i++)
		{
			int min2Pos,minPos=0;
			
			//finding closest jth cluster for ith point
			for(int j=0;j<noOfClusters;j++)
			{
				if(membership[j][i]<membership[minPos][i])
					minPos = j;
			}
			
			//finding 2nd closest cluster for jth point
			if(minPos==0)
			 min2Pos = 1;
			else min2Pos =0;
			for(int j=0;j<noOfClusters;j++)
			{
				if(j!=minPos && membership[j][i]<membership[min2Pos][i])
					min2Pos = j;
			}
			
			if(membership[min2Pos][i]-membership[minPos][i]>del)
			{
				membership[minPos][i]= 1;
				for(int k=0;k<noOfClusters;k++)
				{
					if(k!=minPos)
						membership[k][i]=0.0f;
				}
			}	
		}
	}
	
	private static void determineNewCentroid()
	{
		System.out.println("Cluster Centroids: \n");
		for(int i=0;i<noOfClusters;i++)
		{
			ArrayList<Float> lowerApproxComponent = new ArrayList<Float>();
			ArrayList<Float> boundaryComponent = new ArrayList<Float>();
            for(int k=0;k<datapoints.get(0).point.size();k++)
		    {
                    lowerApproxComponent.add(0.0f);
                    boundaryComponent.add(0.0f);
			}
               
			int lowerApproxCount=0,bc=0;
            float boundaryCount=0.0f;
			for(int j=0;j<datapoints.size();j++)
			{
				if(membership[i][j]==1.0f)
				{
					lowerApproxCount++;
					for(int k=0;k<datapoints.get(j).point.size();k++)
					{
					lowerApproxComponent.set(k, lowerApproxComponent.get(k)+datapoints.get(j).point.get(k));
					}
				}
				else if(membership[i][j]>0.0f && membership[i][j]<1.0f)
				{
                    bc++;
					boundaryCount+=(float)Math.pow(membership[i][j], m);
					for(int k=0;k<datapoints.get(j).point.size();k++)
					{
						boundaryComponent.set(k, boundaryComponent.get(k)+(float)Math.pow(membership[i][j], m)*datapoints.get(j).point.get(k));
					}
				}
			}
			pointCount[i][0]= lowerApproxCount;
            pointCount[i][1]= bc;
            pointCount[i][2]=lowerApproxCount+bc;
            
			ArrayList<Float> clusterCentroid = clusters.get(i).centroid.point;
			for(int k=0;k<datapoints.get(0).point.size();k++)
			{
				clusterCentroid.set(k,0.0f);	
			}
                /*if(lowerApproxCount==0&&boundaryCount==0)
                {
                     for(int k = 0;k<clusterCentroid.size();k++)
				{
				clusterCentroid.set(k,999.0f);
				}  
                }
                else
                {*/
			if(lowerApproxCount==0)
			{
				for(int k = 0;k<clusterCentroid.size();k++)
				{
                                
					clusterCentroid.set(k,boundaryComponent.get(k)/boundaryCount);
				}
			}
			else if(boundaryCount==0)
			{
				for(int k = 0;k<clusterCentroid.size();k++)
				{
                                   
					clusterCentroid.set(k,lowerApproxComponent.get(k)/lowerApproxCount);
				}
			}
			else
			{
				for(int k = 0;k<clusterCentroid.size();k++)
				{
				clusterCentroid.set(k,wlower*lowerApproxComponent.get(k)/lowerApproxCount+wupper*boundaryComponent.get(k)/boundaryCount);
				}
			}
         //}                
         System.out.println(clusterCentroid);
     }
	
	 System.out.println();
	
	}
	
	private static boolean stopSignal()
	{
		for(int i=0;i<noOfClusters;i++)
		{
			for(int j=0;j<datapoints.size();j++)
			{
				if(Math.abs(oldMembership[i][j]-membership[i][j])>epsolon)
					return false;
			}
		}
		return true;
	}
	
	public static void normalise()				//standard normalization between range 0 - 1
	{
		//arrays that store max and min value for every ith dimension
		float[] max,min;
		max = new float[datapoints.get(0).point.size()];
		min = new float[datapoints.get(0).point.size()];
		
		//initialising max and min array to dimensions of first datapoint
		for(int i = 0; i<datapoints.get(0).point.size();i++)
		{
			max[i] = datapoints.get(0).point.get(i).floatValue(); 
			min[i] = datapoints.get(0).point.get(i).floatValue(); 
		}
			
		//finding max and min values for each dimension 
		for(DataPoint dp : datapoints)
		{
			ArrayList<Float> currPoint = dp.point;
			for(int i =0 ; i<currPoint.size();i++)
			{
				if(currPoint.get(i)>max[i])
				{
					max[i]=currPoint.get(i);
				}
				else if(currPoint.get(i)<min[i])
				{
					min[i]=currPoint.get(i);
				}
			}			
		}
		
		//applying normalization formula new value = (oldValue - oldMinVal)/(oldMaxVal - oldMinVal)
		for(DataPoint dp : datapoints)
		{
			ArrayList<Float> currPoint = dp.point;
			for(int i =0 ; i<currPoint.size();i++)
			{
				currPoint.set(i, (currPoint.get(i)-min[i])/(max[i]-min[i]));
			}
		}
	}
	
	public static void allotPointsToClusters()
	{
		for(int j=0;j<datapoints.size();j++)		//For each datapoint
		{
			int maxPos=0;							//Finding cluster to which the jth cluster has maximum membership
			for(int i=0; i<noOfClusters;i++)
			{
				if(membership[i][j]==1.0f)
				{						
					clusters.get(i).memberDataPoints.add(datapoints.get(j));
					break;
				}
				if(membership[i][j]>membership[maxPos][j])
					maxPos = i;
			}
			
			clusters.get(maxPos).memberDataPoints.add(datapoints.get(j));	//adding datapoint to cluster with max membership
		}
	}
}
