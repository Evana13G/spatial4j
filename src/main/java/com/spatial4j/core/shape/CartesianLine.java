package com.spatial4j.core.shape;


/**
 * Created by egizzi on 12/23/13.
 */
public interface CartesianLine extends Shape{

  public void reset(double m, Point pt);

  public double getSlope();

  public Point getDefiningPoint();

}

