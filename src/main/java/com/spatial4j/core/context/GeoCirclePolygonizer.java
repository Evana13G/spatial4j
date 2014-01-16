package com.spatial4j.core.context;

import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.*;

/**
 * Created by egizzi on 1/16/14.
 */
public class GeoCirclePolygonizer {

  public static void main(String[] args) {

    SpatialContext ctx = SpatialContext.GEO;
    Circle circle = new GeoCircle(ctx.makePoint(100, 85), 3, ctx);
    GeoCirclePolygonizer GeoCirclePolygonizerObj = new GeoCirclePolygonizer(ctx, circle);

    List<Point> resultPoints = GeoCirclePolygonizerObj.getEnclosingPolygon(0.001);
  }

  protected final SpatialContext ctx;
  protected final Circle circ;
  protected final Point center;
  protected final Point axialCenter;
  protected final double skew;

  public GeoCirclePolygonizer(SpatialContext ctx, Circle circ){
    this.ctx = ctx;
    this.circ = circ;
    this.center = circ.getCenter();
    this.axialCenter = ctx.makePoint(center.getX(), ((CircleImpl) circ).getYAxis());
    this.skew = DistanceUtils.calcLonDegreesAtLat(center.getY(), 1);
  }

  public List<Point> getEnclosingPolygon(double tolerance){

    double xCoor1 = center.getX();
    double yCoor1 = center.getY()+circ.getRadius();
    double xCoor2 = center.getX()+circ.getRadius();
    double yCoor2 = axialCenter.getY();

    Point definingPoint1 = ctx.makePoint(xCoor1, yCoor1);
    Point definingPoint2 = ctx.makePoint(xCoor2, yCoor2);

    InfBufLine line1 = new InfBufLine (0.0, definingPoint1, 0);
    InfBufLine line2 = new InfBufLine (Double.POSITIVE_INFINITY, definingPoint2, 0);

    ArrayList<Point> resultPoints = new ArrayList<Point>();
    resultPoints.add(definingPoint1);
    recursiveIter(tolerance, line1, line2, resultPoints);
    resultPoints.add(definingPoint2);

    /*HANDLE QUADRANT 4*/
//    System.out.print("Not getting here! \n");
//    if(ctx.isGeo()){
//      double xCoor3 = xCoor1;
//      double yCoor3 = yCoor2 - (yCoor1 - yCoor2);
//      Point definingPoint3 = ctx.makePoint(xCoor3, yCoor3);
//      InfBufLine line3 = new InfBufLine (0.0, definingPoint3, 0);
//      ArrayList<Point> resultPointsQuad4 = new ArrayList<Point>();
//      recursiveIter(tolerance, line2, line3, resultPointsQuad4);
//      resultPointsQuad4.add(definingPoint3);
//
//      //combine the lists of Q1 and Q4 points
//      int resultListSize = resultPointsQuad4.size();
//      for(int i=0; i<resultListSize; i++){
//        resultPoints.add(resultPointsQuad4.get(0));
//      }
//    }

    translatePoints(resultPoints);
    printListOfPoints(resultPoints);

    return resultPoints;
  }

  protected void recursiveIter(double tolerance, InfBufLine line1, InfBufLine line2, List<Point> resultPoints){
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersection(lineIntersectionPoint);
    double currentMaxDistance;
    currentMaxDistance = (ctx.getDistCalc().distance(center, lineIntersectionPoint) - circ.getRadius());
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
    double slope = calcSlope(center, point);
    double theta = Math.atan(slope);
    double bearing = ((Math.PI/2) - theta)*(180/Math.PI);
    if(ctx.isGeo()){bearing = 90 + bearing;};
    Point intersectionPoint = ctx.getDistCalc().pointOnBearing(center, radius, bearing, ctx, null);
    return intersectionPoint;
  }

  //must be given a point on the circle
  protected InfBufLine calcTangentLine(Point pt){
    /* The stretch factor applied to the slope should be the ratio of a unit of distance in latitude over
     * the same unit in longitudes (at the point).  Maybe flip the numerator and denominator.
     * */
    // length long = pi/180 * a (radius of earth) * cos(theta) < theta = degrees lat
    // length lat = pi/180 * a/360
    // a = 6,371 kilometers
    //therefore, lat/long = a/360 / a*cos(theta) = 1/360*cos(theta)

    double skewingFactor = DistanceUtils.calcLonDegreesAtLat(pt.getY(), 1); //dist = 1 ?
    double epsilon = 1E-12;
    double x = pt.getX()-center.getX();
    double y = pt.getY()-center.getY();
    double radius = circ.getRadius();
    double radiusSquared = radius*radius;
    assert ((x*x + y*y < radiusSquared+epsilon) &&
            (x*x + y*y > radiusSquared-epsilon)) : "Point is not tangent to circle";


    //if geodetic, need to calculate a different slope (given two points)
    //double skewingFactor = 1/(360* Math.cos(Math.toRadians(definingPoint2.getY())));
    //InfBufLine line2 = new InfBufLine (skewingFactor, definingPoint2, 0);

    //letting the dist of one degree of lat = 1
    //and dist of one degree of long at that lat = distLon
    double distLon = DistanceUtils.calcLonDegreesAtLat(pt.getY(), 1);
    double skew = distLon;
    //double slope = Math.tan(90 * skew);
    //near pole(85 lat), lat is about 7 times the dist of long

    double slope = getPerpSlope(calcSlope(center, pt));
//    System.out.print('\n');
//    System.out.print("slope: ");
//    System.out.print(slope);
//    slope = slope*skew;
//    System.out.print('\n');
//    System.out.print("skew: ");
//    System.out.print(skew);
//    System.out.print('\n');
//    System.out.print("new slope: ");
//    System.out.print(slope);
    return new InfBufLine(slope, pt, 0);
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
    reflect('y', center, false, false, resultPoints);
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
    System.out.print("\nPolygon Points\n");
    for(int i=0;i<resultPoints.size(); i++){
      System.out.print(resultPoints.get(i));
      System.out.print('\n');
    }
  }
}
