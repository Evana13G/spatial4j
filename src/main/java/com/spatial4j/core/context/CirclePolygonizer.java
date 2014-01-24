package com.spatial4j.core.context;


import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.*;


/**
 * Created by egizzi on 12/23/13.
 */
public class CirclePolygonizer {

  public static void main(String[] args) {
/* Cartesian Circle Test*/
//    SpatialContext ctx_cartesian_test = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(-90, 90, -90, 90, null));
//    Circle circle_cartesian_test = ctx_cartesian_test.makeCircle(50.0, 50.0, 10.0);
//    CirclePolygonizer CirclePolygonizerObj_cartesian_test = new CirclePolygonizer(ctx_cartesian_test, circle_cartesian_test);
//    List<Point> resultPoints_cartesian_test = CirclePolygonizerObj_cartesian_test.getEnclosingPolygon(0.01);

/* Geodetic Circle Test*/
    SpatialContext ctx_geodetic_test = new SpatialContext(true, null, null);
    Circle circle_geodetic_test = (CircleImpl)(new GeoCircle(ctx_geodetic_test.makePoint(100, 70), 10, ctx_geodetic_test));
    CirclePolygonizer CirclePolygonizerObj_geodetic_test = new CirclePolygonizer(ctx_geodetic_test, circle_geodetic_test);
    List<Point> resultPoints_geodetic_test = CirclePolygonizerObj_geodetic_test.getEnclosingPolygon(0.1);

  }

  protected SpatialContext ctx;
  protected Circle circ;
  protected Point center;
  protected Point axialCenter;

  public CirclePolygonizer(SpatialContext ctx, Circle circ){
    this.ctx = ctx;
    this.circ = circ;
    this.center = circ.getCenter();
    this.axialCenter = ctx.makePoint(center.getX(), ((CircleImpl) circ).getYAxis());
  }

  public List<Point> getEnclosingPolygon(double tolerance){

    double xCoor1 = center.getX();
    double yCoor1 = center.getY()+circ.getRadius();
    double xCoor2 = center.getX()+circ.getRadius();
    double yCoor2 = center.getY();

    if (ctx.isGeo()){
      xCoor2 = circ.getBoundingBox().getMaxX();
      yCoor2 = axialCenter.getY();
    }

    Point definingPoint1 = ctx.makePoint(xCoor1, yCoor1);
    Point definingPoint2 = ctx.makePoint(xCoor2, yCoor2);

    InfBufLine line1 = new InfBufLine (0.0, definingPoint1, 0);
    InfBufLine line2 = new InfBufLine (Double.POSITIVE_INFINITY, definingPoint2, 0);

    ArrayList<Point> resultPoints = new ArrayList<Point>();
    resultPoints.add(definingPoint1);
    recursiveIter(tolerance, line1, line2, resultPoints, Math.PI/4, Math.PI/8);
    resultPoints.add(definingPoint2);

    translatePoints(resultPoints);
    //printForGrapherMercator(resultPoints);

    return resultPoints;
  }

  protected void recursiveIter(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> resultPoints, double angle, double plusMinusAngle){
//    System.out.print(Math.toDegrees(angle));
//    System.out.print(", ");
//    System.out.print(Math.toDegrees(plusMinusAngle));
//    System.out.print('\n');
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersection(lineIntersectionPoint, angle);
    double currentMaxDistance;
    currentMaxDistance = Math.abs(ctx.getDistCalc().distance(center, lineIntersectionPoint) - circ.getRadius());
    System.out.print(currentMaxDistance);
    System.out.print('\n');
    if (currentMaxDistance <= tolerance){
      resultPoints.add(lineIntersectionPoint);
    } else {
      InfBufLine line3 = calcTangentLine(circleIntersectionPoint);
      recursiveIter(tolerance, line1, line3, resultPoints, angle-plusMinusAngle, plusMinusAngle/2);
      resultPoints.add(circleIntersectionPoint);
      recursiveIter(tolerance, line3, line2,  resultPoints, angle+plusMinusAngle, plusMinusAngle/2);
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
  protected Point calcCircleIntersection(Point point, double angle){
    double radius = circ.getRadius();
    double slope = calcSlope(center, point);
    double theta = Math.atan(slope);
    double bearing = 0;
    if(ctx.isGeo()){
      bearing = Math.toDegrees(angle);
    }else{
      Math.toDegrees((Math.PI / 2) - theta);
    }

    Point intersectionPoint;

    if(ctx.isGeo()){
      intersectionPoint = ctx.getDistCalc().pointOnBearing(center, radius, bearing, ctx, null);
    }
    else{
      intersectionPoint = ctx.getDistCalc().pointOnBearing(center, radius, bearing, ctx, null);
    }
    return intersectionPoint;
  }

  //must be given a point on the circle
  protected InfBufLine calcTangentLine(Point pt){

    double epsilon = 1E-12;
    double x = pt.getX()-center.getX();
    double y = pt.getY()-center.getY();
    double radius = circ.getRadius();
    double radiusSquared = radius*radius;
    assert ((x*x + y*y < radiusSquared+epsilon) &&
        (x*x + y*y > radiusSquared-epsilon)) : "Point is not tangent to circle";

    //Two point approximation
//    double theta = Math.atan(y/x);
//    Point point1 = ctx.getDistCalc().pointOnBearing(center, circ.getRadius(), Math.toDegrees(theta)+1, ctx, null);
//    Point point2 = ctx.getDistCalc().pointOnBearing(center, circ.getRadius(), Math.toDegrees(theta)-1, ctx, null);
//    double slope = calcSlope(point1, point2);
//    Point upperRight = ctx.makePoint(point2.getX(), point2.getY()+(point1.getY()-point2.getY()));
//    return new InfBufLine(slope, upperRight, 0);

    if(ctx.isGeo()){
      return new InfBufLine(skewCartesianSlope(getPerpSlope(calcSlope(center, pt)), pt), pt, 0);
    }
    return new InfBufLine(getPerpSlope(calcSlope(center, pt)), pt, 0);
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
    if (ctx.isGeo()){
      //reflect('y', center, false, false, resultPoints);
    }else{
      reflect('x', center, true, false, resultPoints);
      reflect('y', center, false, false, resultPoints);
    }
  }

  public void reflect(char axisToReflectOver, Point axesIntersectionPoint, boolean inclusiveStartPt, boolean inclusiveEndPt, List <Point> resultPoints){
    double x;
    double y;
    double xBound = axesIntersectionPoint.getX();
    double yBound = axesIntersectionPoint.getY();

    int lstSize = resultPoints.size();
    int leadingOffset = (inclusiveStartPt) ? 0 : 1;
    int trailingOffset = (inclusiveEndPt) ? 1 : 2;

    if(axisToReflectOver == 'x'){
      for(int i=lstSize-trailingOffset;i>=leadingOffset; i--){
        x = (resultPoints.get(i).getX());
        y =  yBound - (resultPoints.get(i).getY()-yBound);
        Point point = ctx.makePoint(x, y);
        resultPoints.add(point);
      }
    }else if(axisToReflectOver == 'y'){
      for(int i=lstSize-trailingOffset;i>=leadingOffset; i--){
        x =  xBound - (resultPoints.get(i).getX()-xBound);
        y = (resultPoints.get(i).getY());
        Point point = ctx.makePoint(x, y);
        resultPoints.add(point);
      }
    }
  }

  public void printListOfPoints(List <Point> resultPoints){
    for(int i=0;i<resultPoints.size(); i++){
      System.out.print(resultPoints.get(i));
      System.out.print('\n');
    }
  }

  public void printForGrapher(List <Point> resultPoints){
    for(int i=0;i<resultPoints.size(); i++){
      System.out.print(resultPoints.get(i).getX());
      System.out.print(',');
      System.out.print(resultPoints.get(i).getY());
      System.out.print(':');
    }
  }

  public void printForGrapherMercator(List <Point> resultPoints){
    for(int i=0;i<resultPoints.size(); i++){
      Point pt = mercatorProjection(resultPoints.get(i));
      System.out.print(pt.getX());
      System.out.print(',');
      System.out.print(pt.getY());
      System.out.print(':');
    }
  }

  //***************************************************************************************************//
  //*******************************Functions that may or may not be used*******************************//
  //***************************************************************************************************//


  //true = clockwise, false = counter-clockwise
  public double skewCartesianSlope(double slope, Point pt){
    double skew = 1/(DistanceUtils.calcLonDegreesAtLat(pt.getY(), 1));
    boolean direction = (pt.getX() > center.getX())? true : false;
    double angle;
    if(Double.isInfinite(slope)){
      angle = (Math.PI/2)*skew;
      if(direction == true){
        return Math.tan(angle);
      }else{
        return Math.tan(Math.PI-angle);
      }
    }else if(slope == 0){
      return 0;
    }
    return slope*skew;
  }

  protected void recursiveIterAngles(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> resultPoints, double angle, double plusMinusValue){
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersectionAngles(angle);
    double currentMaxDistance;
    currentMaxDistance = (ctx.getDistCalc().distance(center, lineIntersectionPoint) - circ.getRadius());
    if (currentMaxDistance <= tolerance){
      resultPoints.add(lineIntersectionPoint);
    } else {
      InfBufLine line3 = calcTangentLine(circleIntersectionPoint);
      recursiveIterAngles(tolerance, line1, line3, resultPoints, angle - plusMinusValue, plusMinusValue / 2);
      resultPoints.add(circleIntersectionPoint);
      recursiveIterAngles(tolerance, line3, line2, resultPoints, angle + plusMinusValue, plusMinusValue / 2);
    }
  }

  protected Point calcCircleIntersectionAngles(double angle){
    return ctx.getDistCalc().pointOnBearing(center, circ.getRadius(), Math.toDegrees(angle), ctx, null);
  }

 // Equation of Circle: (x-centerX)^2 + (y-centerY)^2 = R^2
  protected Point mercatorProjection(Point pointLonLat){
//source: http://stackoverflow.com/questions/14329691/covert-latitude-longitude-point-to-a-pixels-x-y-on-mercator-projection
    double latitude = pointLonLat.getY();
    double longitude = pointLonLat.getX();
    double mapWidth = 100;
    double mapHeight = 100;

    double x = (longitude+180)*(mapWidth/360);
    double latRad = latitude* Math.PI/180;
    double mercN = Math.log(Math.tan( (Math.PI/4) + (latRad/2)) );
    double y = (mapHeight/2) - (mapWidth*mercN/(2*Math.PI));
    return ctx.makePoint(x, y);
  }

//to be placed in main to handle quadrant 4
    /*HANDLE QUADRANT 1*/
//    ArrayList<Point> resultPoints = new ArrayList<Point>();
//    resultPoints.add(definingPoint1);
//    recursiveIter(tolerance, line1, line2, resultPoints, Math.PI/4, Math.PI/8);
//    resultPoints.add(definingPoint2);

    /*HANDLE QUADRANT 4*/
//    double xCoor3 = xCoor1;
//    //double yCoor3 = center.getY() - (yCoor1 - center.getY());
//    double yCoor3 = circ.getBoundingBox().getMinY();
//    Point definingPoint3 = ctx.makePoint(xCoor3, yCoor3);
//    InfBufLine line3 = new InfBufLine (0.0, definingPoint3, 0);
//    ArrayList<Point> resultPointsQuad4 = new ArrayList<Point>();
//    recursiveIter(tolerance, line2, line3, resultPointsQuad4, Math.PI*3/4, Math.PI/8);
//    resultPointsQuad4.add(definingPoint3);
//
//    //combine the lists of Q1 and Q4 points
//    int resultListSize = resultPointsQuad4.size();
//    for(int i=0; i<resultListSize; i++){
//      resultPoints.add(resultPointsQuad4.get(i));
//    }


}