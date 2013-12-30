package com.spatial4j.core.context;


import java.util.ArrayList;
import java.util.List;

import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.shape.CartesianLine;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.CartesianLineImpl;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.shape.impl.RectangleImpl;

/**
 * Created by egizzi on 12/23/13.
 */
public class CirclePolygonizer {

  public static void main(String[] args) {
    SpatialContext ctx = new SpatialContext(false, new CartesianDistCalc(), new RectangleImpl(0, 100, 200, 300, null));
    CirclePolygonizer CirclePolygonizerTest = new CirclePolygonizer(ctx);
    List<Point> listOfPoints = CirclePolygonizerTest.getEnclosingPolygon(0.1);
    for(int i=0;i<listOfPoints.size(); i++){
      System.out.print(listOfPoints.get(i));
      System.out.print('\n');
    }
  }

  protected SpatialContext ctx;
  protected Circle circ;

  public CirclePolygonizer(SpatialContext ctx){
    this.ctx = ctx;
    this.circ = ctx.makeCircle(50.0, 250.0, 10.0);
  }

  //Note: tolerance will eventually be the maximum distance away from circle
  // for now, we will use it as the number of iterations to use
  public List<Point> getEnclosingPolygon(double tolerance){
    Point definingPoint1 = ctx.makePoint(circ.getCenter().getX(), circ.getCenter().getY()+circ.getRadius());
    Point definingPoint2 = ctx.makePoint(circ.getCenter().getX()+circ.getRadius(), circ.getCenter().getY());
    CartesianLine line_1 = new CartesianLineImpl (0.0, definingPoint1, this.ctx);
    CartesianLine line_2 = new CartesianLineImpl (10000000.0, definingPoint2, this.ctx);
//    CartesianLine line_2 = new CartesianLineImpl (Double.POSITIVE_INFINITY, definingPoint2, this.ctx);
    ArrayList<Point> listOfPoints = new ArrayList<Point>();
    recursiveIter(tolerance, line_1, line_2, listOfPoints);
    return listOfPoints;
  }

//  public CartesianLine iterate(CartesianLine line_1, CartesianLine line_2){
//    Point interPoint = calcLineIntersection(line_1, line_2);
//    CartesianLine centerLine = new CartesianLineImpl(calcSlope(circ.getCenter(), interPoint), interPoint, ctx);
//    Point p = calcCircleIntersection(circ, centerLine);
//    CartesianLine tangentLine = new CartesianLineImpl(-1/calcSlope(circ.getCenter(), interPoint), p, ctx);
//    return tangentLine;
//  }

  public Point calcLineIntersection(CartesianLine L1, CartesianLine L2){
    double X = ((L1.getSlope()*L1.getDefiningPoint().getX()) - L1.getDefiningPoint().getY() -
                (L2.getSlope()*L2.getDefiningPoint().getX()) + L2.getDefiningPoint().getY() )
                /(L1.getSlope() - L2.getSlope());
    double Y = (L1.getSlope()*(X - L1.getDefiningPoint().getX())) + L1.getDefiningPoint().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(CartesianLine line){
    double radius = circ.getRadius();
    double theta = Math.atan(line.getSlope());
    double X = (radius*Math.cos(theta)) + circ.getCenter().getX();
    double Y = (radius*Math.sin(theta)) + circ.getCenter().getY();
    return new PointImpl(X, Y, ctx);
  }

  public Point calcCircleIntersection(Point point){
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
    return (P2.getY()-P1.getY())/(P2.getX()-P1.getX());
  }

//  public List<Point> combineLists(List<Point> lst1, List<Point> lst2){
//    List<Point> lst3 = new ArrayList<Point>();
//    if (){
//
//    }else{
//
//    }
//
//    int minSize = (lst1.size()<=lst2.size() ? lst1.size() : lst2.size());
//    for (int i=0; i<minSize; i++){
//      if(!lst1.contains(lst2[i])){
//        lst3.add(lst2[i]);
//      };
//    };
//    lst1.addAll(lst2);
//    return lst1;
//  }

  public void recursiveIter(double tolerance, CartesianLine line1, CartesianLine line2, List<Point> listOfPoints){
    Point lineIntersectionPoint = calcLineIntersection(line1, line2);
    Point circleIntersectionPoint = calcCircleIntersection(lineIntersectionPoint);
    double currentMaxDistance = ctx.getDistCalc().distance(circleIntersectionPoint, lineIntersectionPoint);
    if (currentMaxDistance <= tolerance){
      listOfPoints.add(lineIntersectionPoint);
    } else {
      CartesianLine line3 = calcTangentLine(circleIntersectionPoint);
      recursiveIter(tolerance, line1, line3, listOfPoints);
      listOfPoints.add(circleIntersectionPoint);
      recursiveIter(tolerance, line3, line2,  listOfPoints);
    }
  }
}