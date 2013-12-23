package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.*;

/**
 * Created by egizzi on 12/23/13.
 */
public class CartesianLineImpl implements CartesianLine{

  private final SpatialContext ctx;
  private double slope;
  private Point definingPoint;

  public CartesianLineImpl(double m, Point pt, SpatialContext ctx){
    this.ctx = ctx;
    reset(m, pt);
  }

  @Override
  public Point getDefiningPoint(){
    return definingPoint;
  }

  @Override
  public double getSlope(){
    return slope;
  }

  @Override
  public void reset(double m, Point pt){
    assert ! isEmpty();
    this.slope = m;
    this.definingPoint = pt;
  }

  //This logic is incorrect
  //filler data for the function
  @Override
  public SpatialRelation relate(Shape other){
    if (isEmpty() || other.isEmpty())
      return SpatialRelation.DISJOINT;
    if (other instanceof CartesianLine)
      return this.equals(other) ? SpatialRelation.INTERSECTS : SpatialRelation.DISJOINT;
    return other.relate(this).transpose();
  }

  @Override
  public Rectangle getBoundingBox(){
    return ctx.makeRectangle(0, 0, 0, 0);
  }

  @Override
  public boolean hasArea(){
    return false;
  }

  @Override
  public double getArea(SpatialContext ctx){
    return 0;
  }

  @Override
  public Point getCenter(){
    return this.definingPoint;
  }

  //incorrect
  @Override
  public Shape getBuffered(SpatialContext ctx, double distance){
    return getBoundingBox();
  }

  @Override
  public boolean isEmpty(){
    return Double.isNaN(slope);
  }

  //incorrect
  @Override
  public boolean equals(Object other){
    return false;
  }


}

