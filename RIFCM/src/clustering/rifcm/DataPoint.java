package clustering.rifcm;

import java.util.ArrayList;


public class DataPoint {
	public ArrayList<Float> point;
	public static int dim ;
	
	public DataPoint()
	{
		this.point = new ArrayList<Float>();
	}
	
	public DataPoint(ArrayList<Float> point)
	{
		// this.point = point;     This statement would create a pointer to the input arraylist and will not create a personal copy of the arraylist for the point
		this.point = new ArrayList<Float>();
		for(int i=0;i<point.size();i++)
		{
			this.point.add(point.get(i));
		}
		
	}

	public ArrayList<Float> getPoint() {
		return point;
	}

	public void setPoint(ArrayList<Float> point)
	{
		this.point = point;
	}
	
	public void displayPoint()
	{
		for (Float dim : point) {
			System.out.print(dim+",");
		}
		System.out.println();
	}
	
	public static float distanceBetween(DataPoint dp1, DataPoint dp2)
	{
		float distance = 0;
				
		for(int i=0;i<dp1.point.size();i++)
		{
			distance += (dp1.point.get(i)-dp2.point.get(i))*(dp1.point.get(i)-dp2.point.get(i));
		}
		distance = (float) Math.sqrt(distance);	
		
		return distance;
	}
	
	public static DataPoint copyDataPoint(DataPoint orgDataPoint)
	{
		if(orgDataPoint==null) return null;
		DataPoint copy = new DataPoint();
		for(Float dim: orgDataPoint.point)
		{
			copy.point.add(dim);
			
		}
		return copy;
	}
}
