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
    CirclePolygonizer CirclePolygonizerTest = new CirclePolygonizer(ctx, circle);
    List<Point> listOfPoints = CirclePolygonizerTest.getEnclosingPolygon(0.1);
    for(int i=0;i<listOfPoints.size(); i++){
      System.out.print(listOfPoints.get(i));
      System.out.print('\n');
    }
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
    //listOfPoints.add(definingPoint1);
    recursiveIter(tolerance, line1, line2, listOfPoints);
    //listOfPoints.add(definingPoint2);
    return listOfPoints;
  }

  public Point calcLineIntersection(InfBufLine line1, InfBufLine line2){
    if(line1.equals(line2)){
      //should really throw an exception here
      //what should I return here??? *****
      return ctx.makePoint(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
    if(Double.isInfinite(line1.getSlope())){
      double X = line1.getIntercept();
      double Y = line2.getSlope()*X + line2.getIntercept();
      return new PointImpl(X, Y, ctx);
    }else if(Double.isInfinite(line2.getSlope())){
      double X = line2.getIntercept();
      double Y = line1.getSlope()*X + line1.getIntercept();
      return new PointImpl(X, Y, ctx);
    }else if(Double.isInfinite(line1.getSlope()) && Double.isInfinite(line2.getSlope())){
      //Should throw an exception here
      return ctx.makePoint(Double.NaN, Double.NaN);
    }
    double X = (line2.getIntercept() - line1.getIntercept())/(line1.getSlope()-line2.getSlope());
    double Y = line1.getSlope()*X + line1.getIntercept();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(InfBufLine line){
    double radius = circ.getRadius();
    double theta = Math.atan(line.getSlope());
    double X = (radius*Math.cos(theta)) + circ.getCenter().getX();
    double Y = (radius*Math.sin(theta)) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(Point point){
    double radius = circ.getRadius();
    double slope = calcSlope(circ.getCenter(), point);
    double theta = Math.atan(slope);
    double X = radius*Math.cos(theta) + circ.getCenter().getX();
    double Y = radius*Math.sin(theta) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  public InfBufLine calcTangentLine(Point pt){
    return new InfBufLine(getPerpSlope(calcSlope(circ.getCenter(), pt)), pt, 0);
  }

  public double calcSlope(Point P1, Point P2){
    double changeInY = P2.getY()-P1.getY();
    double changeInX = P2.getX()-P1.getX();
    if(changeInX == 0){
      return Double.NaN;
    }
    return changeInY/changeInX;
  }

  public double getPerpSlope(double slope){
    if(Double.isInfinite(slope)){
      return 0;
    }else if(slope == 0){
      return Double.POSITIVE_INFINITY;
    }
    return -1/slope;
  }

  public void recursiveIter(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> listOfPoints){
    System.out.print(line1);
    System.out.print(line2);
    System.out.print('\n');
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

//  to create a BufferedLine from a point and slope
//  public BufferedLine createBufferedLine(Point point, double slope, double buff, SpatialContext ctx){
//    double interceptYvalue = point.getY() - (slope*point.getX());
//    Point interceptPoint = ctx.makePoint(0, interceptYvalue);
//    if(equals(interceptPoint, point)){
//      //get an alternative point at x = 1
//      //because we know this cannot be the same point as the intercept point
//      //since x=0 at the intercept point
//      double alternativeYvalue = slope + interceptYvalue;
//      Point alternativePoint = ctx.makePoint(1, alternativeYvalue);
//      return new BufferedLine(point, alternativePoint, 0, ctx);
//    }
//    return new BufferedLine(point, interceptPoint, 0, ctx);
//  }


}