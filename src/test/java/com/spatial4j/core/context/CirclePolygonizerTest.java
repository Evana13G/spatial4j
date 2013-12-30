package com.spatial4j.core.context;


import com.spatial4j.core.context.jts.CirclePolygonizer;
import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.CartesianLine;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.CartesianLineImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;

import static org.junit.Assert.assertEquals;

/**
 * Created by egizzi on 12/27/13.
 */
public class CirclePolygonizerTest {

  public static void main(String[] args){
    CirclePolygonizerTest test = new CirclePolygonizerTest();
    SpatialContext ctx = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(0, 100, 200, 300, null));
    test.testCalcLineIntersection(ctx);
  }

  public CirclePolygonizerTest(){;}

  //function stubs
  public void testCalcLineIntersection(SpatialContext ctx){
    CirclePolygonizer polygonizer = new CirclePolygonizer(ctx);
    Point point1 = ctx.makePoint(0,200);
    Point point2 = ctx.makePoint(100, 200);
    CartesianLine line1 = new CartesianLineImpl(1, point1, ctx);
    CartesianLine line2 = new CartesianLineImpl(-1, point2, ctx);
    assertEquals(50, polygonizer.calcLineIntersection(line1, line2).getX(), 0.000001);
    assertEquals(250, polygonizer.calcLineIntersection(line1, line2).getY(), 0.000001);
  }

  //test for constructor ?

  //test for both methods
  public void testCalcCircleIntersection(){;}
  public void testCalcTangentLine(){;}
  public void testCalcSlope(){;}
  public void testRecursiveIter(){;}


}
