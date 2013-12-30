package com.spatial4j.core.context;


import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.CartesianLine;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.RandomizedShapeTest;
import com.spatial4j.core.shape.impl.CartesianLineImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    ctx = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(0, 100, 200, 300, null));
    polygonizer = new CirclePolygonizer(ctx);
    circ = ctx.makeCircle(50, 250, 10);
  }

  @Test
  public void testCalcLineIntersection(){
    Point point1 = ctx.makePoint(0,200);
    Point point2 = ctx.makePoint(100, 200);
    CartesianLine line1 = new CartesianLineImpl(1, point1, ctx);
    CartesianLine line2 = new CartesianLineImpl(-1, point2, ctx);
    assertEquals(50, polygonizer.calcLineIntersection(line1, line2).getX(), EPS);
    assertEquals(250, polygonizer.calcLineIntersection(line1, line2).getY(), EPS);
  }
//  public void testGetEnclosingPolygon(){;}

  //test for constructor ?
  //test for both methods
//  public void testCalcCircleIntersection(){;}
//  public void testCalcTangentLine(){;}
//  public void testCalcSlope(){;}
//  public void testRecursiveIter(){;}


}
















