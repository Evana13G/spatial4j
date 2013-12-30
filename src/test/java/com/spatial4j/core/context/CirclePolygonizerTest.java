package com.spatial4j.core.context;


import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.CartesianLine;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.RandomizedShapeTest;
import com.spatial4j.core.shape.impl.CartesianLineImpl;
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
  public void testCalcLineIntersection(){
    Point point1 = ctx.makePoint(0,0);
    Point point2 = ctx.makePoint(100, 0);
    CartesianLine line1 = new CartesianLineImpl(1, point1, ctx);
    CartesianLine line2 = new CartesianLineImpl(-1, point2, ctx);
    assertEquals(50, polygonizer.calcLineIntersection(line1, line2).getX(), EPS);
    assertEquals(50, polygonizer.calcLineIntersection(line1, line2).getY(), EPS);
  }

  @Test
  public void testGetEnclosingPolygon(){
    double tolerance = 200;
    Point definingPoint1 = ctx.makePoint(50, 60);
    Point definingPoint2 = ctx.makePoint(60, 50);
    CartesianLine line_1 = new CartesianLineImpl (0.0, definingPoint1, this.ctx);
    CartesianLine line_2 = new CartesianLineImpl (10000000.0, definingPoint2, this.ctx);
    ArrayList<Point> listOfPoints = new ArrayList<Point>();

//    listOfPoints.add(ctx.makePoint(54.142135916624156,60.0));
//    listOfPoints.add(ctx.makePoint(57.071068165418836,57.07106745831209));
//    listOfPoints.add(ctx.makePoint(60.00000041421349,54.142134916624215));


    listOfPoints.add(ctx.makePoint(50.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,60.0));
    listOfPoints.add(ctx.makePoint(60.0,50.0));

    ArrayList<Point> testListOfPoints = new ArrayList<Point>();
    polygonizer.recursiveIter(tolerance, line_1, line_2, testListOfPoints);
    assertEquals(listOfPoints, testListOfPoints);

  }


//  public void testCalcCircleIntersection(){;}
//  public void testCalcTangentLine(){;}
//  public void testCalcSlope(){;}
//  public void testRecursiveIter(){;}


}
















