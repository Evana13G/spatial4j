package com.spatial4j.core.context;


import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.RandomizedShapeTest;
import com.spatial4j.core.shape.impl.InfBufLine;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Test;

import java.util.ArrayList;

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
    polygonizer = new CirclePolygonizer(ctx, circ);
  }

  @Test
  public void testGetEnclosingPolygon(){
    double tolerance = 200;
    Point definingPoint1 = ctx.makePoint(50, 60);
    Point definingPoint2 = ctx.makePoint(60, 50);

    InfBufLine line1 = new InfBufLine(0.0, definingPoint1, 0);
    InfBufLine line2 = new InfBufLine(Double.POSITIVE_INFINITY, definingPoint2, 0);

    ArrayList<Point> listOfPoints = new ArrayList<Point>();

//    listOfPoints.add(ctx.makePoint(54.142135916624156,60.0));
//    listOfPoints.add(ctx.makePoint(57.071068165418836,57.07106745831209));
//    listOfPoints.add(ctx.makePoint(60.00000041421349,54.142134916624215));

    listOfPoints.add(ctx.makePoint(50.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,50.0));

    ArrayList<Point> testListOfPoints = new ArrayList<Point>();
    polygonizer.recursiveIter(tolerance, line1, line2, testListOfPoints);
//    assertEquals(listOfPoints, testListOfPoints);
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
    assertEquals(ctx.makePoint(Double.NaN, Double.NaN), polygonizer.calcLineIntersection(line3, line4));

    point1.reset(25, 0);
    point2.reset(75, 0);

    InfBufLine line5 = new InfBufLine(1, point1, 0);
    InfBufLine line6 = new InfBufLine(1, point2, 0);
    assertEquals(ctx.makePoint(Double.NaN, Double.NaN), polygonizer.calcLineIntersection(line5, line6));


    InfBufLine line7 = new InfBufLine(1, point1, 0);
    InfBufLine line8 = new InfBufLine(Double.POSITIVE_INFINITY, point2, 0);
    assertEquals(ctx.makePoint(75, 50), polygonizer.calcLineIntersection(line7, line8));

  }

  @Test
  public void testCalcCircleIntersection(){

    //need to test calcCircleIntersection with a line as an arg?
    //Might wipe that function

    //Test with a point from all four quadrants
    Point point1 = ctx.makePoint(100,100);
    assertEquals(ctx.makePoint(57.0710678118654755,57.0710678118654755), polygonizer.calcCircleIntersection(point1));

    point1.reset(100, 0);
    assertEquals(ctx.makePoint(57.0710678118654755,42.928932188134524), polygonizer.calcCircleIntersection(point1));

    point1.reset(0,0);
    assertEquals(ctx.makePoint(42.928932188134524,42.928932188134524), polygonizer.calcCircleIntersection(point1));

    point1.reset(0,100);
    assertEquals(ctx.makePoint(42.928932188134524,57.0710678118654755), polygonizer.calcCircleIntersection(point1));

  }

  //@Test
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

    pt.reset(42.928932188134524,57.0710678118654755);
    InfBufLine expectedTangentLine3 = new InfBufLine(-1, pt, 0);
    InfBufLine actualTangentLine3 = polygonizer.calcTangentLine(pt);

    assertEquals(expectedTangentLine3.getSlope(), actualTangentLine3.getSlope(), EPS);
    assertEquals(expectedTangentLine3.getIntercept(), actualTangentLine3.getIntercept(), EPS);

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
    assertEquals(Double.NaN, polygonizer.calcSlope(point1, point2), EPS);
  }

  @Test
  public void testRecursiveIter(){
    double tolerance = 1;
    InfBufLine line1 = new InfBufLine(0, ctx.makePoint(50,60), 0);
    InfBufLine line2 = new InfBufLine(10000000, ctx.makePoint(60,50), 0);
    ArrayList<Point> listOfPoints = new ArrayList<Point>();
    listOfPoints.add(ctx.makePoint(50.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,50.0));
    ArrayList<Point> testListOfPoints = new ArrayList<Point>();
    polygonizer.recursiveIter(tolerance, line1, line2, testListOfPoints);

    //assertEquals(listOfPoints, testListOfPoints);

    tolerance = 0.1;
    listOfPoints.clear();
    testListOfPoints.clear();
    listOfPoints.add(ctx.makePoint(50.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,60.0));
    listOfPoints.add(ctx.makePoint(57.0710678118654755,57.0710678118654755));
    listOfPoints.add(ctx.makePoint(60.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,50.0));
    polygonizer.recursiveIter(tolerance, line1, line2, testListOfPoints);

    //assertEquals(listOfPoints, testListOfPoints);
  }
}
