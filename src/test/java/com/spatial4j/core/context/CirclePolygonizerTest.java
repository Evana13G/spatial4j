package com.spatial4j.core.context;


import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.RandomizedShapeTest;
import com.spatial4j.core.shape.impl.InfBufLine;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by egizzi on 12/27/13.
*/
public class CirclePolygonizerTest extends RandomizedShapeTest{

  public static final double EPS = 0.00001;
  protected SpatialContext ctx;
  protected CirclePolygonizer polygonizer;
  protected Circle circ;

  //constructor for test class?
  public CirclePolygonizerTest() {
    ctx = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(0, 100, 0, 100, null));
    circ = ctx.makeCircle(50, 50, 10);
    polygonizer = new CirclePolygonizer(ctx, circ, false);
  }

  @Test
  public void testGetEnclosingPolygon(){
    double tolerance = 20; //only want one iteration of recursion
    ArrayList<Point> resultPoints = new ArrayList<Point>();

    resultPoints.add(ctx.makePoint(50.0,60.0));
    resultPoints.add(ctx.makePoint(60.0,60.0));
    resultPoints.add(ctx.makePoint(60.0,50.0));
    resultPoints.add(ctx.makePoint(60.0,40.0));
    resultPoints.add(ctx.makePoint(50.0,40.0));
    resultPoints.add(ctx.makePoint(40.0,40.0));
    resultPoints.add(ctx.makePoint(40.0,50.0));
    resultPoints.add(ctx.makePoint(40.0,60.0));

    List <Point> testResultPoints = polygonizer.getEnclosingPolygon(tolerance);
    assertEquals(resultPoints, testResultPoints);
  }

  @Test
  public void testCalcLineIntersection(){
    Point point1 = ctx.makePoint(0,0);
    Point point2 = ctx.makePoint(100, 0);

    InfBufLine line1 = new InfBufLine(1, point1, 0);
    InfBufLine line2 = new InfBufLine(-1, point2, 0);
    assertEquals(ctx.makePoint(50, 50), polygonizer.calcLineIntersection(line1, line2));

    InfBufLine line3 = new InfBufLine(Double.POSITIVE_INFINITY, point1, 0);
    InfBufLine line4 = new InfBufLine(Double.POSITIVE_INFINITY, point2, 0);
    // Make sure it throws and exception

    point1.reset(25, 0);
    point2.reset(75, 0);

    InfBufLine line5 = new InfBufLine(1, point1, 0);
    InfBufLine line6 = new InfBufLine(1, point2, 0);
    // Make sure it throws and exception


    InfBufLine line7 = new InfBufLine(1, point1, 0);
    InfBufLine line8 = new InfBufLine(Double.POSITIVE_INFINITY, point2, 0);
    assertEquals(ctx.makePoint(75, 50), polygonizer.calcLineIntersection(line7, line8));

  }

  @Test
  public void testCalcCircleIntersection(){
    //Test with a point from all four quadrants
    Point point1 = ctx.makePoint(100,100);
    assertEquals(ctx.makePoint(57.0710678118654755,57.0710678118654755), polygonizer.calcCircleIntersection(point1));

    point1.reset(100, 0);
    assertEquals(ctx.makePoint(57.0710678118654755,42.928932188134524), polygonizer.calcCircleIntersection(point1));
  }

  @Test
  public void testCalcTangentLine(){
    //given the argument of a point on the circle
    //(passed a point found using 'calcCircleIntersection')
    //need to test for any random pt on the circle
    Point pt = ctx.makePoint(50, 60);

    InfBufLine expectedTangentLine = new InfBufLine(0, pt, 0);
    InfBufLine actualTangentLine = polygonizer.calcTangentLine(pt);

    assertEquals(expectedTangentLine.getSlope(), actualTangentLine.getSlope(), EPS);
    assertEquals(expectedTangentLine.getIntercept(), actualTangentLine.getIntercept(), EPS);

    pt.reset(57.0710678118654755,57.0710678118654755);
    InfBufLine expectedTangentLine2 = new InfBufLine(-1, pt, 0);
    InfBufLine actualTangentLine2 = polygonizer.calcTangentLine(pt);

    assertEquals(expectedTangentLine2.getSlope(), actualTangentLine2.getSlope(), EPS);
    assertEquals(expectedTangentLine2.getIntercept(), actualTangentLine2.getIntercept(), EPS);
  }

  @Test
  public void testCalcSlope(){
    Point point1 = ctx.makePoint(0,0);
    Point point2 = ctx.makePoint(50, 50);
    assertEquals(1, polygonizer.calcSlope(point1, point2), EPS);

    point2.reset(50, 0);
    assertEquals(0, polygonizer.calcSlope(point1, point2), EPS);

    point2.reset(0, 50);
    assertEquals(Double.POSITIVE_INFINITY, polygonizer.calcSlope(point1, point2), EPS);

    point2.reset(0, 0);

    try {
      polygonizer.calcSlope(point1, point2);
      fail();
    } catch (IllegalArgumentException e) {
      //expected
    }
  }

  @Test
  public void testRecursiveIter(){
    double tolerance = 20;
    InfBufLine line1 = new InfBufLine(0, ctx.makePoint(50,60), 0);
    InfBufLine line2 = new InfBufLine(Double.POSITIVE_INFINITY, ctx.makePoint(60,50), 0);
    ArrayList<Point> resultPoints = new ArrayList<Point>();
    resultPoints.add(ctx.makePoint(60.0,60.0));
    ArrayList<Point> testResultPoints = new ArrayList<Point>();
    polygonizer.recursiveIter(tolerance, line1, line2, testResultPoints);

    assertEquals(resultPoints, testResultPoints);

    tolerance = 0.1;
    resultPoints.clear();
    testResultPoints.clear();
    resultPoints.add(ctx.makePoint(50.0,60.0));
    resultPoints.add(ctx.makePoint(60.0,60.0));
    resultPoints.add(ctx.makePoint(57.0710678118654755,57.0710678118654755));
    resultPoints.add(ctx.makePoint(60.0,60.0));
    resultPoints.add(ctx.makePoint(60.0,50.0));
    polygonizer.recursiveIter(tolerance, line1, line2, testResultPoints);

    //assertEquals(resultPoints, testResultPoints);
  }

  @Test
  public void testIsTrueTangent(){
    double DISTANCE = 0.1;

    Point point = ctx.makePoint(randomIntBetween(51, 99), randomIntBetween(51, 99));
    Point tangentPoint = polygonizer.calcCircleIntersection(point);
    InfBufLine tangentLine = polygonizer.calcTangentLine(tangentPoint);

    List <Point> pointsToTest = getCoordinatesGivenDistance(DISTANCE, tangentLine);
    Point point1 = pointsToTest.get(0);
    Point point2 = pointsToTest.get(1);

    //System.out.print(pointsToTest);

    assertTrue(isOutsideCircle(point1));
    assertTrue(isOutsideCircle(point2));
    assertFalse(isOutsideCircle(tangentPoint));

    double xPos = tangentPoint.getX() + DISTANCE;
    double xNeg = tangentPoint.getX() - DISTANCE;
    double yPos = tangentLine.getSlope()*xPos + tangentLine.getIntercept();
    double yNeg = tangentLine.getSlope()*xNeg + tangentLine.getIntercept();

    assertTrue(isOutsideCircle(ctx.makePoint(xPos, yPos)));
    assertTrue(isOutsideCircle(ctx.makePoint(xNeg, yNeg)));
    assertFalse(isOutsideCircle(tangentPoint));

    tangentPoint.reset(50, 60);
    InfBufLine tangentLine1 = polygonizer.calcTangentLine(tangentPoint);
    xPos = tangentPoint.getX()+DISTANCE;
    xNeg = tangentPoint.getX()-DISTANCE;
    yPos = tangentLine1.getSlope()*xPos + tangentLine1.getIntercept();
    yNeg = tangentLine1.getSlope()*xNeg + tangentLine1.getIntercept();

    assertTrue(isOutsideCircle(ctx.makePoint(xPos, yPos)));
    assertTrue(isOutsideCircle(ctx.makePoint(xNeg, yNeg)));
    assertFalse(isOutsideCircle(tangentPoint));

    tangentPoint.reset(60, 50);
    InfBufLine tangentLine2 = polygonizer.calcTangentLine(tangentPoint);
    xPos = tangentLine2.getIntercept();
    xNeg = xPos;
    yPos = tangentPoint.getX()+DISTANCE;
    yNeg = tangentPoint.getX()-DISTANCE;

    assertTrue(isOutsideCircle(ctx.makePoint(xPos, yPos)));
    assertTrue(isOutsideCircle(ctx.makePoint(xNeg, yNeg)));
    assertFalse(isOutsideCircle(tangentPoint));
  }

  public boolean isOutsideCircle(Point point){
    double epsilon = 3E-15;
//    double epsilon = .000000000000003;
    double radius = circ.getRadius();
    double pointDistanceFromCenter = ctx.getDistCalc().distance(circ.getCenter(), point)-epsilon;
    if(pointDistanceFromCenter > radius){
      return true;
    }
    return false;
  }

  public List<Point> getCoordinatesGivenDistance(double distance, InfBufLine line){

    double radius = circ.getRadius();
    double centerToPoint = Math.sqrt(distance*distance + radius*radius);

    double theta = Math.atan(polygonizer.getPerpSlope(line.getSlope()));
    double thetaShift = Math.atan(distance/radius);
    double thetaSup = theta + thetaShift;
    double thetaInf = theta - thetaShift;

    double x1 = centerToPoint*Math.cos(thetaSup) + circ.getCenter().getX();
    double y1 = line.getSlope()*x1 + line.getIntercept();
    double x2 = centerToPoint*Math.cos(thetaInf) + circ.getCenter().getX();
    double y2 = line.getSlope()*x2 + line.getIntercept();

    ArrayList<Point> resultPoints = new ArrayList<Point>();
    resultPoints.add(ctx.makePoint(x1, y1));
    resultPoints.add(ctx.makePoint(x2, y2));
    return resultPoints;
  }

}

