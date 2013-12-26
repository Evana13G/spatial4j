package com.spatial4j.core.shape.jts;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
  public List<Point> getEnclosingPolygon(int tolerance){
    double RADIUS = circ.getRadius();
    Point CENTER = circ.getCenter();
    CartesianLine line_1 = new CartesianLineImpl (0.0, CENTER, this.ctx);
    CartesianLine line_2 = new CartesianLineImpl (10000.0, CENTER, this.ctx);
    //CartesianLine line_2 = new CartesianLineImpl (Double.POSITIVE_INFINITY, CENTER, this.ctx);
    return recursiveIter(tolerance, line_1, line_2);
  }

//  public CartesianLine iterate(CartesianLine line_1, CartesianLine line_2){
//    Point interPoint = calcLineIntersection(line_1, line_2);
//    CartesianLine centerLine = new CartesianLineImpl(calcSlope(circ.getCenter(), interPoint), interPoint, ctx);
//    Point p = calcCircleIntersection(circ, centerLine);
//    CartesianLine tangentLine = new CartesianLineImpl(-1/calcSlope(circ.getCenter(), interPoint), p, ctx);
//    return tangentLine;
//  }

  public Point calcLineIntersection(CartesianLine L1, CartesianLine L2){
    double X = ((L1.getSlope()*L1.getDefiningPoint().getX()) + L1.getDefiningPoint().getY() +
                (L2.getSlope()*L2.getDefiningPoint().getX()) + L2.getDefiningPoint().getY() )
                /(L1.getSlope() - L2.getSlope());
    double Y = (L1.getSlope()*(X - L1.getDefiningPoint().getX())) + L1.getDefiningPoint().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(Circle circ, CartesianLine line){
    double radius = circ.getRadius();
    double theta = Math.atan(line.getSlope());
    double X = (radius*Math.cos(theta)) + circ.getCenter().getX();
    double Y = (radius*Math.sin(theta)) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(Circle circ, Point point){
    double radius = circ.getRadius();
    double slope = calcSlope(circ.getCenter(), point);
    double theta = Math.atan(slope);
    double X = radius*Math.cos(theta) + circ.getCenter().getX();
    double Y = radius*Math.sin(theta) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  public CartesianLine calcTangentLine(Point pt){
    return new CartesianLineImpl(-1/calcSlope(circ.getCenter(), pt), pt, ctx);
  }

  public double calcSlope(Point P1, Point P2){
    return (P2.getY()-P1.getY())/(P2.getX()-P2.getX());
  }

  public List<Point> recursiveIter(int iter, CartesianLine line1, CartesianLine line2){
    if(iter == 0){
      List<Point> listOfPoints =  new ArrayList<Point>();
      Point intersectionPoint = calcLineIntersection(line1, line2);
      listOfPoints.add(intersectionPoint);
      return listOfPoints;
    } else {
      List<Point> listOfPoints =  new ArrayList<Point>();
      Point intersectionPoint = calcLineIntersection(line1, line2);
      Point circleIntersectionPoint = calcCircleIntersection(circ, intersectionPoint);
      CartesianLine line3 = calcTangentLine(circleIntersectionPoint);
      listOfPoints.add(intersectionPoint);
      return addLst(listOfPoints, recursiveIter(iter - 1, line1, line3), recursiveIter(iter - 1, line3, line2));
    }
  }

  public List<Point> addLst(List<Point> lst1, List<Point> lst2, List<Point> lst3){
    lst1.addAll(lst2);
    lst1.addAll(lst3);
    return lst1;
  }

}