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
    CirclePolygonizer CirclePolygonizerObj = new CirclePolygonizer(ctx, circle, true);

    //List<Point> resultPoints = CirclePolygonizerObj.getEnclosingPolygon(20);

    ArrayList <Point> test = new ArrayList<Point>();
    test.add(ctx.makePoint(50, 250));
    test.add(ctx.makePoint(40, 260));

    List<Point> resultPoints = CirclePolygonizerObj.reflect('x', false, false, test);
  }

  protected SpatialContext ctx;
  protected Circle circ;
  protected boolean isGeo;

  public CirclePolygonizer(SpatialContext ctx, Circle circ, boolean isGeo){
    this.ctx = ctx;
    this.circ = circ;
    this.isGeo = isGeo;
  }

  public List<Point> getEnclosingPolygon(double tolerance){

    double xCoor1 = circ.getCenter().getX();
    double yCoor1 = circ.getCenter().getY()+circ.getRadius();
    double xCoor2 = circ.getCenter().getX()+circ.getRadius();
    double yCoor2 = circ.getCenter().getY();

    Point definingPoint1 = ctx.makePoint(xCoor1, yCoor1);
    Point definingPoint2 = ctx.makePoint(xCoor2, yCoor2);

    InfBufLine line1 = new InfBufLine (0.0, definingPoint1, 0);
    InfBufLine line2 = new InfBufLine (Double.POSITIVE_INFINITY, definingPoint2, 0);

    ArrayList<Point> resultPoints = new ArrayList<Point>();
    resultPoints.add(definingPoint1);
    recursiveIter(tolerance, line1, line2, resultPoints);
    resultPoints.add(definingPoint2);

    translatePoints(resultPoints);

    printListOfPoints(resultPoints);

    return resultPoints;
  }

  protected void recursiveIter(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> resultPoints){
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersection(lineIntersectionPoint);
    double currentMaxDistance = ctx.getDistCalc().distance(circleIntersectionPoint, lineIntersectionPoint);
    if (currentMaxDistance <= tolerance){
      resultPoints.add(lineIntersectionPoint);
    } else {
      InfBufLine line3 = calcTangentLine(circleIntersectionPoint);
      recursiveIter(tolerance, line1, line3, resultPoints);
      resultPoints.add(circleIntersectionPoint);
      recursiveIter(tolerance, line3, line2,  resultPoints);
    }
  }

  protected Point calcLineIntersection(InfBufLine line1, InfBufLine line2){
    if(line1.equals(line2)){
      throw new IllegalArgumentException("Cannot calculate intersection point of two equivalent lines");
    } else if(line1.getSlope() == line2.getSlope()){
      //Should throw an exception here
      throw new IllegalArgumentException("Cannot calculate intersection point of two parallel lines");
    }else if(Double.isInfinite(line1.getSlope())){
      double x = line1.getIntercept();
      double y = line2.getSlope()*x + line2.getIntercept();
      return new PointImpl(x, y, ctx);
    }else if(Double.isInfinite(line2.getSlope())){
      double x = line2.getIntercept();
      double y = line1.getSlope()*x + line1.getIntercept();
      return new PointImpl(x, y, ctx);
    }else{
      double x = (line2.getIntercept() - line1.getIntercept())/(line1.getSlope()-line2.getSlope());
      double y = line1.getSlope()*x + line1.getIntercept();
      return new PointImpl(x, y, ctx);
    }
  }

  //assumed that point is outside circle
  protected Point calcCircleIntersection(Point point){
    double radius = circ.getRadius();
    double slope = calcSlope(circ.getCenter(), point);
    double theta = Math.atan(slope);
    double x = radius*Math.cos(theta) + circ.getCenter().getX();
    double y = radius*Math.sin(theta) + circ.getCenter().getY();
    return new PointImpl(x, y, ctx);
  }

  //must be given a point on the circle
  protected InfBufLine calcTangentLine(Point pt){
    double epsilon = 1E-12;
    double x = pt.getX()-circ.getCenter().getX();
    double y = pt.getY()-circ.getCenter().getY();
    double radius = circ.getRadius();
    double radiusSquared = radius*radius;
    assert ((x*x + y*y < radiusSquared+epsilon) &&
        (x*x + y*y > radiusSquared-epsilon)) : "Point is not tangent to circle";
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

  protected void translatePoints(List <Point> resultPoints){
    double xBound = circ.getCenter().getX();
    double yBound = circ.getCenter().getY();
    double x;
    double y;

    int lstSize = resultPoints.size();
    for(int i=lstSize-2;i>=0; i--){
      x = (resultPoints.get(i).getX());
      y =  yBound - (resultPoints.get(i).getY()-yBound);
      Point point = ctx.makePoint(x, y);
      resultPoints.add(point);
    }
    lstSize = resultPoints.size();
    for(int i=lstSize-2;i>0; i--){
      x =  xBound - (resultPoints.get(i).getX()-xBound);
      y = (resultPoints.get(i).getY());
      Point point = ctx.makePoint(x, y);
      resultPoints.add(point);
    }
  }

  public void printListOfPoints(List <Point> resultPoints){
    System.out.print("polygon points\n");
    for(int i=0;i<resultPoints.size(); i++){
      System.out.print(resultPoints.get(i));
      System.out.print('\n');
    }
  }

  public List <Point> reflect(char axis, boolean inclusiveStartPt, boolean inclusiveEndPt, List <Point> pointsToReflect){
    System.out.print(pointsToReflect);
    ArrayList<Point> reflectedPoints = new ArrayList<Point>();
    double xBound = circ.getCenter().getX();
    double yBound = circ.getCenter().getY();
    double x;
    double y;

    int lstSize = pointsToReflect.size();
    int leadingOffset = (inclusiveStartPt) ? 0 : 1;
    int trailingOffset = (inclusiveEndPt) ? 1 : 2;

    if(axis == 'x'){

      for(int i=lstSize-trailingOffset;i>=leadingOffset; i--){

        x = (pointsToReflect.get(i).getX());
        y =  yBound - (pointsToReflect.get(i).getY()-yBound);
        Point point = ctx.makePoint(x, y);
        reflectedPoints.add(point);
      }

    }else if(axis == 'y'){

      for(int i=lstSize-trailingOffset;i>leadingOffset; i--){
        x =  xBound - (pointsToReflect.get(i).getX()-xBound);
        y = (pointsToReflect.get(i).getY());
        Point point = ctx.makePoint(x, y);
        reflectedPoints.add(point);
      }

    }
    printListOfPoints(reflectedPoints);
    return reflectedPoints;
  }

}