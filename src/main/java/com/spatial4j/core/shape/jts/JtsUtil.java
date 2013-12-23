package com.spatial4j.core.shape.jts;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.CartesianLine;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.CartesianLineImpl;
import com.spatial4j.core.shape.impl.PointImpl;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon; //once return is impl correctly for getEnclosingPolygon

/**
 * Created by egizzi on 12/23/13.
 */
public class JtsUtil {

  protected SpatialContext ctx;
  protected GeometryFactory geom;
  protected Circle circ;

  public JtsUtil(SpatialContext ctx, GeometryFactory GeoFact){
    this.geom = GeoFact;
    this.ctx = ctx;
  }

  //Note: tolerance will eventually be the maximum distance away from circle
  // for now, we will use it as the number of iterations to use
  public void getEnclosingPolygon(double tolerance){
    double RADIUS = circ.getRadius();
    Point CENTER = circ.getCenter();
    CartesianLine line_1 = new CartesianLineImpl (0.0, CENTER, this.ctx);
    CartesianLine line_2 = new CartesianLineImpl (10000.0, CENTER, this.ctx);
    //CartesianLine line_2 = new CartesianLineImpl (Double.POSITIVE_INFINITY, CENTER, this.ctx);

    CartesianLine line_3 = iterate(line_1, line_2);

    //will eventually incorporate iterative behavior
    for(int i=0; i<tolerance; i++){
      line_3 = iterate(line_1, line_3);

      iterate(line_3, line_2);
    }

    return ;
  }

  public CartesianLine iterate(CartesianLine line_1, CartesianLine line_2){
    Point interPoint = calcLineIntersection(line_1, line_2);
    CartesianLine centerLine = new CartesianLineImpl(calcSlope(circ.getCenter(), interPoint), interPoint, ctx);
    Point p = calcCircleIntersection(circ, centerLine);
    CartesianLine tangentLine = new CartesianLineImpl(-1/calcSlope(circ.getCenter(), interPoint), p);
    return tangentLine;
  }

  public Point calcLineIntersection(CartesianLine L1, CartesianLine L2){
    double X = ((L1.getSlope()*L1.getDefiningPoint().getX()) + L1.getDefiningPoint().getY() +
                (L2.getSlope()*L2.getDefiningPoint().getX()) + L2.getDefiningPoint().getY() )
                /(L1.getSlope() - L2.getSlope());
    double Y = (L1.getSlope()*(X - L1.getDefiningPoint().getX())) + L1.getDefiningPoint().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(Circle circ, CartesianLine line){
    circ.getCenter().getX()
  }

  public double calcSlope(Point P1, Point P2){
    return (P2.getY()-P1.getY())/(P2.getX()-P2.getX());
  }

}
