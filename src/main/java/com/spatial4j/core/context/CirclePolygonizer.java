package com.spatial4j.core.context;


import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.*;


/**
 * Created by egizzi on 12/23/13.
 */
public class CirclePolygonizer {

  public static void main(String[] args) {
    SpatialContext ctx = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(0, 100, 200, 300, null));
    Circle circle = ctx.makeCircle(50.0, 250.0, 10.0);
    CirclePolygonizer CirclePolygonizerObj = new CirclePolygonizer(ctx, circle);

    List<Point> lstOfPoints = CirclePolygonizerObj.getEnclosingPolygon(0.1);
  }

  protected SpatialContext ctx;
  protected Circle circ;

  public CirclePolygonizer(SpatialContext ctx, Circle circ){
    this.ctx = ctx;
    this.circ = circ;
  }

  public List<Point> getEnclosingPolygon(double tolerance){

    Point definingPoint1 = ctx.makePoint(circ.getCenter().getX(), circ.getCenter().getY()+circ.getRadius());
    Point definingPoint2 = ctx.makePoint(circ.getCenter().getX()+circ.getRadius(), circ.getCenter().getY());

    InfBufLine line1 = new InfBufLine (0.0, definingPoint1, 0);
    InfBufLine line2 = new InfBufLine (Double.POSITIVE_INFINITY, definingPoint2, 0);

    ArrayList<Point> listOfPoints = new ArrayList<Point>();
    listOfPoints.add(definingPoint1);
    recursiveIter(tolerance, line1, line2, listOfPoints);
    listOfPoints.add(definingPoint2);

    translatePoints(listOfPoints);

   // printListOfPoints(listOfPoints);

    return listOfPoints;
  }

  protected void recursiveIter(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> listOfPoints){
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersection(lineIntersectionPoint);
    double currentMaxDistance = ctx.getDistCalc().distance(circleIntersectionPoint, lineIntersectionPoint);
    if (currentMaxDistance <= tolerance){
      listOfPoints.add(lineIntersectionPoint);
    } else {
      InfBufLine line3 = calcTangentLine(circleIntersectionPoint);
      recursiveIter(tolerance, line1, line3, listOfPoints);
      listOfPoints.add(circleIntersectionPoint);
      recursiveIter(tolerance, line3, line2,  listOfPoints);
    }
  }

  protected Point calcLineIntersection(InfBufLine line1, InfBufLine line2){

    if(line1.equals(line2)){
      throw new IllegalArgumentException("Cannot calculate intersection point of two equivalent lines");
    } else if(line1.getSlope() == line2.getSlope()){
      //Should throw an exception here
      throw new IllegalArgumentException("Cannot calculate intersection point of two parallel lines");
    }else if(Double.isInfinite(line1.getSlope())){
      double X = line1.getIntercept();
      double Y = line2.getSlope()*X + line2.getIntercept();
      return new PointImpl(X, Y, ctx);
    }else if(Double.isInfinite(line2.getSlope())){
      double X = line2.getIntercept();
      double Y = line1.getSlope()*X + line1.getIntercept();
      return new PointImpl(X, Y, ctx);
    }else{
      double X = (line2.getIntercept() - line1.getIntercept())/(line1.getSlope()-line2.getSlope());
      double Y = line1.getSlope()*X + line1.getIntercept();
      return new PointImpl(X, Y, ctx);
    }
  }

  //assumed that point is outside circle
  protected Point calcCircleIntersection(Point point){
    double radius = circ.getRadius();
    double slope = calcSlope(circ.getCenter(), point);
    double theta = Math.atan(slope);
    double X = radius*Math.cos(theta) + circ.getCenter().getX();
    double Y = radius*Math.sin(theta) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  //must be given a point on the circle
  protected InfBufLine calcTangentLine(Point pt){
    double epsilon = 1E-12;
    double X = pt.getX()-circ.getCenter().getX();
    double Y = pt.getY()-circ.getCenter().getY();
    double radius = circ.getRadius();
    double radiusSquared = radius*radius;
    if( !((X*X + Y*Y < radiusSquared+epsilon) &&
        (X*X + Y*Y > radiusSquared-epsilon))){
      throw new IllegalArgumentException("Point does not lie on circle");
    }
    return new InfBufLine(getPerpSlope(calcSlope(circ.getCenter(), pt)), pt, 0);
  }

  protected double calcSlope(Point point1, Point point2){
    if(point1.equals(point2)){
      throw new IllegalArgumentException("Cannot calculate slope between two equivalent points");
    }
    double changeInY = point2.getY()-point1.getY();
    double changeInX = point2.getX()-point1.getX();
    if(changeInX == 0){
      return Double.POSITIVE_INFINITY;
    }
    return changeInY/changeInX;
  }

  protected double getPerpSlope(double slope){
    if(Double.isInfinite(slope)){
      return 0;
    }else if(slope == 0){
      return Double.POSITIVE_INFINITY;
    }
    return -1/slope;
  }

  protected void translatePoints(List <Point> lstOfPoints){
    double xBound = circ.getCenter().getX();
    double yBound = circ.getCenter().getY();
    double X = 0;
    double Y = 0;

    int lstSize = lstOfPoints.size();
    for(int i=lstSize-2;i>=0; i--){
      X = (lstOfPoints.get(i).getX());
      Y =  yBound - (lstOfPoints.get(i).getY()-yBound);
      Point point = ctx.makePoint(X, Y);
      lstOfPoints.add(point);
    }
    lstSize = lstOfPoints.size();
    for(int i=lstSize-2;i>0; i--){
      X =  xBound - (lstOfPoints.get(i).getX()-xBound);
      Y = (lstOfPoints.get(i).getY());
      Point point = ctx.makePoint(X, Y);
      lstOfPoints.add(point);
    }
  }

  public void printListOfPoints(List <Point> lstOfPoints){
    System.out.print("polygon points\n");
    for(int i=0;i<lstOfPoints.size(); i++){
      System.out.print(lstOfPoints.get(i));
      System.out.print('\n');
    }
  }


}